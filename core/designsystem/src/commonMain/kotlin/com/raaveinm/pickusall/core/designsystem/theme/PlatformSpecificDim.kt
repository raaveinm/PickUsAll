@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.raaveinm.pickusall.core.designsystem.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp

//
// Created by Kirill "Raaveinm" on 9/21/26.
//

expect object PlatformSpecificDim {
    val systemBarsPadding: PaddingValues
    val contentPaddingMedium: Dp
    val contentPaddingLarge: Dp
    val contentPaddingAdvanced: Dp
}