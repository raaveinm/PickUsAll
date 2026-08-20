package com.raaveinm.core.model.lib

data class CommunityContent(
    val gameInfo: GameInfo,
    val imageUrl: List<String>,
    val inLibrary: Boolean,
    val gameTags: List<String>
)
