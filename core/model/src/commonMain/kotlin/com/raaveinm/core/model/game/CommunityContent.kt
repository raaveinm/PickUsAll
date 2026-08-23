package com.raaveinm.core.model.game

import com.raaveinm.core.model.user.OwnedGame

// UI-facing convenience shape for the Color Picker screen (library entry + candidate
// screenshots/tags) — doesn't map to a single Steam endpoint 1:1, unlike the DTOs
// elsewhere in this module. Worth revisiting once the Color Picker repository is real:
// this data will actually be assembled from OwnedGame + GameDetails + local flags.
data class CommunityContent(
    val ownedGame: OwnedGame,
    val imageUrl: List<String>,
    val inLibrary: Boolean,
    val gameTags: List<String>
)
