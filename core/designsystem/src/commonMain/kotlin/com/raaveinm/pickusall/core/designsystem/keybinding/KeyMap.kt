package com.raaveinm.pickusall.core.designsystem.keybinding

import androidx.compose.ui.input.key.Key

//
// Created by Kirill "Raaveinm" on 9/29/26.
//

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

    private companion object {
        val ReservedChords = setOf(
            chord(Key.C, SpecialKeys.PRIMARY), chord(Key.V, SpecialKeys.PRIMARY), chord(Key.X, SpecialKeys.PRIMARY),
            chord(Key.A, SpecialKeys.PRIMARY), chord(Key.Z, SpecialKeys.PRIMARY), chord(Key.Z, SpecialKeys.PRIMARY, SpecialKeys.SHIFT),
            chord(Key.Q, SpecialKeys.PRIMARY), chord(Key.W, SpecialKeys.PRIMARY),
        )
    }
}