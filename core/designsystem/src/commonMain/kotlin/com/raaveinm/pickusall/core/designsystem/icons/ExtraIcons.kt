package com.raaveinm.pickusall.core.designsystem.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.vectorResource
import pickusall.core.designsystem.generated.resources.Res
import pickusall.core.designsystem.generated.resources.ic_apple_icon
import pickusall.core.designsystem.generated.resources.ic_arch_icon
import pickusall.core.designsystem.generated.resources.ic_steam_icon
import pickusall.core.designsystem.generated.resources.ic_win_icon

//
// Created by Kirill "Raaveinm" on 11/05/2026.
//

object ExtraIcons {
    val AppleIcon: ImageVector
        @Composable
        get() = vectorResource(Res.drawable.ic_apple_icon)

    val SteamIcon: ImageVector
        @Composable
        get() = vectorResource(Res.drawable.ic_steam_icon)

    val WinIcon: ImageVector
        @Composable
        get() = vectorResource(Res.drawable.ic_win_icon)

    val ArchIcon: ImageVector
        @Composable
        get() = vectorResource(Res.drawable.ic_arch_icon)
}
