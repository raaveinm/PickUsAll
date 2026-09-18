@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.raaveinm.picasso.ui.actions

import platform.UIKit.UIPasteboard

actual object ClipboardHelper {
    actual fun setText(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}