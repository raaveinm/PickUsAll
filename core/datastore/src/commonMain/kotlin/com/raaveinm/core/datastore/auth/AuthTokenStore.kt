package com.raaveinm.core.datastore.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.raaveinm.core.datastore.LEGACY_AUTH_EXPIRES_AT
import com.raaveinm.core.datastore.LEGACY_AUTH_TOKEN
import com.raaveinm.core.datastore.LEGACY_AUTH_USER_ID
import com.raaveinm.core.datastore.authExpiresAtKey
import com.raaveinm.core.datastore.authTokenKey
import com.raaveinm.core.datastore.authUserIdKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

//
// Created by Kirill "Raaveinm" on 10/1/26.
//

/**
 * The persisted sessions, one per server (`Servers.id`): a token is only ever valid on the
 * server that issued it, so it is stored - and looked up - together with that server.
 * [sessionFor] is the single source of truth for "who is logged in *there*"; callers pick the
 * server (see `AuthRepository.session` for the selected one) and observe it, so a login or
 * logout propagates on its own.
 *
 * A null emission means logged out on that server. That is the only logged-out signal; there
 * is no separate boolean to keep in sync with it.
 */
class AuthTokenStore(private val dataStore: DataStore<Preferences>) {

    fun sessionFor(serverId: Long): Flow<UserAuthToken?> =
        dataStore.data.map { it.toSession(serverId) }.distinctUntilChanged()

    suspend fun save(serverId: Long, session: UserAuthToken) {
        dataStore.edit { prefs ->
            prefs.removeLegacySession()
            prefs[authTokenKey(serverId)] = session.token
            prefs[authUserIdKey(serverId)] = session.userId
            prefs[authExpiresAtKey(serverId)] = session.expiresAtEpochMs
        }
    }

    suspend fun clear(serverId: Long) {
        dataStore.edit { prefs ->
            prefs.removeLegacySession()
            prefs.removeSession(serverId)
        }
    }

    /** Clears the session only while it still holds [token] - one edit, so a login in between survives. */
    suspend fun clearIfToken(serverId: Long, token: String) {
        dataStore.edit { prefs ->
            if (prefs[authTokenKey(serverId)] == token) prefs.removeSession(serverId)
        }
    }

    /** One-shot read for callers that can't observe (e.g. building a request header). */
    suspend fun current(serverId: Long): UserAuthToken? = sessionFor(serverId).first()

    private fun MutablePreferences.removeSession(serverId: Long) {
        remove(authTokenKey(serverId))
        remove(authUserIdKey(serverId))
        remove(authExpiresAtKey(serverId))
    }

    private fun MutablePreferences.removeLegacySession() {
        remove(LEGACY_AUTH_TOKEN)
        remove(LEGACY_AUTH_USER_ID)
        remove(LEGACY_AUTH_EXPIRES_AT)
    }

    private fun Preferences.toSession(serverId: Long): UserAuthToken? {
        val token = this[authTokenKey(serverId)] ?: return null
        val userId = this[authUserIdKey(serverId)] ?: return null
        // A half-written session is treated as no session rather than as a broken one.
        if (token.isBlank() || userId == 0L) return null
        return UserAuthToken(
            token = token,
            userId = userId,
            expiresAtEpochMs = this[authExpiresAtKey(serverId)] ?: 0L
        )
    }
}
