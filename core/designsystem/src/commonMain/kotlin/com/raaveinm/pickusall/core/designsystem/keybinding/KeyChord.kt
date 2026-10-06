package com.raaveinm.pickusall.core.designsystem.keybinding

import androidx.compose.runtime.Immutable
import androidx.compose.ui.input.key.Key

//
// Created by Kirill "Raaveinm" on 9/28/26.
//

/**
 * A key combination: one non-modifier [key] plus the modifiers held when it was pressed.
 *
 * [isPrimary] is the Meta key - Cmd on Apple, Super/Windows key elsewhere - and [isControl] is
 * the physical Ctrl key. Both are recorded exactly as pressed on every platform, so Ctrl+Q and
 * Super+Q are distinct chords a user can bind separately. Which of the two the host OS treats as
 * *its* shortcut modifier is a separate question, answered by [SYSTEM_MODIFIER].
 *
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
    /** Whether any modifier is held. A chord without one is only bindable if [key] is F1-F12. */
    val hasModifier: Boolean get() = isPrimary || isShift || isAlt || isControl

    /**
     * Whether this chord is safe to fire while a text field has focus - i.e. it can't be
     * mistaken for typing. Backs [Binding.allowWhileTyping].
     */
    val hasShortcutModifier: Boolean get() = isPrimary || isControl

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
                "Super".takeIf { isPrimary },
                "Ctrl".takeIf { isControl },
                "Alt".takeIf { isAlt },
                "Shift".takeIf { isShift },
                keyName
            ).joinToString("+")
        }
    }
}
