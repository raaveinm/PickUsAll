package com.raaveinm.core.datastore

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

//
// Created by Kirill "Raaveinm" on 10/1/26.
//

///////////////////////////////////////////////
/// Auth
///////////////////////////////////////////////

internal val AUTH_TOKEN = stringPreferencesKey("auth_token")
internal val AUTH_USER_ID = longPreferencesKey("auth_user_id")
internal val AUTH_EXPIRES_AT = longPreferencesKey("auth_expires_at")

///////////////////////////////////////////////
/// Behaviour
///////////////////////////////////////////////

internal const val KEY_BINDING_PREFIX: String = "key_binding_"
