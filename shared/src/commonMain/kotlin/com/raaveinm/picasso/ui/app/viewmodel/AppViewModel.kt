package com.raaveinm.picasso.ui.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raaveinm.core.database.dao.UserDao
import com.raaveinm.core.database.entities.api.user.toDto
import com.raaveinm.core.model.user.User
import com.raaveinm.picasso.data.repository.AuthRepository
import com.raaveinm.picasso.data.repository.LoginError
import com.raaveinm.picasso.data.repository.LoginFailure
import com.raaveinm.picasso.data.sync.SyncCoordinator
import com.raaveinm.picasso.ui.actions.ClipboardHelper
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
    val message: AppMessage? = null,
    val dismissTimer: Boolean = false,
    val user: User? = null,
    val isLoggingIn: Boolean = false,
    val pendingLoginUrl: String? = null
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
    private val syncCoordinator: SyncCoordinator
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState = _uiState.asStateFlow()
    private var dismissJob: Job? = null
    private var loginJob: Job? = null

    init {
        syncCoordinator.start()

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
     * Step one of the login: resolves the active server, mints a nonce and puts the
     * `/auth/steam/begin` URL in [AppUiState.pendingLoginUrl]. The *UI* has to open
     * it, because Steam's consent page is a browser flow - there is nothing to show
     * in-app. [onLoginUrlOpened] then starts polling for the token.
     */
    fun login() {
        if (_uiState.value.isLoggingIn) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingIn = true) }
            authRepository.startLogin()
                .onSuccess { attempt ->
                    _uiState.update { it.copy(pendingLoginUrl = attempt.url) }
                    awaitLogin(attempt.state)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoggingIn = false) }
                    postMessage(WarnLevel.ERROR, error.describe())
                }
        }
    }

    fun onLoginUrlOpened() {
        _uiState.update { it.copy(pendingLoginUrl = null) }
    }

    private fun awaitLogin(state: String) {
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            try {
                authRepository.awaitLogin(state)
                    .onFailure { error -> postMessage(WarnLevel.WARN, error.describe()) }
            } finally {
                _uiState.update { it.copy(isLoggingIn = false) }
            }
        }
    }

    fun logout() {
        loginJob?.cancel()
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // logout() already cleared the local session; nothing left to undo.
            }
            _uiState.update { it.copy(isLoggingIn = false, pendingLoginUrl = null) }
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

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setSideBarExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isSideBarExpanded = expanded) }
    }

    fun toggleSideBar() {
        _uiState.update { it.copy(isSideBarExpanded = !it.isSideBarExpanded) }
    }

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
}
