package com.raaveinm.pickusall.core.designsystem.keybinding

import androidx.compose.runtime.Immutable
import androidx.compose.ui.input.key.Key

//
// Created by Kirill "Raaveinm" on 9/28/26.
//

/**
 * A key combination: one non-modifier [key] plus the modifiers held when it was pressed.
 *
 * [isPrimary] is the platform's "main" shortcut modifier - Cmd on Apple, Ctrl everywhere else -
 * so one stored chord means the same thing on every platform.
 * Value equality is what makes [KeyMap] lookups work, so keep this a plain data class.
 */
@Immutable
data class KeyChord(
    val key: Key,
    val isPrimary: Boolean = false,
    val isShift: Boolean = false,
    val isAlt: Boolean = false,
    val isControl: Boolean = false
) {
    /** "Ctrl+Shift+R" on Windows/Linux, "⇧⌘R" on Apple platforms. */
    fun display(isApple: Boolean = IS_APPLE): String {
        val keyName: String = key.displayName()
        return if (isApple) {
            buildString {
                if (isControl) append("⌃")
                if (isAlt) append("⌥")
                if (isShift) append("⇧")
                if (isPrimary) append("⌘")
                append(keyName)
            }
        } else {
            listOfNotNull(
                "Ctrl".takeIf { isPrimary || isControl },
                "Alt".takeIf { isAlt },
                "Shift".takeIf { isShift },
                keyName
            ).joinToString("+")
        }
    }
}
