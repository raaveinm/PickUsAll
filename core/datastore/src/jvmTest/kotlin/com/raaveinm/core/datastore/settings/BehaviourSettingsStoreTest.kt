package com.raaveinm.core.datastore.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.raaveinm.core.datastore.KEY_BINDING_PREFIX
import com.raaveinm.core.datastore.auth.createSettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

/** Runs against a real DataStore file, since "survives a restart" is the whole point of the store. */
class BehaviourSettingsStoreTest {

    private val chord: StoredChord = StoredChord(keyCode = 42L, isPrimary = true, isShift = true, isAlt = false, isControl = false)

    private fun tempFile(): File = File.createTempFile("settings", ".preferences_pb").also {
        it.delete()
        it.deleteOnExit()
    }

    @Test
    fun savedBindingSurvivesANewStoreInstanceOnTheSameFile() = runBlocking {
        val file: File = tempFile()

        val firstLaunch: Job = SupervisorJob()
        val first: DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
            scope = CoroutineScope(Dispatchers.IO + firstLaunch),
            produceFile = { file.absolutePath.toPath() }
        )
        BehaviourSettingsStore(first).saveKeyBinding("REFRESH", chord)
        firstLaunch.cancelAndJoin()

        val reopened: BehaviourSettings = BehaviourSettingsStore(createSettingsDataStore { file.absolutePath }).settings.first()
        assertEquals(mapOf("REFRESH" to chord), reopened.keyBindings)
    }

    @Test
    fun resetRemovesTheOverride() = runBlocking {
        val store = BehaviourSettingsStore(createSettingsDataStore { tempFile().absolutePath })
        store.saveKeyBinding("REFRESH", chord)
        store.resetKeyBinding("REFRESH")
        assertEquals(emptyMap(), store.settings.first().keyBindings)
    }

    @Test
    fun damagedEntryIsIgnoredWhileGoodOnesSurvive() = runBlocking {
        val dataStore = createSettingsDataStore { tempFile().absolutePath }
        val store = BehaviourSettingsStore(dataStore)
        store.saveKeyBinding("REFRESH", chord)
        dataStore.edit { it[stringPreferencesKey(KEY_BINDING_PREFIX + "BROKEN")] = "not,a,chord" }

        assertEquals(mapOf("REFRESH" to chord), store.settings.first().keyBindings)
    }
}
