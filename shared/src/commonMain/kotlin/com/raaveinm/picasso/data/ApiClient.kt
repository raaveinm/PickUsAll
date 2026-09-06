package com.raaveinm.picasso.data

import com.raaveinm.core.model.game.Game
import com.raaveinm.core.model.responses.GetFriendListResponse
import com.raaveinm.core.model.responses.GetGameStoreInfoResponse
import com.raaveinm.core.model.responses.GetOwnedGamesResponse
import com.raaveinm.core.model.responses.GetPlayerSummariesResponse
import com.raaveinm.core.model.user.Friend
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
const val PLAYER_SUMMARIES_LIMIT = 100

class ApiClient(private val httpClient: HttpClient) {
    private val steamApi: String
        get() = AppConfig.STEAM_API_KEY
    val userId: Long
        get() = AppConfig.USER_ID

    ///////////////////////////////////////////////
    // User's profile fetch
    ///////////////////////////////////////////////

    suspend fun getPlayerSummary(steamId: Long): User? =
        getPlayerSummaries(listOf(steamId)).firstOrNull()

    suspend fun getPlayerSummaries(steamIds: List<Long>): List<User> {
        if (steamIds.isEmpty()) return emptyList()
        require(steamIds.size <= PLAYER_SUMMARIES_LIMIT) {
            "GetPlayerSummaries takes at most $PLAYER_SUMMARIES_LIMIT ids, got ${steamIds.size}"
        }
        return httpClient.get("https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/") {
            parameter("key", steamApi)
            parameter("steamids", steamIds.joinToString(","))
            parameter("format", "json")
        }.body<GetPlayerSummariesResponse>().response.players
    }

    ///////////////////////////////////////////////
    // User's friend list fetch
    ///////////////////////////////////////////////

    suspend fun getFriendList(steamId: Long): List<Friend> =
        httpClient.get("https://api.steampowered.com/ISteamUser/GetFriendList/v0001/") {
            parameter("key", steamApi)
            parameter("steamid", steamId)
            parameter("relationship", "friend")
            parameter("format", "json")
        }.body<GetFriendListResponse>().friendsList?.friends.orEmpty()

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
