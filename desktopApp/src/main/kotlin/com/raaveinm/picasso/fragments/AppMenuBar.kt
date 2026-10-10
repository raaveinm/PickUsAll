package com.raaveinm.picasso.fragments

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import com.raaveinm.picasso.ui.app.QuickDestination
import org.jetbrains.compose.resources.stringResource
import pickusall.desktopapp.generated.resources.Res
import pickusall.desktopapp.generated.resources.application_settings
import pickusall.desktopapp.generated.resources.behavior_settings
import pickusall.desktopapp.generated.resources.friend_window_search
import pickusall.desktopapp.generated.resources.go_to_chat
import pickusall.desktopapp.generated.resources.go_to_friends
import pickusall.desktopapp.generated.resources.go_to_library
import pickusall.desktopapp.generated.resources.quick_nav_menu
import pickusall.desktopapp.generated.resources.server_settings
import pickusall.desktopapp.generated.resources.settings_selector
import pickusall.desktopapp.generated.resources.steam_features
import pickusall.desktopapp.generated.resources.visual_settings

@Composable
fun FrameWindowScope.AppMenuBar(
    onNavigate: (QuickDestination) -> Unit,
    onOpenSteamWindow: () -> Unit,
) {
    MenuBar {
        Menu(stringResource(Res.string.quick_nav_menu)) {
            Item(stringResource(Res.string.go_to_library), onClick = { onNavigate(QuickDestination.Library) })
            Item(stringResource(Res.string.go_to_chat), onClick = { onNavigate(QuickDestination.Chat) })
            Item(stringResource(Res.string.go_to_friends), onClick = { onNavigate(QuickDestination.Friends) })

            Menu(stringResource(Res.string.settings_selector)) {
                Item(stringResource(Res.string.server_settings), onClick = { onNavigate(QuickDestination.ServerSettings) })
                Item(stringResource(Res.string.application_settings), onClick = { onNavigate(QuickDestination.ApplicationSettings) })
                Item(stringResource(Res.string.visual_settings), onClick = { onNavigate(QuickDestination.VisualSettings) })
                Item(stringResource(Res.string.behavior_settings), onClick = { onNavigate(QuickDestination.BehaviourSettings) })
            }
        }
        Menu(stringResource(Res.string.steam_features)) {
            Item(stringResource(Res.string.friend_window_search), onClick = onOpenSteamWindow)
        }
    }
}