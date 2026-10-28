package com.raaveinm.core.datastore.settings

//
// Created by Kirill "Raaveinm" on 10/28/2026.
//

data class VisualSettingsList(
    val themeMode: ThemeMode = ThemeMode.AUTO,      // auto - device's light/dark, otherwise forced light or dark
    val isDynamicColor: Boolean = true,             // if enabled - dynamic colors else static colors
    val isSteamPFProfileBackground: Boolean = false // if enabled - uses steam profile background as app background
)
