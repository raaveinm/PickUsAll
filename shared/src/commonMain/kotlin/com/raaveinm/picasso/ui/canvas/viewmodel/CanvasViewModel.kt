package com.raaveinm.picasso.ui.canvas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raaveinm.core.database.dao.GameDao
import com.raaveinm.core.database.dao.UserDao
import com.raaveinm.core.database.entities.api.game.toCommunityContent
import com.raaveinm.core.database.entities.api.user.toDto
import com.raaveinm.core.database.entities.game.GameQueue
import com.raaveinm.core.database.entities.game.toDto
import com.raaveinm.core.model.game.LibraryOrder
import com.raaveinm.picasso.data.repository.AuthRepository
import com.raaveinm.picasso.data.repository.GameStoreRepository
import com.raaveinm.picasso.data.repository.OwnedGamesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CanvasViewModel(
    private val userDao: UserDao,
    private val gameDao: GameDao,
    private val ownedGamesRepository: OwnedGamesRepository,
    private val gameStoreRepository: GameStoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CanvasUiState())
    val uiState = _uiState.asStateFlow()
    private val _libraryOrder = MutableStateFlow(LibraryOrder.NAME)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    /**
     * The logged-in steamId, or null. Every library/queue query is keyed on it, so
     * logging in or out has to re-subscribe them rather than just re-run them -
     * hence [flatMapLatest] over the session instead of reading an id once.
     */
    private val selfSteamId = authRepository.session
        .map { it?.userId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        combine(
            selfSteamId.flatMapLatest { id ->
                if (id == null) flowOf(emptyList()) else userDao.getUserLibrary(id)
            },
            gameDao.observeGamesWithDetails(),
            selfSteamId.flatMapLatest { id ->
                if (id == null) flowOf(emptyList()) else gameDao.observeQueue(id)
            },
            _libraryOrder
        ) { library, gamesWithDetails, queue, order ->
            val sortedLibrary = when (order) {
                LibraryOrder.NAME -> library.sortedBy { it.name }
                LibraryOrder.PLAYTIME -> library.sortedByDescending { it.playtimeForever }
                LibraryOrder.LAST_PLAYED -> library.sortedByDescending { it.rTimeLastPlayed }
            }
            CanvasUiState(
                userLibrary = sortedLibrary.map { it.toDto() },
                libraryOrder = order,
                communityContent = gamesWithDetails.toCommunityContent(library),
                gameQueue = queue.map { it.toDto() }
            )
        }.onEach { state -> _uiState.update { state } }.launchIn(viewModelScope)

        selfSteamId
            .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else userDao.getUserLibrary(id) }
            .onEach { library -> gameStoreRepository.refreshMissingDetails(library.map { it.appId }) }
            .launchIn(viewModelScope)

        // Re-pull the library whenever a *different* user signs in, not just at startup.
        selfSteamId
            .onEach { id -> if (id != null) refreshLibrary() }
            .launchIn(viewModelScope)
    }

    fun onOrderChanged(order: LibraryOrder) {
        _libraryOrder.update { order }
    }

    fun reorderQueue(orderedGameIds: List<Int>) {
        val self = selfSteamId.value ?: return
        viewModelScope.launch {
            gameDao.reorderQueue(self, orderedGameIds)
        }
    }

    fun addToQueue(gameId: Int, priority: Int) {
        val self = selfSteamId.value ?: return
        viewModelScope.launch {
            try {
                gameStoreRepository.refreshMissingDetails(listOf(gameId))
                if (gameId !in gameDao.getCachedAppIds()) {
                    println("CanvasViewModel.addToQueue: appId=$gameId has no Games row after refresh, not adding")
                    return@launch
                }
                gameDao.addToQueue(
                    GameQueue(id = gameId.toLong(), userId = self, gameId = gameId, priority = priority)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // TODO: surface a real error state once there's a UI for it (e.g.
                // invalid appId that Steam's store API doesn't recognize).
                println("CanvasViewModel.addToQueue failed: ${e.stackTraceToString()}")
            }
        }
    }

    fun refreshLibrary() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            val self = authRepository.session.first()?.userId ?: return@launch
            _isRefreshing.value = true
            try {
                ownedGamesRepository.refresh(self)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
//                _uiState.update {it.copy(warning = Pair(WarnLevel.ERROR, "GAME LIB UPDATE ERR: glue_400")) }
                println("CanvasViewModel.refreshLibrary failed: ${e.stackTraceToString()}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
