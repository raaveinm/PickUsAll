package com.raaveinm.picasso.ui.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raaveinm.core.database.dao.UserDao
import com.raaveinm.core.database.entities.api.user.toDto
import com.raaveinm.core.model.user.User
import com.raaveinm.features.impl_webrtc.CallManager
import com.raaveinm.features.impl_webrtc.IncomingCall
import com.raaveinm.picasso.data.repository.AuthRepository
import com.raaveinm.picasso.data.auth.secureNonce
import com.raaveinm.picasso.data.repository.LoginError
import com.raaveinm.picasso.data.repository.LoginFailure
import com.raaveinm.picasso.data.sync.ServerCacheGuard
import com.raaveinm.picasso.data.sync.SignedOutCacheGuard
import com.raaveinm.picasso.data.sync.SyncCoordinator
import com.raaveinm.picasso.ui.actions.ClipboardHelper
import com.raaveinm.picasso.ui.navigation.SideWindow
import com.raaveinm.picasso.ui.navigation.SideWindowRequest
import com.raaveinm.pickusall.core.designsystem.utils.WarnLevel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class AppMessage(
    val level: WarnLevel,
    val text: String
)

data class AppUiState(
    val selectedTab: Int = 0,
    val isSideBarExpanded: Boolean = false,
    val isBottomBarHidden: Boolean = false,
    val message: AppMessage? = null,
    val dismissTimer: Boolean = false,
    val user: User? = null,
    val isLoggingIn: Boolean = false,
    val sideWindowRequest: SideWindowRequest? = null,
    val incomingCaller: User? = null,
) {
    val isLoggedIn: Boolean get() = user != null
}

/**
 * Holds UI state shared across the whole app shell (bottom nav selection,
 * sidebar visibility, the global warning/error banner) rather than any single
 * screen. Other viewmodels/repositories can be pointed at [postMessage] as a
 * sink for cross-cutting failures (failed sends, sync errors, ...) once there's
 * a call site for that.
 *
 * Also owns the Steam login/logout the sidebar triggers - see [login] for why
 * that's a two-step dance rather than one call.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(
    private val authRepository: AuthRepository,
    private val userDao: UserDao,
    private val syncCoordinator: SyncCoordinator,
    private val signedOutCacheGuard: SignedOutCacheGuard,
    private val serverCacheGuard: ServerCacheGuard,
    private val callManager: CallManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState = _uiState.asStateFlow()
    private var dismissJob: Job? = null
    private var loginJob: Job? = null
    private var activeLoginId: String? = null
    private var incomingCallRequestId: String? = null

    init {
        signedOutCacheGuard.start()
        serverCacheGuard.start()
        syncCoordinator.start()

        callManager.incomingCall
            .onEach(::onIncomingCallChanged)
            .flatMapLatest { call ->
                if (call == null) flowOf(null)
                else userDao.observeUser(call.peerSteamId).map { it?.toDto() }
            }
            .onEach { caller -> _uiState.update { it.copy(incomingCaller = caller) } }
            .launchIn(viewModelScope)

        syncCoordinator.sessionRejected
            .onEach {
                postMessage(WarnLevel.WARN, "SIGNED OUT: the server no longer accepts this session, log in again (auth_e103)")
            }
            .launchIn(viewModelScope)

        authRepository.session
            .map { it?.userId }
            .distinctUntilChanged()
            .flatMapLatest { id -> if (id == null) flowOf(null) else userDao.observeUser(id) }
            .onEach { row -> _uiState.update { it.copy(user = row?.toDto()) } }
            .launchIn(viewModelScope)
    }

    ///////////////////////////////////////////////
    // Auth
    ///////////////////////////////////////////////

    /**
     * Presents the server's Steam login URL in the assistive window while the repository
     * polls for completion. Closing that presentation cancels this local login attempt.
     */
    fun login() {
        if (_uiState.value.isLoggingIn) return
        val loginId: String = secureNonce()
        activeLoginId = loginId
        _uiState.update { it.copy(isLoggingIn = true) }
        loginJob = viewModelScope.launch {
            try {
                authRepository.startLogin()
                    .onSuccess { attempt ->
                        val request = SideWindowRequest(
                            destination = SideWindow.WebAuth(
                                url = attempt.url,
                                loginAttemptId = attempt.state,
                            ),
                            id = loginId,
                        )
                        _uiState.update { it.copy(sideWindowRequest = request) }
                        authRepository.awaitLogin(attempt)
                            .onFailure { error -> postMessage(WarnLevel.WARN, error.describe()) }
                    }
                    .onFailure { error -> postMessage(WarnLevel.ERROR, error.describe()) }
            } finally {
                if (activeLoginId == loginId) {
                    activeLoginId = null
                    _uiState.update { state ->
                        state.copy(
                            isLoggingIn = false,
                            sideWindowRequest = state.sideWindowRequest?.takeUnless { it.id == loginId },
                        )
                    }
                }
            }
        }
    }

    fun logout() {
        cancelLogin()
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // logout() already cleared the local session; nothing left to undo.
            }
        }
    }

    private fun Throwable.describe(): String = when ((this as? LoginError)?.failure) {
        LoginFailure.NoServer -> "LOGIN FAILED: add a server in Settings first (auth_e100)"
        LoginFailure.TimedOut -> "LOGIN TIMED OUT: the Steam sign-in wasn't completed (auth_e101)"
        else -> "LOGIN FAILED: ${message ?: "unknown error"} (auth_e102)"
    }

    ///////////////////////////////////////////////
    // Navigation
    ///////////////////////////////////////////////

    fun selectTab(tab: Int) = _uiState.update { it.copy(selectedTab = tab) }

    fun setSideBarExpanded(expanded: Boolean) = _uiState.update { it.copy(isSideBarExpanded = expanded) }

    fun toggleSideBar() = _uiState.update { it.copy(isSideBarExpanded = !it.isSideBarExpanded) }

    fun hideBottomBar() = _uiState.update { it.copy(isBottomBarHidden = true) }

    fun showBottomBar() = _uiState.update { it.copy(isBottomBarHidden = false) }

    ///////////////////////////////////////////////
    // Logging
    ///////////////////////////////////////////////

    fun postMessage(level: WarnLevel, text: String) {
        dismissJob?.cancel()

        _uiState.update { it.copy(message = AppMessage(level, text)) }

        dismissJob = viewModelScope.launch {
            delay(7000.milliseconds)
            _uiState.update { it.copy(message = null) }
        }
    }

    fun dismissMessage() {
        dismissJob?.cancel()
        dismissJob = null
        _uiState.update { it.copy(message = null) }
    }

    fun copyMessage() {
        ClipboardHelper.setText(uiState.value.message?.text ?: "no_err")
    }

    ///////////////////////////////////////////////
    // Side window requests
    ///////////////////////////////////////////////

    fun setWindowRequest(screen: SideWindow): SideWindowRequest {
        val request = SideWindowRequest(destination = screen)
        _uiState.update { it.copy(sideWindowRequest = request) }
        return request
    }

    /** Ignores dismissals from a screen that has already been replaced. */
    fun resetSideWindowRequest(requestId: String) {
        val request: SideWindowRequest = _uiState.value.sideWindowRequest?.takeIf { it.id == requestId } ?: return
        val call: SideWindow.IncomingCall? = request.destination as? SideWindow.IncomingCall
        if (call != null) callManager.declineIncomingCall(call.callId)
        if (activeLoginId == requestId) cancelLogin()
        clearSideWindowRequest(requestId)
    }

    fun acceptIncomingCall(requestId: String) {
        val request: SideWindowRequest = _uiState.value.sideWindowRequest?.takeIf { it.id == requestId } ?: return
        val call: SideWindow.IncomingCall = request.destination as? SideWindow.IncomingCall ?: return
        callManager.acceptIncomingCall(call.callId)
        clearSideWindowRequest(requestId)
    }

    private fun clearSideWindowRequest(requestId: String) {
        _uiState.update { state ->
            state.copy(sideWindowRequest = state.sideWindowRequest?.takeUnless { it.id == requestId })
        }
    }

    private fun onIncomingCallChanged(call: IncomingCall?) {
        if (call == null) {
            incomingCallRequestId?.let(::clearSideWindowRequest)
            incomingCallRequestId = null
        } else {
            val request: SideWindowRequest = setWindowRequest(
                SideWindow.IncomingCall(callId = call.id, peerSteamId = call.peerSteamId),
            )
            incomingCallRequestId = request.id
        }
    }

    private fun cancelLogin() {
        val loginId: String? = activeLoginId
        activeLoginId = null
        loginJob?.cancel()
        loginJob = null
        _uiState.update { state ->
            state.copy(
                isLoggingIn = false,
                sideWindowRequest = state.sideWindowRequest?.takeUnless { it.id == loginId },
            )
        }
    }
}
