package com.raaveinm.core.datastore.auth

import java.io.File

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class AuthDataStoreFactory {
    actual fun authDataStorePath(): String = pathOf(AUTH_DATASTORE_FILE)

    actual fun settingsDataStorePath(): String = pathOf(SETTINGS_DATASTORE_FILE)

    private fun pathOf(fileName: String): String {
        val dir = File(System.getProperty("user.home"), ".pickusall")
        dir.mkdirs()
        return File(dir, fileName).absolutePath
    }
}
