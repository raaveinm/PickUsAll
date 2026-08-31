package com.raaveinm.picasso.ui.canvas.viewmodel

import com.raaveinm.core.database.entities.api.game.Games
import com.raaveinm.core.model.user.OwnedGame

data class CanvasUiState(
    val userLibrary: List<OwnedGame> = emptyList(),
    val gameStore: List<Games> = emptyList()
)
