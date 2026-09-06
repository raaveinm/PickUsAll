package com.raaveinm.picasso.data.repository

//
// Created by Kirill "Raaveinm" on 9/4/26.
//

import com.raaveinm.core.database.dao.UserDao
import com.raaveinm.core.database.entities.api.user.toEntity
import com.raaveinm.picasso.data.ApiClient
import com.raaveinm.picasso.data.PLAYER_SUMMARIES_LIMIT
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Single source of truth stays Room: this only writes, nothing here returns the friends.
 * The UI observes `ChatDao.getUserFriends()`, which re-emits once these writes land.
 */
class FriendsRepository(
    private val apiClient: ApiClient,
    private val userDao: UserDao
) {
    @OptIn(ExperimentalTime::class)
    suspend fun refresh() {
        val fetchedAt = Clock.System.now().epochSeconds
        val self = apiClient.userId
        val friends = apiClient.getFriendList(self)

        val profiles = (listOf(self) + friends.map { it.steamId })
            .distinct()
            .chunked(PLAYER_SUMMARIES_LIMIT)
            .flatMap { apiClient.getPlayerSummaries(it) }
        userDao.upsertUsers(profiles.map { it.toEntity(fetchedAt) })

        val cached = profiles.mapTo(mutableSetOf()) { it.steamId }
        userDao.replaceFriends(
            userSteamId = self,
            friends = friends
                .filter { it.steamId in cached }
                .map { it.toEntity(userSteamId = self, createdAt = fetchedAt) }
        )
    }
}
