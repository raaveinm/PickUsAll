package com.raaveinm.picasso.ui.settings.viewmodel

import com.raaveinm.pickusall.core.designsystem.keybinding.Commands
import com.raaveinm.pickusall.core.designsystem.keybinding.KeyChord
import com.raaveinm.pickusall.core.designsystem.keybinding.RebindProblem

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

/** One row of the key binding list: a command and the chord that currently triggers it. */
data class ChordBindings(
    val command: Commands,
    val chord: KeyChord,
    val isDefault: Boolean
)

/** One-shot feedback after a rebinding attempt, rendered by `StatusCard`. */
sealed interface Notice {
    data class Rebound(val command: Commands, val chord: KeyChord) : Notice
    data class Reset(val command: Commands, val chord: KeyChord) : Notice
    data class Rejected(val command: Commands, val chord: KeyChord, val problem: RebindProblem) : Notice
    data object Cancelled : Notice
}

data class BehaviourState(
    val bindings: List<ChordBindings> = emptyList(), // list of hotkeys actions
    val recordingFor: Commands? = null,              // non-null while waiting for the user to press the new chord
    val notice: Notice? = null
)
