package com.raaveinm.picasso.ui.app

import androidx.compose.runtime.Immutable

//
// Created by Kirill "Raaveinm" on 10/6/26.
//

/**
 * Host-window operations [com.raaveinm.picasso.App] can *trigger* but cannot *implement* - only
 * the platform entry point owns the window and the application lifecycle. Desktop fills these in
 * from `ApplicationScope.exitApplication` and the `Window(visible = ...)` flag; the tray menu
 * drives the same two actions.
 *
 * A `null` member means "this platform has no such concept" rather than "do nothing", and the
 * hotkey handler relies on that distinction: an unsupported command leaves the key event
 * unconsumed instead of swallowing it. Android and iOS pass [Unsupported], where the OS owns the
 * window and there is no Ctrl+Q to begin with.
 */
@Immutable
data class WindowActions(
    val onQuit: (() -> Unit)? = null,                   // terminate the app (desktop: exitApplication)
    val onToggleVisibility: (() -> Unit)? = null        // hide/show the window, leaving the tray icon alive
) {
    companion object {
        val Unsupported: WindowActions = WindowActions()
    }
}
