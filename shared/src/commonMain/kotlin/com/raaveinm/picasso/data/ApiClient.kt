package com.raaveinm.picasso.data

import com.raaveinm.core.model.user.GetOwnedGamesResponse
import com.raaveinm.core.model.user.OwnedGame
import com.raaveinm.picasso.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

//
// Created by Kirill "Raaveinm" on 8/5/26.
// Copyright (c) 2026 RetrogradeMercury. All rights reserved.
//

class ApiClient(private val httpClient: HttpClient) {
    private val steamApi: String
        get() = AppConfig.STEAM_API_KEY
    val userId: Long
        get() = AppConfig.USER_ID

    suspend fun getOwnedGames(): List<OwnedGame> =
        httpClient.get("https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/") {
            parameter("key", steamApi)
            parameter("steamid", userId)
            parameter("include_appinfo", true)
            parameter("include_played_free_games", true)
            parameter("format", "json")
        }.body<GetOwnedGamesResponse>().response.games
}
