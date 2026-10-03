package com.raaveinm.core.datastore

import com.raaveinm.core.datastore.auth.AuthDataStoreFactory
import com.raaveinm.core.datastore.auth.AuthTokenStore
import com.raaveinm.core.datastore.auth.UserAuthToken
import com.raaveinm.core.datastore.settings.BehaviourSettingsStore
import com.raaveinm.core.datastore.settings.StoredChord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.dsl.koinApplication
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

class DataStoreModuleTest {

    @Test
    fun authAndSettingsStoresResolveToSeparateFiles() = runBlocking {
        val fakeHome: File = Files.createTempDirectory("pickusall-home").toFile().also { it.deleteOnExit() }
        val realHome: String? = System.getProperty("user.home")
        System.setProperty("user.home", fakeHome.absolutePath)
        try {
            val koin = koinApplication { modules(authDataStoreModule(AuthDataStoreFactory())) }.koin
            val auth: AuthTokenStore = koin.get()
            val behaviour: BehaviourSettingsStore = koin.get()

            behaviour.saveKeyBinding("REFRESH", StoredChord(7L, isPrimary = true, isShift = false, isAlt = false, isControl = false))
            auth.save(UserAuthToken(token = "t", userId = 1L, expiresAtEpochMs = 0L))

            val dir = File(fakeHome, ".pickusall")
            assertTrue(File(dir, "settings.preferences_pb").exists(), "settings file")
            assertTrue(File(dir, "auth.preferences_pb").exists(), "auth file")

            auth.clear()
            assertNull(auth.current())
            assertNotNull(behaviour.settings.first().keyBindings["REFRESH"])
            assertEquals(1, behaviour.settings.first().keyBindings.size)
        } finally {
            System.setProperty("user.home", realHome)
        }
    }
}
