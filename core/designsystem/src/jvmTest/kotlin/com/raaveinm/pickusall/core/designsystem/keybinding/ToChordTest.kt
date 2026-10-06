package com.raaveinm.pickusall.core.designsystem.keybinding

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

@OptIn(InternalComposeUiApi::class)
class ToChordTest {

    private fun event(
        key: Key,
        type: KeyEventType = KeyEventType.KeyDown,
        ctrl: Boolean = false,
        meta: Boolean = false,
        alt: Boolean = false,
        shift: Boolean = false
    ): KeyEvent = KeyEvent(
        key = key,
        type = type,
        isCtrlPressed = ctrl,
        isMetaPressed = meta,
        isAltPressed = alt,
        isShiftPressed = shift
    )

    private fun systemModifierEvent(key: Key): KeyEvent =
        if (IS_APPLE) event(key, meta = true) else event(key, ctrl = true)

    @Test
    fun ctrlAndMetaAreDistinctModifiers() {
        assertEquals(chord(Key.R, SpecialKeys.CONTROL), event(Key.R, ctrl = true).toChordOrNull())
        assertEquals(chord(Key.R, SpecialKeys.PRIMARY), event(Key.R, meta = true).toChordOrNull())
    }

    @Test
    fun systemModifierMatchesDefaultChordOfEveryCommand() {
        Commands.entries.forEach { command: Commands ->
            val key: Key = KeyMap.defaultChordFor(command)!!.key
            assertEquals(
                KeyMap.defaultChordFor(command),
                systemModifierEvent(key).toChordOrNull(),
                "default binding for $command is unreachable from a real key event"
            )
        }
    }

    @Test
    fun modifierPressedAloneIsNotAChord() {
        assertNull(event(Key.CtrlLeft, ctrl = true).toChordOrNull())
        assertNull(event(Key.ShiftLeft, shift = true).toChordOrNull())
    }

    @Test
    fun keyReleaseIsNotAChord() {
        assertNull(event(Key.R, type = KeyEventType.KeyUp, ctrl = true).toChordOrNull())
    }

    @Test
    fun otherModifiersAreCarriedOver() {
        val chord: KeyChord? = event(Key.R, ctrl = true, alt = true, shift = true).toChordOrNull()
        assertEquals(chord(Key.R, SpecialKeys.CONTROL, SpecialKeys.ALTERNATIVE, SpecialKeys.SHIFT), chord)
    }

    @Test
    fun recordedChordRoundTripsThroughKeyMap() {
        val recorded: KeyChord = event(Key.F5).toChordOrNull()!!
        val keyMap: KeyMap = KeyMap.withOverrides(mapOf(Commands.REFRESH to recorded))
        val pressedLater: KeyChord = event(Key.F5).toChordOrNull()!!
        assertEquals(listOf(Commands.REFRESH), keyMap.bindingsFor(pressedLater).map { it.command })
    }
}
