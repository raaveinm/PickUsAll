package com.raaveinm.pickusall.core.designsystem.keybinding

import androidx.compose.ui.input.key.Key
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

class KeyMapTest {

    private val ctrlShiftR: KeyChord = chord(Key.R, SpecialKeys.PRIMARY, SpecialKeys.SHIFT)

    @Test
    fun defaultsBindRefreshToPrimaryR() {
        val keyMap: KeyMap = KeyMap.defaults()
        assertEquals(chord(Key.R, SpecialKeys.PRIMARY), keyMap.chordFor(Commands.REFRESH))
        assertEquals(listOf(Commands.REFRESH), keyMap.bindingsFor(chord(Key.R, SpecialKeys.PRIMARY)).map { it.command })
    }

    @Test
    fun overrideReplacesChordAndOldChordNoLongerFires() {
        val keyMap: KeyMap = KeyMap.withOverrides(mapOf(Commands.REFRESH to ctrlShiftR))
        assertEquals(ctrlShiftR, keyMap.chordFor(Commands.REFRESH))
        assertEquals(emptyList(), keyMap.bindingsFor(chord(Key.R, SpecialKeys.PRIMARY)))
    }

    @Test
    fun overrideOfReservedChordIsSkippedInsteadOfThrowing() {
        val keyMap: KeyMap = KeyMap.withOverrides(mapOf(Commands.REFRESH to chord(Key.C, SpecialKeys.PRIMARY)))
        assertEquals(KeyMap.defaultChordFor(Commands.REFRESH), keyMap.chordFor(Commands.REFRESH))
    }

    @Test
    fun checkRejectsReservedChord() {
        assertEquals(
            RebindProblem.Reserved,
            KeyMap.defaults().check(Commands.REFRESH, chord(Key.V, SpecialKeys.PRIMARY))
        )
    }

    @Test
    fun checkRejectsBareKeyButAllowsFunctionKey() {
        val keyMap: KeyMap = KeyMap.defaults()
        assertEquals(RebindProblem.NeedsModifier, keyMap.check(Commands.REFRESH, chord(Key.R)))
        assertNull(keyMap.check(Commands.REFRESH, chord(Key.F5)))
    }

    @Test
    fun checkAcceptsChordTheCommandAlreadyOwns() {
        // re-recording the current chord must not report a conflict with itself
        val keyMap: KeyMap = KeyMap.defaults()
        assertNull(keyMap.check(Commands.REFRESH, chord(Key.R, SpecialKeys.PRIMARY)))
    }

    @Test
    fun constructorStillRejectsInconsistentHardcodedList() {
        assertFailsWith<IllegalArgumentException> {
            KeyMap(listOf(Binding(chord(Key.C, SpecialKeys.PRIMARY), Commands.REFRESH)))
        }
    }

    @Test
    fun displayFormatsPerPlatform() {
        assertEquals("Ctrl+Shift+R", ctrlShiftR.display(isApple = false))
        assertEquals("⇧⌘R", ctrlShiftR.display(isApple = true))
        assertEquals("F5", chord(Key.F5).display(isApple = false))
    }
}
