package com.raaveinm.pickusall.core.designsystem.keybinding

//
// Created by Kirill "Raaveinm" on 9/28/26.
//

/**
 * Overall commands which could be triggered
 *
 * Applicable context defined in
 * @see com.raaveinm.pickusall.core.designsystem.keybinding.Context
 *
 * The enum [name] is the key a rebinding is persisted under (see `BehaviourSettingsStore`),
 * so renaming a constant silently resets the user's binding for it. Removing one is safe:
 * stored entries for unknown names are ignored.
 */

enum class Commands(val title: String) {
    REFRESH("refresh"),             // Refresh/update action
}
