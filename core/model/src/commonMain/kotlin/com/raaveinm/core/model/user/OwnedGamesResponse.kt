package com.raaveinm.core.model.user

//
// Created by Kirill "Raaveinm" on 9/1/26.
//

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetOwnedGamesResponse(
    val response: OwnedGamesBody
)

@Serializable
data class OwnedGamesBody(
    @SerialName("game_count")
    val gameCount: Int = 0,
    val games: List<OwnedGame> = emptyList()
)
