package com.raaveinm.core.datastore.settings

import com.raaveinm.core.datastore.auth.createSettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

//
// Created by Kirill "Raaveinm" on 10/26/26.
//

class ServerSettingsStoreTest {

    private fun tempFile(): File = File.createTempFile("settings", ".preferences_pb").also {
        it.delete()
        it.deleteOnExit()
    }

    @Test
    fun nothingIsSelectedUntilTheUserPicksAServer() = runBlocking {
        val store = ServerSettingsStore(createSettingsDataStore { tempFile().absolutePath })
        assertNull(store.settings.first().selectedServerId)
    }

    @Test
    fun theLastSelectionWins() = runBlocking {
        val store = ServerSettingsStore(createSettingsDataStore { tempFile().absolutePath })
        store.selectServer(3L)
        store.selectServer(7L)
        assertEquals(7L, store.settings.first().selectedServerId)
    }

    @Test
    fun cacheOwnerIsRememberedSeparatelyFromTheSelection() = runBlocking {
        val store = ServerSettingsStore(createSettingsDataStore { tempFile().absolutePath })
        assertNull(store.settings.first().cachedServerId)

        store.selectServer(7L)
        store.markCacheOwner(3L)

        assertEquals(ServerSettingsList(selectedServerId = 7L, cachedServerId = 3L), store.settings.first())
    }
}
