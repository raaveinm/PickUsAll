@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.raaveinm.picasso.ui.actions

expect object ClipboardHelper {
    fun setText(text: String)
}