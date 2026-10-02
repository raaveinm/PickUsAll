package com.raaveinm.picasso.data.repository

import com.raaveinm.core.database.dao.ServerDao
import com.raaveinm.core.datastore.auth.AuthTokenStore
import com.raaveinm.core.datastore.auth.UserAuthToken
import com.raaveinm.core.model.toHttpBaseUrl
import com.raaveinm.picasso.data.AuthApi
import com.raaveinm.picasso.data.auth.secureNonce
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

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
 * Owns "who is logged in".
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
class AuthRepository(
    private val authApi: AuthApi,
    private val authTokenStore: AuthTokenStore,
    private val serverDao: ServerDao
) {
    val session: Flow<UserAuthToken?> = authTokenStore.session

    /**
     * Resolves the active server and builds the browser URL for it. The returned
     * nonce has to be handed to [awaitLogin] - it's what ties the eventual token to
     * this attempt.
     */
    suspend fun startLogin(): Result<LoginAttempt> {
        val baseUrl = activeServerBaseUrl() ?: return Result.failure(LoginError(LoginFailure.NoServer))
        val state = secureNonce()
        return Result.success(
            LoginAttempt(url = authApi.beginLoginUrl(baseUrl, state), state = state)
        )
    }

    /**
     * Polls until the backend releases the token for [state], then persists it -
     * which is what makes [session] emit and the rest of the app notice the login.
     *
     * A single failed poll is not fatal: the server is reachable enough to have
     * been picked, and the user may still be mid-login on Steam's page.
     */
    suspend fun awaitLogin(state: String): Result<Unit> {
        val baseUrl = activeServerBaseUrl() ?: return Result.failure(LoginError(LoginFailure.NoServer))

        val claimed: UserAuthToken? = withTimeoutOrNull(LOGIN_POLL_TIMEOUT) {
            var found: UserAuthToken? = null
            while (found == null) {
                found = try {
                    authApi.pollLogin(baseUrl, state)
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

        authTokenStore.save(claimed)
        return Result.success(Unit)
    }

    /**
     * Clears the local session first and tells the server second: a logout the user
     * asked for has to stick even if the server is unreachable, otherwise a dead
     * network would leave them logged in against their wishes. The server-side token
     * then just expires on its own.
     */
    suspend fun logout() {
        val current = authTokenStore.current()
        authTokenStore.clear()

        val baseUrl = activeServerBaseUrl() ?: return
        if (current == null) return
        try {
            authApi.logout(baseUrl, current.token)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Best effort - the local session is already gone.
        }
    }

    /*
     * "Active server" is still first-of-list, matching ChatViewModel's signaling
     * pick. Becomes a real selection once the multi-server switch in Settings lands.
     */
    private suspend fun activeServerBaseUrl(): String? =
        serverDao.getAllServers().first()
            .firstOrNull { it.url.isNotBlank() }
            ?.url
            ?.toHttpBaseUrl()
}

data class LoginAttempt(val url: String, val state: String)

class LoginError(val failure: LoginFailure) : Exception(failure.toString())
