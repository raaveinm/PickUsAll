package com.raaveinm.core.datastore.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.raaveinm.core.datastore.AUTH_EXPIRES_AT
import com.raaveinm.core.datastore.AUTH_TOKEN
import com.raaveinm.core.datastore.AUTH_USER_ID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

//
// Created by Kirill "Raaveinm" on 10/1/26.
//

/**
 * The persisted session. [session] is the single source of truth for "who is
 * logged in" across the whole app - viewmodels observe it rather than reading a
 * build-time constant, so a login or logout propagates everywhere on its own.
 *
 * A null emission means logged out. That is the only logged-out signal; there is
 * no separate boolean to keep in sync with it.
 */
class AuthTokenStore(private val dataStore: DataStore<Preferences>) {

    val session: Flow<UserAuthToken?> = dataStore.data.map { it.toSession() }

    suspend fun save(session: UserAuthToken) {
        dataStore.edit { prefs ->
            prefs[AUTH_TOKEN] = session.token
            prefs[AUTH_USER_ID] = session.userId
            prefs[AUTH_EXPIRES_AT] = session.expiresAtEpochMs
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(AUTH_TOKEN)
            prefs.remove(AUTH_USER_ID)
            prefs.remove(AUTH_EXPIRES_AT)
        }
    }

    /** One-shot read for callers that can't observe (e.g. building a request header). */
    suspend fun current(): UserAuthToken? = session.first()

    private fun Preferences.toSession(): UserAuthToken? {
        val token = this[AUTH_TOKEN] ?: return null
        val userId = this[AUTH_USER_ID] ?: return null
        // A half-written session is treated as no session rather than as a broken one.
        if (token.isBlank() || userId == 0L) return null
        return UserAuthToken(
            token = token,
            userId = userId,
            expiresAtEpochMs = this[AUTH_EXPIRES_AT] ?: 0L
        )
    }
}
