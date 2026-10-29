package com.raaveinm.core.datastore.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.raaveinm.core.datastore.KEY_BINDING_PREFIX
import com.raaveinm.core.datastore.ON_CLOSE_REQUEST
import com.raaveinm.core.model.preferences.OnCloseAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

/**
 * Persisted Settings / Behaviour. [settings] is the single source of truth: observers re-emit on
 * their own after [saveKeyBinding] / [resetKeyBinding], nothing returns the new value directly.
 */
class BehaviourSettingsStore(private val dataStore: DataStore<Preferences>) {

    val chordSettings: Flow<BehaviourSettings> = dataStore.data.map { it.toSettings() }
    val settings: Flow<BehaviourSettingsList> = dataStore.data.map { prefs ->
        BehaviourSettingsList(
            onCloseRequest = OnCloseAction.entries[prefs[ON_CLOSE_REQUEST]?:0]
        )
    }

    suspend fun setOnCloseRequestAction(request: OnCloseAction) {
        dataStore.edit { prefs -> prefs[ON_CLOSE_REQUEST] = request.ordinal }
    }

    ///////////////////////////////////////////////
    // Key Bindings
    ///////////////////////////////////////////////

    suspend fun saveKeyBinding(commandId: String, chord: StoredChord) {
        dataStore.edit { prefs -> prefs[keyBindingKey(commandId)] = chord.encode() }
    }

    suspend fun resetKeyBinding(commandId: String) {
        dataStore.edit { prefs -> prefs.remove(keyBindingKey(commandId)) }
    }

    private fun keyBindingKey(commandId: String) = stringPreferencesKey(KEY_BINDING_PREFIX + commandId)

    private fun Preferences.toSettings(): BehaviourSettings {
        val bindings: Map<String, StoredChord> = buildMap {
            asMap().forEach { (key, value) ->
                if (!key.name.startsWith(KEY_BINDING_PREFIX) || value !is String) return@forEach
                val chord: StoredChord = value.decodeStoredChord() ?: return@forEach // damaged entry == no override
                put(key.name.removePrefix(KEY_BINDING_PREFIX), chord)
            }
        }
        return BehaviourSettings(keyBindings = bindings)
    }
}
