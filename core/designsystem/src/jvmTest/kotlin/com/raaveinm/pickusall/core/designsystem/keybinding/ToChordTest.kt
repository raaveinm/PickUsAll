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

    @Test
    fun ctrlRMatchesDefaultRefreshChordOffApple() {
        val chord: KeyChord? = event(Key.R, ctrl = true).toChordOrNull(isApple = false)
        assertEquals(KeyMap.defaultChordFor(Commands.REFRESH), chord)
    }

    @Test
    fun cmdRMatchesDefaultRefreshChordOnApple() {
        val chord: KeyChord? = event(Key.R, meta = true).toChordOrNull(isApple = true)
        assertEquals(KeyMap.defaultChordFor(Commands.REFRESH), chord)
    }

    @Test
    fun foreignModifierIsIgnored() {
        assertNull(event(Key.R, meta = true).toChordOrNull(isApple = false))
        assertNull(event(Key.R, ctrl = true).toChordOrNull(isApple = true))
    }

    @Test
    fun modifierPressedAloneIsNotAChord() {
        assertNull(event(Key.CtrlLeft, ctrl = true).toChordOrNull(isApple = false))
        assertNull(event(Key.ShiftLeft, shift = true).toChordOrNull(isApple = false))
    }

    @Test
    fun keyReleaseIsNotAChord() {
        assertNull(event(Key.R, type = KeyEventType.KeyUp, ctrl = true).toChordOrNull(isApple = false))
    }

    @Test
    fun otherModifiersAreCarriedOver() {
        val chord: KeyChord? = event(Key.R, ctrl = true, alt = true, shift = true).toChordOrNull(isApple = false)
        assertEquals(chord(Key.R, SpecialKeys.PRIMARY, SpecialKeys.ALTERNATIVE, SpecialKeys.SHIFT), chord)
    }

    @Test
    fun recordedChordRoundTripsThroughKeyMap() {
        val recorded: KeyChord = event(Key.F5).toChordOrNull(isApple = false)!!
        val keyMap: KeyMap = KeyMap.withOverrides(mapOf(Commands.REFRESH to recorded))
        val pressedLater: KeyChord = event(Key.F5).toChordOrNull(isApple = false)!!
        assertEquals(listOf(Commands.REFRESH), keyMap.bindingsFor(pressedLater).map { it.command })
    }
}
