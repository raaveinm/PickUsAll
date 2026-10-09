package com.raaveinm.picasso.data.server

import com.raaveinm.core.database.dao.ServerDao
import com.raaveinm.core.model.toHttpBaseUrl
import com.raaveinm.core.model.toWsUrl
import com.raaveinm.picasso.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

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
 * The active server joined with the login session. Null means "nothing to talk to":
 * no server configured, or signed out.
 *
 * "Active" is still the first of the server list - `ORDER BY added DESC`, i.e. the
 * most recently added - exactly as `AuthRepository` picks it. The real, user-chosen
 * selection (with flush-then-switch) replaces this one function when it lands.
 */
class ActiveServer(
    serverDao: ServerDao,
    authRepository: AuthRepository
) : ServerContextSource {
    override val context: Flow<ServerContext?> = combine(
        serverDao.getAllServers().map { servers -> servers.firstOrNull { it.url.isNotBlank() } },
        authRepository.session
    ) { server, session ->
        if (server == null || session == null) {
            null
        } else {
            ServerContext(
                serverId = server.id,
                baseUrl = server.url.toHttpBaseUrl(),
                wsUrl = server.url.toWsUrl(),
                token = session.token,
                selfSteamId = session.userId
            )
        }
    }.distinctUntilChanged()
}
