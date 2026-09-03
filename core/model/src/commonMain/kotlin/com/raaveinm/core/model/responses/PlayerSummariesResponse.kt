package com.raaveinm.core.model.responses

//
// Created by Kirill "Raaveinm" on 9/3/26.
//

import com.raaveinm.core.model.user.User
import kotlinx.serialization.Serializable

@Serializable
data class GetPlayerSummariesResponse(
    val response: PlayerSummariesBody
)

@Serializable
data class PlayerSummariesBody(
    val players: List<User> = emptyList()
)
