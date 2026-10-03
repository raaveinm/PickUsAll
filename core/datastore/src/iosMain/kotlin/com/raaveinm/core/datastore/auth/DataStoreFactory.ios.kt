package com.raaveinm.core.datastore.auth

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class AuthDataStoreFactory {
    actual fun authDataStorePath(): String = pathOf(AUTH_DATASTORE_FILE)

    actual fun settingsDataStorePath(): String = pathOf(SETTINGS_DATASTORE_FILE)

    @OptIn(ExperimentalForeignApi::class)
    private fun pathOf(fileName: String): String {
        val documents: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        return requireNotNull(documents?.path) { "no documents directory" } + "/" + fileName
    }
}
