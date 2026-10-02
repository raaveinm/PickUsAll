package com.raaveinm.picasso.data

import com.raaveinm.core.datastore.auth.UserAuthToken
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//
// Created by Kirill "Raaveinm" on 10/1/26.
// Copyright (c) 2026 RetrogradeMercury. All rights reserved.
//

/**
 * picassobackend's auth endpoints. See that repo's `transport/http/AuthController.hpp`.
 *
 * @sample GET http://domain.or.ip/auth/steam/poll?state=<nonce>
 */
@Serializable
data class AuthTokenResponse(
    /** Opaque bearer token; the server keeps only its SHA-256. */
    @SerialName("token") val token: String,
    /** Sent as a string - a 64-bit steamId doesn't survive a JSON number intact. */
    @SerialName("steamId") val steamId: String,
    /** Epoch millis. */
    @SerialName("expiresAt") val expiresAt: Long = 0L
) {
    /** Null when the server sent a steamId that isn't a 64-bit integer - treated as a failed login. */
    fun toSession(): UserAuthToken? = steamId.toLongOrNull()?.let { id ->
        UserAuthToken(token = token, userId = id, expiresAtEpochMs = expiresAt)
    }
}

class AuthApi(private val httpClient: HttpClient) {

    /**
     * The URL to open in a *browser*, not to fetch. It 302s to Steam, and the
     * eventual token is claimed out-of-band via [pollLogin] using the same [state].
     */
    fun beginLoginUrl(baseUrl: String, state: String): String =
        "$baseUrl/auth/steam/begin?state=$state"

    /**
     * One poll attempt. `null` means "not ready yet, ask again" - the server answers
     * 204 both while the user is still on Steam's page and for a nonce that already
     * got claimed or expired, and the client can't tell those apart (by design).
     */
    suspend fun pollLogin(baseUrl: String, state: String): UserAuthToken? {
        val response = httpClient.get("$baseUrl/auth/steam/poll") {
            parameter("state", state)
        }
        if (response.status != HttpStatusCode.OK) return null
        return response.body<AuthTokenResponse>().toSession()
    }

    /** Best-effort server-side revocation; the local session is cleared regardless. */
    suspend fun logout(baseUrl: String, token: String) {
        httpClient.post("$baseUrl/auth/logout") {
            header("Authorization", "Bearer $token")
        }
    }
}
