package com.raaveinm.pickusall.core.designsystem.keybinding

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

/** Why [KeyMap.check] refused a chord for a command. `null` from `check` means the chord is fine. */
sealed interface RebindProblem {

    /** Collides with a system/editing shortcut (copy, paste, quit, ...). */
    data object Reserved : RebindProblem

    /** A bare key (no modifier) would fire while the user is typing; function keys are exempt. */
    data object NeedsModifier : RebindProblem

    /** Another command already owns the chord in a context that overlaps with this one. */
    data class Taken(val by: Commands) : RebindProblem
}
