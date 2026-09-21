@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.raaveinm.pickusall.core.designsystem.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

actual object PlatformSpecificDim {
    actual val systemBarsPadding: PaddingValues
        get() = PaddingValues(top = 56.dp, bottom = 8.dp)
    actual val contentPaddingMedium: Dp
        get() = 32.dp
    actual val contentPaddingLarge: Dp
        get() = 144.dp
    actual val contentPaddingAdvanced: Dp
        get() = 232.dp
}