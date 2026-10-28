package com.raaveinm.core.datastore.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.raaveinm.core.datastore.DYNAMIC_COLOR
import com.raaveinm.core.datastore.STEAM_PROFILE_BACKGROUND
import com.raaveinm.core.datastore.THEME_MODE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//
// Created by Kirill "Raaveinm" on 10/28/2026.
//

class VisualSettingsStore(private val dataStore: DataStore<Preferences>) {
    val settings: Flow<VisualSettingsList> = dataStore.data.map { prefs ->
        VisualSettingsList(
            // a missing or unknown value (e.g. a removed constant) falls back to following the device
            themeMode = ThemeMode.entries.firstOrNull { it.name == prefs[THEME_MODE] } ?: ThemeMode.AUTO,
            isDynamicColor = prefs[DYNAMIC_COLOR] ?: true,
            isSteamPFProfileBackground = prefs[STEAM_PROFILE_BACKGROUND] ?: false
        )
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences -> preferences[THEME_MODE] = themeMode.name }
    }

    suspend fun setDynamicColor(isDynamicColor: Boolean) {
        dataStore.edit { preferences -> preferences[DYNAMIC_COLOR] = isDynamicColor }
    }

    suspend fun setSteamPFProfileBackgroundMode(isSteamPFProfileBackground: Boolean) {
        dataStore.edit { preferences -> preferences[STEAM_PROFILE_BACKGROUND] = isSteamPFProfileBackground }
    }
}