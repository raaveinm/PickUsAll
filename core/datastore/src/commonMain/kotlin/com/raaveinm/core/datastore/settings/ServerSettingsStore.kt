package com.raaveinm.core.datastore.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.raaveinm.core.datastore.CACHED_SERVER_ID
import com.raaveinm.core.datastore.SELECTED_SERVER_ID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//
// Created by Kirill "Raaveinm" on 10/26/26.
//

/**
 * Persisted Settings / Server. Same shape as [BehaviourSettingsStore]: [settings] is the single
 * source of truth and re-emits on its own after [selectServer] / [markCacheOwner].
 */
class ServerSettingsStore(private val dataStore: DataStore<Preferences>) {

    val settings: Flow<ServerSettingsList> = dataStore.data.map { prefs ->
        ServerSettingsList(
            selectedServerId = prefs[SELECTED_SERVER_ID],
            cachedServerId = prefs[CACHED_SERVER_ID]
        )
    }

    suspend fun selectServer(serverId: Long) {
        dataStore.edit { prefs -> prefs[SELECTED_SERVER_ID] = serverId }
    }

    /** Records that the cached chat data now belongs to [serverId]; call it only after the old data is gone. */
    suspend fun markCacheOwner(serverId: Long) {
        dataStore.edit { prefs -> prefs[CACHED_SERVER_ID] = serverId }
    }
}
