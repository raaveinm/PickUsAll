package com.raaveinm.pickusall.core.designsystem.keybinding

import androidx.compose.ui.input.key.Key
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

class KeyMapTest {

    private val superShiftR: KeyChord = chord(Key.R, SpecialKeys.PRIMARY, SpecialKeys.SHIFT)

    @Test
    fun defaultsBindRefreshToSystemModifierR() {
        val keyMap: KeyMap = KeyMap.defaults()
        assertEquals(chord(Key.R, SYSTEM_MODIFIER), keyMap.chordFor(Commands.REFRESH))
        assertEquals(
            listOf(Commands.REFRESH),
            keyMap.bindingsFor(chord(Key.R, SYSTEM_MODIFIER)).map { it.command }
        )
    }

    @Test
    fun quitAndMinimizeHaveDefaultsDespiteNeighbouringReservedChords() {
        val keyMap: KeyMap = KeyMap.defaults()
        assertEquals(chord(Key.Q, SYSTEM_MODIFIER), keyMap.chordFor(Commands.QUIT_APPLICATION))
        assertEquals(chord(Key.W, SYSTEM_MODIFIER), keyMap.chordFor(Commands.MINIMIZE_APPLICATION))
    }

    @Test
    fun overrideReplacesChordAndOldChordNoLongerFires() {
        val keyMap: KeyMap = KeyMap.withOverrides(mapOf(Commands.REFRESH to superShiftR))
        assertEquals(superShiftR, keyMap.chordFor(Commands.REFRESH))
        assertEquals(emptyList(), keyMap.bindingsFor(chord(Key.R, SYSTEM_MODIFIER)))
    }

    @Test
    fun overrideOfReservedChordIsSkippedInsteadOfThrowing() {
        val keyMap: KeyMap = KeyMap.withOverrides(mapOf(Commands.REFRESH to chord(Key.C, SYSTEM_MODIFIER)))
        assertEquals(KeyMap.defaultChordFor(Commands.REFRESH), keyMap.chordFor(Commands.REFRESH))
    }

    @Test
    fun checkRejectsReservedChord() {
        assertEquals(
            RebindProblem.Reserved,
            KeyMap.defaults().check(Commands.REFRESH, chord(Key.V, SYSTEM_MODIFIER))
        )
    }

    @Test
    fun checkReportsCommandThatAlreadyOwnsTheChord() {
        assertEquals(
            RebindProblem.Taken(Commands.QUIT_APPLICATION),
            KeyMap.defaults().check(Commands.REFRESH, chord(Key.Q, SYSTEM_MODIFIER))
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
        assertNull(keyMap.check(Commands.REFRESH, chord(Key.R, SYSTEM_MODIFIER)))
    }

    @Test
    fun constructorStillRejectsInconsistentHardcodedList() {
        assertFailsWith<IllegalArgumentException> {
            KeyMap(listOf(Binding(chord(Key.C, SYSTEM_MODIFIER), Commands.REFRESH)))
        }
    }

    @Test
    fun displayDistinguishesControlFromPrimary() {
        assertEquals("Super+Shift+R", superShiftR.display(isApple = false))
        assertEquals("⇧⌘R", superShiftR.display(isApple = true))
        assertEquals(
            "Ctrl+Shift+R",
            chord(Key.R, SpecialKeys.CONTROL, SpecialKeys.SHIFT).display(isApple = false)
        )
        assertEquals("F5", chord(Key.F5).display(isApple = false))
    }

    @Test
    fun aChordWithOnlyAControlModifierStillFiresWhileTyping() {
        // Ctrl+R is the off-Apple default; it must not be treated as plain typing just because
        // isPrimary is now reserved for Meta.
        val binding: Binding = KeyMap.defaults().bindings.first { it.command == Commands.REFRESH }
        assertTrue(binding.allowWhileTyping)
    }
}
