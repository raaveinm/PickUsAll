package com.raaveinm.core.datastore.auth

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class AuthDataStoreFactory {
    @OptIn(ExperimentalForeignApi::class)
    actual fun authDataStorePath(): String {
        val documents: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        return requireNotNull(documents?.path) { "no documents directory" } + "/" + AUTH_DATASTORE_FILE
    }
}
