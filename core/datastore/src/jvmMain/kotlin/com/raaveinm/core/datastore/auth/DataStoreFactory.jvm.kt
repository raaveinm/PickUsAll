package com.raaveinm.core.datastore.auth

import java.io.File

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class AuthDataStoreFactory {
    actual fun authDataStorePath(): String {
        val dir = File(System.getProperty("user.home"), ".pickusall")
        dir.mkdirs()
        return File(dir, AUTH_DATASTORE_FILE).absolutePath
    }
}
