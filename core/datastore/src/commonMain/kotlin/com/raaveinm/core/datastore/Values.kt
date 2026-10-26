package com.raaveinm.core.datastore

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

//
// Created by Kirill "Raaveinm" on 10/1/26.
//

///////////////////////////////////////////////
/// Auth
///////////////////////////////////////////////

internal fun authTokenKey(serverId: Long) = stringPreferencesKey("auth_token_$serverId")
internal fun authUserIdKey(serverId: Long) = longPreferencesKey("auth_user_id_$serverId")
internal fun authExpiresAtKey(serverId: Long) = longPreferencesKey("auth_expires_at_$serverId")

internal val LEGACY_AUTH_TOKEN = stringPreferencesKey("auth_token")
internal val LEGACY_AUTH_USER_ID = longPreferencesKey("auth_user_id")
internal val LEGACY_AUTH_EXPIRES_AT = longPreferencesKey("auth_expires_at")

///////////////////////////////////////////////
/// Behaviour
///////////////////////////////////////////////

internal const val KEY_BINDING_PREFIX: String = "key_binding_"

///////////////////////////////////////////////
/// Server
///////////////////////////////////////////////

internal val SELECTED_SERVER_ID = longPreferencesKey("selected_server_id")
internal val CACHED_SERVER_ID = longPreferencesKey("cached_server_id")
