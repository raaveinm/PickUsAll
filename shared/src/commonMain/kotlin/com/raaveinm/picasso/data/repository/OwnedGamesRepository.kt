package com.raaveinm.picasso.data.repository

//
// Created by Kirill "Raaveinm" on 9/1/26.
//

import com.raaveinm.core.database.dao.UserDao
import com.raaveinm.core.database.entities.api.user.toEntity
import com.raaveinm.picasso.data.ApiClient
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
class OwnedGamesRepository(
    private val apiClient: ApiClient,
    private val userDao: UserDao
) {
    @OptIn(ExperimentalTime::class)
    suspend fun refresh(selfSteamId: Long) {
        val fetchedAt = Clock.System.now().epochSeconds
        val summary = apiClient.getPlayerSummary(selfSteamId)
            ?: error("GetPlayerSummaries returned no player for steamId=$selfSteamId")
        userDao.addUser(summary.toEntity(fetchedAt))
        val games = apiClient.getOwnedGames(selfSteamId)
        userDao.addOwnedGames(games.map { it.toEntity(selfSteamId, fetchedAt) })
    }
}
