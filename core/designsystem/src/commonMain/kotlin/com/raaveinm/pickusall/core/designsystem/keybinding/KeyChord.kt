package com.raaveinm.pickusall.core.designsystem.keybinding

import androidx.compose.runtime.Immutable
import androidx.compose.ui.input.key.Key

//
// Created by Kirill "Raaveinm" on 9/28/26.
//

//PRIMARY, SHIFT, ALTERNATIVE, CONTROL
@Immutable
data class KeyChord(
    val key: Key,
    val isPrimary: Boolean,
    val isShift: Boolean,
    val isAlt: Boolean,
    val isControl: Boolean
) {
    fun display(){}
}