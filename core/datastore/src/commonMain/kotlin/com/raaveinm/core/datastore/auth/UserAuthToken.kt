package com.raaveinm.core.datastore.auth

//
// Created by Kirill "Raaveinm" on 10/1/26.
// Copyright (c) 2026 RetrogradeMercury. All rights reserved.
//

/**
 * A live session as handed over by picassobackend's `/auth/steam/poll`.
 *
 * This is the app's answer to "who am I" - it replaces the single hardcoded
 * `AppConfig.USER_ID` that every Steam call and every outgoing chat message used
 * to be attributed to. Absence of a [UserAuthToken] *is* the logged-out state;
 * there is no separate flag.
 *
 * @sample GET http://domain.or.ip/auth/steam/poll?state=<nonce>
 *   -> {"token":"9f86d08...","steamId":"76561198966516520","expiresAt":1793491200000}
 */
data class UserAuthToken(
    /**
     * Opaque bearer token. The server stores only its SHA-256, so this exact
     * string is the single copy - losing it means re-running the Steam login.
     * Sent as `Authorization: Bearer <token>` on the WS upgrade and to `/auth/logout`.
     */
    val token: String,

    /**
     * The Steam id the server resolved from the OpenID assertion. Trusted for
     * *display and local queries only* - the server never accepts it as a claim
     * of identity over the wire, it re-derives the sender from [token].
     *
     * Arrives as a JSON string (64-bit ids lose precision as JSON numbers) and is
     * parsed to Long here, since every local Room query keys on it as an integer.
     */
    val userId: Long,

    /**
     * Epoch millis. The server enforces this independently; the client checks it
     * only to avoid firing requests it knows will 401. 0 means "unknown", which
     * is treated as not-yet-expired.
     */
    val expiresAtEpochMs: Long = 0L
) {
    fun isValidAt(nowEpochMs: Long): Boolean =
        token.isNotBlank() && (expiresAtEpochMs == 0L || nowEpochMs < expiresAtEpochMs)
}
