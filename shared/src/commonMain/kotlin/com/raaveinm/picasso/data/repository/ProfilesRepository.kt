package com.raaveinm.picasso.data.repository

import com.raaveinm.core.database.dao.ChatDao
import com.raaveinm.core.database.dao.UserDao
import com.raaveinm.core.database.entities.api.user.toEntity
import com.raaveinm.picasso.data.ApiClient
import com.raaveinm.picasso.data.PLAYER_SUMMARIES_LIMIT
import kotlinx.coroutines.CancellationException
import kotlin.time.Clock

//
// Created by Kirill "Raaveinm" on 10/9/26.
//

/** What the sync layer needs from the profile cache: "replace whatever placeholders you can". */
fun interface ProfileHydrator {
    suspend fun hydrateStubs()
}

class ProfilesRepository(
    private val apiClient: ApiClient,
    private val chatDao: ChatDao,
    private val userDao: UserDao
) : ProfileHydrator {
    override suspend fun hydrateStubs() {
        val stubs = chatDao.getStubSteamIds()
        if (stubs.isEmpty()) return
        try {
            val fetchedAt = Clock.System.now().epochSeconds
            val profiles = stubs
                .chunked(PLAYER_SUMMARIES_LIMIT)
                .flatMap { apiClient.getPlayerSummaries(it) }
            userDao.upsertUsers(profiles.map { it.toEntity(fetchedAt) })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("ProfilesRepository.hydrateStubs failed: $e")
        }
    }
}
