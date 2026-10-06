package com.raaveinm.picasso.ui.app

import androidx.compose.runtime.Immutable

//
// Created by Kirill "Raaveinm" on 10/6/26.
//

/**
 * Host-window operations [com.raaveinm.picasso.App] can *trigger* but cannot *implement* - only
 * the platform entry point owns the window and the application lifecycle. Desktop fills these in
 * from `ApplicationScope.exitApplication` and `WindowState.isMinimized`; the tray menu drives the
 * same two actions.
 *
 * [onMinimize] iconifies the window - it stays in the taskbar and the OS owns restoring it. That
 * is deliberately *not* the same as hiding it (`Window(visible = false)`), which would drop the
 * window off the taskbar and leave the tray icon as the only way back.
 *
 * A `null` member means "this platform has no such concept" rather than "do nothing", and the
 * hotkey handler relies on that distinction: an unsupported command leaves the key event
 * unconsumed instead of swallowing it. Android and iOS pass [Unsupported], where the OS owns the
 * window and there is no Ctrl+Q to begin with.
 */
@Immutable
data class WindowActions(
    val onQuit: (() -> Unit)? = null,       // terminate the app (desktop: exitApplication)
    val onMinimize: (() -> Unit)? = null    // iconify the window (desktop: WindowState.isMinimized)
) {
    companion object {
        val Unsupported: WindowActions = WindowActions()
    }
}
