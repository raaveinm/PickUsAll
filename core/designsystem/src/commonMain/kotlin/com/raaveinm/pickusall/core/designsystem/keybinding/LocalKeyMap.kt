package com.raaveinm.pickusall.core.designsystem.keybinding

import androidx.compose.runtime.staticCompositionLocalOf

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

/**
 * The keymap currently in effect. The app provides the user's persisted one near the root;
 * without a provider (previews, tests) it falls back to the factory defaults.
 *
 * Static because it changes rarely (only on a rebind), and then everything reading it should re-run anyway.
 */
val LocalKeyMap = staticCompositionLocalOf { KeyMap.defaults() }
