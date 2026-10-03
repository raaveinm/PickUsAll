package com.raaveinm.core.datastore

import com.raaveinm.core.datastore.auth.AuthDataStoreFactory
import com.raaveinm.core.datastore.auth.AuthTokenStore
import com.raaveinm.core.datastore.auth.createAuthDataStore
import com.raaveinm.core.datastore.auth.createSettingsDataStore
import com.raaveinm.core.datastore.settings.BehaviourSettingsStore
import org.koin.core.qualifier.named
import org.koin.dsl.module

//
// Created by Kirill "Raaveinm" on 10/1/26.
//

private val AUTH_STORE = named("auth_datastore")
private val SETTINGS_STORE = named("settings_datastore")

fun authDataStoreModule(factory: AuthDataStoreFactory) = module {
    single(AUTH_STORE) { createAuthDataStore { factory.authDataStorePath() } }
    single { AuthTokenStore(get(AUTH_STORE)) }

    single(SETTINGS_STORE) { createSettingsDataStore { factory.settingsDataStorePath() } }
    single { BehaviourSettingsStore(get(SETTINGS_STORE)) }
}
