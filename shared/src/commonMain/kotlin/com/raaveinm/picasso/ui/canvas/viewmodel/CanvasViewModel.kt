package com.raaveinm.picasso.ui.canvas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raaveinm.core.database.dao.GameDao
import com.raaveinm.core.database.dao.UserDao
import com.raaveinm.core.database.entities.api.game.toCommunityContent
import com.raaveinm.core.database.entities.api.user.toDto
import com.raaveinm.core.model.game.LibraryOrder
import com.raaveinm.picasso.AppConfig
import com.raaveinm.picasso.data.repository.GameStoreRepository
import com.raaveinm.picasso.data.repository.OwnedGamesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class CanvasViewModel(
    private val userDao: UserDao,
    private val gameDao: GameDao,
    private val ownedGamesRepository: OwnedGamesRepository,
    private val gameStoreRepository: GameStoreRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CanvasUiState())
    val uiState = _uiState.asStateFlow()
    private val _libraryOrder = MutableStateFlow(LibraryOrder.NAME)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        combine(
            userDao.getUserLibrary(AppConfig.USER_ID),
            gameDao.observeGamesWithDetails(),
            _libraryOrder
        ) { library, gamesWithDetails, order ->
            val sortedLibrary = when (order) {
                LibraryOrder.NAME -> library.sortedBy { it.name }
                LibraryOrder.PLAYTIME -> library.sortedByDescending { it.playtimeForever }
                LibraryOrder.LAST_PLAYED -> library.sortedByDescending { it.rTimeLastPlayed }
            }
            CanvasUiState(
                userLibrary = sortedLibrary.map { it.toDto() },
                libraryOrder = order,
                communityContent = gamesWithDetails.toCommunityContent(library)
            )
        }.onEach { state -> _uiState.update { state } }.launchIn(viewModelScope)

        userDao.getUserLibrary(AppConfig.USER_ID)
            .onEach { library -> gameStoreRepository.refreshMissingDetails(library.map { it.appId }) }
            .launchIn(viewModelScope)

        refreshLibrary()
    }

    fun onOrderChanged(order: LibraryOrder) {
        _libraryOrder.update { order }
    }

    fun refreshLibrary() {
        if (_isRefreshing.value) return // ignore an overlapping pull/entry-refresh
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                ownedGamesRepository.refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // TODO: surface a real error state once there's a UI for it; for now
                // a failed refresh just leaves the last cached library in place.
                println("CanvasViewModel.refreshLibrary failed: $e")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
