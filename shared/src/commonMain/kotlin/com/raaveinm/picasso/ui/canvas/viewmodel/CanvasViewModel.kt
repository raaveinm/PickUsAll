package com.raaveinm.picasso.ui.canvas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raaveinm.core.database.dao.GameDao
import com.raaveinm.core.database.dao.UserDao
import com.raaveinm.core.database.entities.api.user.toDto
import com.raaveinm.picasso.AppConfig
import com.raaveinm.picasso.data.mock.Mock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class CanvasViewModel(
    private val userDao: UserDao,
    private val gameDao: GameDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(CanvasUiState())
    val uiState = _uiState.asStateFlow()


    init {
        combine(
            userDao.getUserLibrary(AppConfig.USER_ID),
            gameDao.observeGames()
        ) { library, games ->
            CanvasUiState(
                userLibrary = library.map { it.toDto() },
                gameStore = games
            )
        }.onEach { state -> _uiState.update { state } }.launchIn(viewModelScope)
    }
}
