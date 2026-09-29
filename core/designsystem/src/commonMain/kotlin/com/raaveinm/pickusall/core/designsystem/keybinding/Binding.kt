package com.raaveinm.pickusall.core.designsystem.keybinding

//
// Created by Kirill "Raaveinm" on 9/29/26.
//

data class Binding(
    val chord: KeyChord,
    val command: Commands,
    val context: Context = Context.APPLICATION,
    val allowWhileTyping: Boolean = chord.isPrimary
)
