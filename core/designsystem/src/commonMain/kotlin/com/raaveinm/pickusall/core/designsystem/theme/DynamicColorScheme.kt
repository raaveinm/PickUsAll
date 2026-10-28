package com.raaveinm.pickusall.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

//
// Created by Kirill "Raaveinm" on 10/28/2026.
//


@Composable
internal expect fun dynamicColorSchemeOrNull(darkTheme: Boolean): ColorScheme?
