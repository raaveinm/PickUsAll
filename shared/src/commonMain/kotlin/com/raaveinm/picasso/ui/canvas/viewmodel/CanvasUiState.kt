package com.raaveinm.picasso.ui.canvas.viewmodel

import com.raaveinm.core.model.game.CommunityContent
import com.raaveinm.core.model.game.LibraryOrder
import com.raaveinm.core.model.user.OwnedGame

data class CanvasUiState(
    val userLibrary: List<OwnedGame> = emptyList(),
    val libraryOrder: LibraryOrder = LibraryOrder.NAME,
    val communityContent: List<CommunityContent> = emptyList()
)
