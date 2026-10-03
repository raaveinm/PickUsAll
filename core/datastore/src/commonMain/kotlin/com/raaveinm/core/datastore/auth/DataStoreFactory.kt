package com.raaveinm.core.datastore.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

internal const val AUTH_DATASTORE_FILE = "auth.preferences_pb"
internal const val SETTINGS_DATASTORE_FILE = "settings.preferences_pb"

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class AuthDataStoreFactory {
    fun authDataStorePath(): String
    fun settingsDataStorePath(): String
}

fun createAuthDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

fun createSettingsDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )
