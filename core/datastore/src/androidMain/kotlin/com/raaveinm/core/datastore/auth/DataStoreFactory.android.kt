package com.raaveinm.core.datastore.auth

import android.content.Context

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class AuthDataStoreFactory(private val context: Context) {
    actual fun authDataStorePath(): String =
        context.filesDir.resolve(AUTH_DATASTORE_FILE).absolutePath

    actual fun settingsDataStorePath(): String =
        context.filesDir.resolve(SETTINGS_DATASTORE_FILE).absolutePath
}
