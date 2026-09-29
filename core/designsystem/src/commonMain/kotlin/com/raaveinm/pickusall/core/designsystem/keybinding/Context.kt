package com.raaveinm.pickusall.core.designsystem.keybinding

//
// Created by Kirill "Raaveinm" on 9/28/26.
//

/**
 * Application context to define applicable scope for command
 *
 * @param com.raaveinm.pickusall.core.designsystem.keybinding.Context
 * - `APPLICATION` - Accessible from each application screen
 * - `EXCLUDED` - Exception for `APPLICATION` context (ignoring their bindings)
 * - `CANVAS_LIBRARY` - Canvas/Library specific commands
 *
 */

enum class Context {
    APPLICATION,
    EXCLUDED,
    CANVAS_LIBRARY
}