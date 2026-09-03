package com.raaveinm.core.model.responses

import com.raaveinm.core.model.game.Game
import kotlinx.serialization.Serializable

//
// Created by Kirill "Raaveinm" on 9/1/26.
//

///////////////////////////////////////////////
// URLs (For Reference)
///////////////////////////////////////////////
// Store Game Info: https://store.steampowered.com/api/appdetails?appids=${appId}&format=json

// Keyed by app id (e.g. "620") rather than a fixed field, since the store API
// echoes back whatever appid was requested as the top-level JSON key.
typealias GetGameStoreInfoResponse = Map<String, GameStoreInfoEntry>

@Serializable
data class GameStoreInfoEntry(
    val success: Boolean,
    val data: Game? = null
)
