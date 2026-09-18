@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.raaveinm.picasso.ui.actions

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual object ClipboardHelper {
    actual fun setText(text: String) {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    }
}