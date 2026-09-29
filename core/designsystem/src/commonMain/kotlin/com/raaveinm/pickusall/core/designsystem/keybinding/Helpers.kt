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

fun KeyEvent.toChordOrNull(isApple: Boolean = false) : KeyChord? {
    if (type != KeyEventType.KeyDown) return null       // Ignoring KeyUp so a shortcut never double click
    if (isApple && isCtrlPressed) return null           // Ignoring ctrl on MacOS
    if (!isApple && isMetaPressed) return null          // ignoring Meta on other platforms
    return KeyChord(
        key = key,
        isPrimary = isMetaPressed,
        isAlt = isAltPressed,
        isShift = isShiftPressed,
        isControl = isCtrlPressed
    )
}

private val keyLabels: Map<Key, String> = mapOf(
    Key.A to "A", Key.B to "B", Key.C to "C", Key.D to "D",
    Key.R to "R"
)

private fun keyLabel(key: Key): String = keyLabels[key] ?: key.toString()


///////////////////////////////////////////////
// Key Mapping
///////////////////////////////////////////////

val AppKeyMap = KeyMap(
    listOf(
        Binding(chord(Key.R, SpecialKeys.PRIMARY), Commands.REFRESH)
    )
)