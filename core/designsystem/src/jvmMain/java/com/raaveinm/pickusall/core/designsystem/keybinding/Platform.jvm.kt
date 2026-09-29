package com.raaveinm.pickusall.core.designsystem.keybinding

actual val IS_APPLE: Boolean
    get() = System.getProperty("os.name").orEmpty().lowercase().contains("mac")