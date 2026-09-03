package com.raaveinm.picasso.data

import com.raaveinm.core.model.game.Game
import com.raaveinm.core.model.responses.GetGameStoreInfoResponse
import com.raaveinm.core.model.responses.GetOwnedGamesResponse
import com.raaveinm.core.model.responses.GetPlayerSummariesResponse
import com.raaveinm.core.model.user.OwnedGame
import com.raaveinm.core.model.user.User
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

    ///////////////////////////////////////////////
    // User's profile fetch
    ///////////////////////////////////////////////

    suspend fun getPlayerSummary(steamId: Long): User? =
        httpClient.get("https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/") {
            parameter("key", steamApi)
            parameter("steamids", steamId)
            parameter("format", "json")
        }.body<GetPlayerSummariesResponse>().response.players.firstOrNull()

    ///////////////////////////////////////////////
    // User's owned games fetch
    ///////////////////////////////////////////////

    suspend fun getOwnedGames(): List<OwnedGame> =
        httpClient.get("https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/") {
            parameter("key", steamApi)
            parameter("steamid", userId)
            parameter("include_appinfo", true)
            parameter("include_played_free_games", true)
            parameter("format", "json")
        }.body<GetOwnedGamesResponse>().response.games

    ///////////////////////////////////////////////
    // Store's game info fetch
    ///////////////////////////////////////////////
    suspend fun getGameStoreInfo(appId: Long): Game? =
        httpClient.get("https://store.steampowered.com/api/appdetails") {
            parameter("appids", appId)
            parameter("format", "json")
        }.body<GetGameStoreInfoResponse>()[appId.toString()]?.data
}
