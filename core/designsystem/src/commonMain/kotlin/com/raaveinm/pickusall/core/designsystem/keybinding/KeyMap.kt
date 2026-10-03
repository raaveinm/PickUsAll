package com.raaveinm.pickusall.core.designsystem.keybinding

import androidx.compose.ui.input.key.Key

//
// Created by Kirill "Raaveinm" on 9/29/26.
//

/**
 * Immutable snapshot of "which chord triggers which command". A rebind produces a new instance
 * (see [withOverrides]) rather than mutating this one.
 *
 * The constructor throws on an inconsistent list - intended for hardcoded lists like
 * [DEFAULT_BINDINGS], where that is a programming error. Anything user-supplied goes through
 * [withOverrides] / [check], which reject bad input instead of throwing.
 */
class KeyMap(val bindings: List<Binding>) {

    init {
        val problems = buildList {
            bindings.filter { it.chord in ReservedChords }
                .forEach { add("Reserved chord ${it.chord.display()} used by ${it.command}") }

            bindings.groupBy { it.chord to it.context }
                .filterValues { it.size > 1 }
                .forEach { (k, v) -> add("Duplicate ${k.first.display()} in ${k.second}: ${v.map { it.command }}") }

            bindings.groupBy { it.chord }.forEach { (chord, list) ->
                if (list.size > 1 && list.any { it.context == Context.APPLICATION }) {
                    add("Global chord ${chord.display()} is also bound in: ${list.filter { it.context != Context.APPLICATION }.map { it.context }}")
                }
            }

            bindings.groupBy { it.command }
                .filterValues { it.size > 1 }
                .forEach { (c, _) -> add("Command $c is bound more than once") }
        }
        require(problems.isEmpty()) { "Invalid keymap:\n" + problems.joinToString("\n") { " - $it" } }
    }

    private val byChord: Map<KeyChord, List<Binding>> = bindings.groupBy { it.chord }
    private val byCommand: Map<Commands, Binding> = bindings.associateBy { it.command }

    fun bindingsFor(chord: KeyChord): List<Binding> = byChord[chord].orEmpty()

    fun chordFor(command: Commands): KeyChord? = byCommand[command]?.chord

    /**
     * Whether [chord] may be assigned to [command]. `null` means yes, otherwise the reason it can't.
     * Doesn't change anything - the caller applies the rebind (via [withOverrides]) only on `null`.
     */
    fun check(command: Commands, chord: KeyChord): RebindProblem? {
        if (chord in ReservedChords) return RebindProblem.Reserved

        val hasModifier: Boolean = chord.isPrimary || chord.isShift || chord.isAlt || chord.isControl
        if (!hasModifier && chord.key !in FUNCTION_KEYS) return RebindProblem.NeedsModifier

        val context: Context = byCommand[command]?.context ?: return null
        val owner: Binding? = bindings.firstOrNull {
            it.command != command && it.chord == chord && contextsOverlap(it.context, context)
        }
        return owner?.let { RebindProblem.Taken(it.command) }
    }

    companion object {

        /** Factory keymap. */
        fun defaults(): KeyMap = KeyMap(DEFAULT_BINDINGS)

        /** The chord [command] has out of the box, for "is this customised?" / "reset" in the UI. */
        fun defaultChordFor(command: Commands): KeyChord? =
            DEFAULT_BINDINGS.firstOrNull { it.command == command }?.chord


        fun withOverrides(overrides: Map<Commands, KeyChord>): KeyMap {
            var keyMap: KeyMap = defaults()
            Commands.entries.forEach { command: Commands ->
                val chord: KeyChord = overrides[command] ?: return@forEach
                if (keyMap.check(command, chord) != null) return@forEach
                keyMap = KeyMap(
                    keyMap.bindings.map {
                        if (it.command == command) it.copy(chord = chord, allowWhileTyping = chord.isPrimary) else it
                    }
                )
            }
            return keyMap
        }

        /** Same chord in two contexts collides unless both are specific and different. */
        private fun contextsOverlap(a: Context, b: Context): Boolean =
            a == b || a == Context.APPLICATION || b == Context.APPLICATION

        private val ReservedChords = setOf(
            chord(Key.C, SpecialKeys.PRIMARY), chord(Key.V, SpecialKeys.PRIMARY), chord(Key.X, SpecialKeys.PRIMARY),
            chord(Key.A, SpecialKeys.PRIMARY), chord(Key.Z, SpecialKeys.PRIMARY), chord(Key.Z, SpecialKeys.PRIMARY, SpecialKeys.SHIFT),
            chord(Key.Q, SpecialKeys.PRIMARY), chord(Key.W, SpecialKeys.PRIMARY),
        )
    }
}
