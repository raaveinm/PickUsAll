package com.raaveinm.picasso.data.server

import com.raaveinm.core.model.toHttpBaseUrl
import com.raaveinm.core.model.toWsUrl
import com.raaveinm.picasso.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first

//
// Created by Kirill "Raaveinm" on 10/9/26.
//

data class ServerContext(
    val serverId: Long,
    val baseUrl: String,
    val wsUrl: String,
    val token: String,
    val selfSteamId: Long
)

/**
 * Where the rest of the data layer learns which server it is talking to. An interface so
 * the sync logic can be exercised against a fixed server without a settings database and
 * a login session behind it.
 */
interface ServerContextSource {
    val context: Flow<ServerContext?>

    suspend fun current(): ServerContext? = context.first()
}

/**
 * The selected server ([ServerSelection]) joined with *its* login session. Null means
 * "nothing to talk to": no server configured, signed out on the selected server, or the
 * cache still holds another server's data.
 *
 * The last condition is [cacheReadyFor]: after a switch the connection must not open
 * before the previous server's data is gone, or the first sync would write into rows that
 * are about to be wiped. `ServerCacheGuard` publishes the id once the cache is clean.
 */
class ActiveServer(
    authRepository: AuthRepository,
    cacheReadyFor: Flow<Long?>
) : ServerContextSource {
    override val context: Flow<ServerContext?> = combine(
        authRepository.activeSession,
        cacheReadyFor
    ) { active, readyFor ->
        if (active == null || readyFor != active.server.id) {
            null
        } else {
            ServerContext(
                serverId = active.server.id,
                baseUrl = active.server.url.toHttpBaseUrl(),
                wsUrl = active.server.url.toWsUrl(),
                token = active.token.token,
                selfSteamId = active.token.userId
            )
        }
    }.distinctUntilChanged()
}
