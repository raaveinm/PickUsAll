package com.raaveinm.pickusall.core.designsystem.keybinding

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

//
// Created by Kirill "Raaveinm" on 9/28/26.
//

fun chord(key: Key, vararg spec: SpecialKeys) = KeyChord(
    key = key,
    isPrimary = SpecialKeys.PRIMARY in spec,
    isShift = SpecialKeys.SHIFT in spec,
    isAlt = SpecialKeys.ALTERNATIVE in spec,
    isControl = SpecialKeys.CONTROL in spec
)

/**
 * Turns a raw key event into the chord it represents, or `null` when it can't be one:
 * key releases, or a modifier pressed on its own.
 *
 * Every modifier is reported verbatim and identically on every platform - Meta becomes
 * [KeyChord.isPrimary] (Cmd on Apple, Super/Windows key elsewhere) and Ctrl becomes
 * [KeyChord.isControl]. Nothing is folded or dropped, so Ctrl+Q and Super+Q are two different
 * bindable chords.
 *
 * The flip side is that a single chord no longer means "the OS shortcut modifier" on both
 * Apple and non-Apple platforms, so hardcoded lists have to pick per platform - see
 * [SYSTEM_MODIFIER].
 */
fun KeyEvent.toChordOrNull() : KeyChord? {
    if (type != KeyEventType.KeyDown) return null       // Ignoring KeyUp so a shortcut never double click
    if (key in MODIFIER_KEYS) return null               // "Ctrl" alone is not a shortcut

    return KeyChord(
        key = key,
        isPrimary = isMetaPressed,
        isAlt = isAltPressed,
        isShift = isShiftPressed,
        isControl = isCtrlPressed
    )
}

private val MODIFIER_KEYS: Set<Key> = setOf(
    Key.CtrlLeft, Key.CtrlRight,
    Key.ShiftLeft, Key.ShiftRight,
    Key.AltLeft, Key.AltRight,
    Key.MetaLeft, Key.MetaRight,
    Key.CapsLock
)


///////////////////////////////////////////////
// Key names
///////////////////////////////////////////////

private val KEY_NAMES: Map<Key, String> = buildMap {
    listOf(
        Key.A, Key.B, Key.C, Key.D, Key.E, Key.F, Key.G, Key.H, Key.I, Key.J, Key.K, Key.L, Key.M,
        Key.N, Key.O, Key.P, Key.Q, Key.R, Key.S, Key.T, Key.U, Key.V, Key.W, Key.X, Key.Y, Key.Z
    ).forEachIndexed { index: Int, key: Key -> put(key, ('A' + index).toString()) }

    listOf(
        Key.Zero, Key.One, Key.Two, Key.Three, Key.Four,
        Key.Five, Key.Six, Key.Seven, Key.Eight, Key.Nine
    ).forEachIndexed { index: Int, key: Key -> put(key, index.toString()) }

    listOf(
        Key.F1, Key.F2, Key.F3, Key.F4, Key.F5, Key.F6,
        Key.F7, Key.F8, Key.F9, Key.F10, Key.F11, Key.F12
    ).forEachIndexed { index: Int, key: Key -> put(key, "F${index + 1}") }

    put(Key.Escape, "Esc")
    put(Key.Enter, "Enter")
    put(Key.Spacebar, "Space")
    put(Key.Tab, "Tab")
    put(Key.Backspace, "Backspace")
    put(Key.Delete, "Delete")
    put(Key.Insert, "Insert")
    put(Key.MoveHome, "Home")
    put(Key.MoveEnd, "End")
    put(Key.PageUp, "PgUp")
    put(Key.PageDown, "PgDn")
    put(Key.DirectionUp, "↑")
    put(Key.DirectionDown, "↓")
    put(Key.DirectionLeft, "←")
    put(Key.DirectionRight, "→")
    put(Key.Comma, ",")
    put(Key.Period, ".")
    put(Key.Minus, "-")
    put(Key.Equals, "=")
    put(Key.Slash, "/")
    put(Key.Backslash, "\\")
    put(Key.Semicolon, ";")
    put(Key.Apostrophe, "'")
    put(Key.LeftBracket, "[")
    put(Key.RightBracket, "]")
    put(Key.Grave, "`")
}

internal val FUNCTION_KEYS: Set<Key> = setOf(
    Key.F1, Key.F2, Key.F3, Key.F4, Key.F5, Key.F6,
    Key.F7, Key.F8, Key.F9, Key.F10, Key.F11, Key.F12
)

/** Human readable name of a physical key. Keys missing from the table fall back to [Key.toString]. */
fun Key.displayName(): String = KEY_NAMES[this] ?: toString()


///////////////////////////////////////////////
// Key Mapping
///////////////////////////////////////////////

val SYSTEM_MODIFIER: SpecialKeys = if (IS_APPLE) SpecialKeys.PRIMARY else SpecialKeys.CONTROL

val DEFAULT_BINDINGS: List<Binding> = listOf(
    Binding(chord(Key.R, SYSTEM_MODIFIER), Commands.REFRESH),               // Ctrl+R  / ⌘R
    Binding(chord(Key.Q, SYSTEM_MODIFIER), Commands.QUIT_APPLICATION),      // Ctrl+Q  / ⌘Q
    Binding(chord(Key.W, SYSTEM_MODIFIER), Commands.MINIMIZE_APPLICATION)   // Ctrl+W  / ⌘W
)
