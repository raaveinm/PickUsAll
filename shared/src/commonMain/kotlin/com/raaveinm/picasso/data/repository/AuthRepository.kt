package com.raaveinm.picasso.data.repository

import com.raaveinm.core.database.entities.server.Servers
import com.raaveinm.core.datastore.auth.AuthTokenStore
import com.raaveinm.core.datastore.auth.UserAuthToken
import com.raaveinm.core.model.toHttpBaseUrl
import com.raaveinm.picasso.data.AuthApi
import com.raaveinm.picasso.data.auth.secureNonce
import com.raaveinm.picasso.data.server.ServerSelection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Clock.System

//
// Created by Kirill "Raaveinm" on 10/1/26.
// Copyright (c) 2026 RetrogradeMercury. All rights reserved.
//

/* Must stay under the backend's PENDING_LOGIN_TTL_MS (5 min) - past it the nonce is pruned. */
private val LOGIN_POLL_TIMEOUT: Duration = 4.minutes
private val LOGIN_POLL_INTERVAL: Duration = 1500.milliseconds

/**
 * Why a login attempt couldn't be started, or didn't finish.
 *
 * There is deliberately no `Network` case: a failed poll is never fatal on its own
 * (the user may simply still be on Steam's page), so transport errors are retried
 * until the overall timeout and then reported as [TimedOut].
 */
sealed interface LoginFailure {
    /** No server configured yet - the user has to add one in Settings first. */
    data object NoServer : LoginFailure

    /** The user never finished on Steam's side, or the nonce expired before they did. */
    data object TimedOut : LoginFailure
}

/**
 * A valid session together with the server it belongs to. They travel as one value because
 * a token must never be paired with a server other than the one that issued it - two
 * separately observed flows could briefly disagree right after a server switch.
 */
data class ServerSession(val server: Servers, val token: UserAuthToken)

/**
 * Owns "who is logged in" - on the *selected* server (see [ServerSelection]). Every server
 * has its own session, so selecting another server changes [session] to whatever is stored
 * for that one, or to null when the user never signed in there (or the token expired).
 *
 * Follows the same single-source-of-truth rule as the other repositories: [session]
 * is what callers observe, and every method here exists only to *write* into the
 * DataStore behind it. Nothing returns the session as a value.
 *
 * The login handshake is three-legged because Steam's redirect lands in a browser,
 * not in this process (see picassobackend's `AuthController`): [startLogin] hands
 * back a URL to open and a nonce, then [awaitLogin] polls until the server releases
 * the token minted for that nonce.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class AuthRepository(
    private val authApi: AuthApi,
    private val authTokenStore: AuthTokenStore,
    private val serverSelection: ServerSelection
) {
    /** The selected server and its valid token; null = nothing selected, or signed out there. */
    val activeSession: Flow<ServerSession?> = serverSelection.server.flatMapLatest { server ->
        if (server == null) {
            flowOf(null)
        } else {
            authTokenStore.sessionFor(server.id).map { token ->
                // An expired token counts as no token: the server would refuse it anyway.
                token
                    ?.takeIf { it.isValidAt(System.now().toEpochMilliseconds()) }
                    ?.let { ServerSession(server, it) }
            }
        }
    }

    val session: Flow<UserAuthToken?> = activeSession.map { it?.token }.distinctUntilChanged()

    /**
     * Resolves the selected server and builds the browser URL for it. The returned
     * attempt has to be handed to [awaitLogin] - its nonce is what ties the eventual
     * token to this attempt, and it remembers which server the login was started on.
     */
    suspend fun startLogin(): Result<LoginAttempt> {
        val server: Servers = serverSelection.current() ?: return Result.failure(LoginError(LoginFailure.NoServer))
        val baseUrl: String = server.url.toHttpBaseUrl()
        val state = secureNonce()
        return Result.success(
            LoginAttempt(
                url = authApi.beginLoginUrl(baseUrl, state),
                state = state,
                serverId = server.id,
                baseUrl = baseUrl
            )
        )
    }

    /**
     * Polls until the backend releases the token for [attempt], then persists it under
     * the server the attempt was started on (even if the selection moved meanwhile) -
     * which is what makes [session] emit and the rest of the app notice the login.
     *
     * A single failed poll is not fatal: the server is reachable enough to have
     * been picked, and the user may still be mid-login on Steam's page.
     */
    suspend fun awaitLogin(attempt: LoginAttempt): Result<Unit> {
        val claimed: UserAuthToken? = withTimeoutOrNull(LOGIN_POLL_TIMEOUT) {
            var found: UserAuthToken? = null
            while (found == null) {
                found = try {
                    authApi.pollLogin(attempt.baseUrl, attempt.state)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Transient - keep polling until the overall timeout gives up.
                    null
                }
                if (found == null) delay(LOGIN_POLL_INTERVAL)
            }
            found
        }

        if (claimed == null) return Result.failure(LoginError(LoginFailure.TimedOut))

        authTokenStore.save(attempt.serverId, claimed)
        return Result.success(Unit)
    }

    /**
     * Clears the selected server's local session first and tells that server second: a logout
     * the user asked for has to stick even if the server is unreachable, otherwise a dead
     * network would leave them logged in against their wishes. The server-side token
     * then just expires on its own. Sessions on other servers are untouched.
     */
    suspend fun logout() {
        val server: Servers = serverSelection.current() ?: return
        val current: UserAuthToken? = authTokenStore.current(server.id)
        authTokenStore.clear(server.id)

        if (current == null) return
        try {
            authApi.logout(server.url.toHttpBaseUrl(), current.token)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Best effort - the local session is already gone.
        }
    }

    /**
     * Drops the local session of [serverId] because that server refused [token] (it lost or
     * revoked it). Nothing to tell the server. Only that exact token is dropped: a probe that
     * answers late must not sign out a session the user started in the meantime.
     */
    suspend fun forgetRejectedSession(serverId: Long, token: String) {
        authTokenStore.clearIfToken(serverId, token)
    }

    /** Forgets the session of a server the user deleted; its token would otherwise sit on disk forever. */
    suspend fun forgetServer(serverId: Long) {
        authTokenStore.clear(serverId)
    }
}

/**
 * @param serverId the server the login was started on - where the token will be stored.
 * @param baseUrl that server's http base url at the time, so a later edit or switch can't redirect the polling.
 */
data class LoginAttempt(val url: String, val state: String, val serverId: Long, val baseUrl: String)

class LoginError(val failure: LoginFailure) : Exception(failure.toString())
