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

    /**
     * Finds the Artist behind [query] - a SteamID64, a profile URL (`/profiles/<id>` or
     * `/id/<vanity>`) or a bare vanity name - and caches their profile in Room. Returns the
     * steamId to observe, or null when nothing matched (or Steam could not be reached).
     */
    suspend fun lookup(query: String): Long? {
        val target = parseProfileQuery(query) ?: return null
        return try {
            val steamId = when (target) {
                is ProfileQuery.ById -> target.steamId
                is ProfileQuery.ByVanity -> apiClient.resolveVanityUrl(target.vanity) ?: return null
            }
            val profile = apiClient.getPlayerSummary(steamId) ?: return null
            userDao.addUser(profile.toEntity(Clock.System.now().epochSeconds))
            steamId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("ProfilesRepository.lookup failed: $e")
            null
        }
    }
}

///////////////////////////////////////////////
/// Profile query parsing
///////////////////////////////////////////////

/** SteamID64s are 17 digits and start at this value (individual accounts, public universe). */
private const val STEAM_ID64_BASE: Long = 76561197960265728L

internal sealed interface ProfileQuery {
    data class ById(val steamId: Long) : ProfileQuery
    data class ByVanity(val vanity: String) : ProfileQuery
}

/** Steam vanity names: 2..32 characters of letters, digits, `_` and `-`. */
private val VANITY_NAME = Regex("^[A-Za-z0-9_-]{2,32}$")

/**
 * Turns what a user typed into something Steam can answer, without a network call:
 * `76561198…` -> id, `…steamcommunity.com/profiles/<id>` -> id, `…/id/<name>` -> vanity,
 * a bare `name` -> vanity. Anything else (spaces, other sites) -> null.
 */
internal fun parseProfileQuery(query: String): ProfileQuery? {
    val trimmed = query.trim().trimEnd('/')
    if (trimmed.isEmpty()) return null

    trimmed.toLongOrNull()?.let { id ->
        return if (id >= STEAM_ID64_BASE) ProfileQuery.ById(id) else null
    }

    val path = trimmed.substringAfter("steamcommunity.com/", missingDelimiterValue = "")
    if (path.isNotEmpty()) {
        val segments = path.split('/')
        val value = segments.getOrNull(1)?.substringBefore('?') ?: return null
        return when (segments.first()) {
            "profiles" -> value.toLongOrNull()?.takeIf { it >= STEAM_ID64_BASE }?.let { ProfileQuery.ById(it) }
            "id" -> value.takeIf { VANITY_NAME.matches(it) }?.let { ProfileQuery.ByVanity(it) }
            else -> null
        }
    }

    return if (VANITY_NAME.matches(trimmed)) ProfileQuery.ByVanity(trimmed) else null
}
