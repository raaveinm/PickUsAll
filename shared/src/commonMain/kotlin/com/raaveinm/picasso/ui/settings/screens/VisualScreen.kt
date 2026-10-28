package com.raaveinm.picasso.ui.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raaveinm.core.datastore.settings.ThemeMode
import com.raaveinm.core.datastore.settings.VisualSettingsList
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.picasso.ui.settings.fragments.SwitchSetting
import com.raaveinm.picasso.ui.settings.fragments.ToggleSetting
import com.raaveinm.picasso.ui.settings.viewmodel.SettingsViewModel
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.PlatformSpecificDim
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.theme_mode
import pickusall.shared.generated.resources.theme_mode_auto
import pickusall.shared.generated.resources.theme_mode_dark
import pickusall.shared.generated.resources.theme_mode_light
import pickusall.shared.generated.resources.toggle_dynamic_color
import pickusall.shared.generated.resources.toggle_pfp_background

//
// Created by Kirill "Raaveinm" on 10/26/2026.
//

@Composable
fun VisualScreen(
    viewModel: SettingsViewModel,
    appViewModel: AppViewModel,
    modifier: Modifier
) {
    val visualSettings: VisualSettingsList by viewModel.visualSettings.collectAsState()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            modifier = Modifier.padding(horizontal = Dimensions.medium),
            columns = GridCells.Adaptive(minSize = 400.dp),
            contentPadding = PaddingValues(
                top = PlatformSpecificDim.contentPaddingMedium,
                bottom = PlatformSpecificDim.contentPaddingLarge
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.small),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.medium)
        ) {

            ///////////////////////////////////////////////
            /// Theme: auto / light / dark
            ///////////////////////////////////////////////
            item {
                SwitchSetting(
                    modifier = Modifier,
                    listOfOptions = listOf(
                        stringResource(Res.string.theme_mode_auto),
                        stringResource(Res.string.theme_mode_light),
                        stringResource(Res.string.theme_mode_dark)
                    ),
                    selectedOption = visualSettings.themeMode.ordinal,
                    onSelected = { viewModel.setThemeMode(ThemeMode.entries[it]) },
                    settingNameRes = Res.string.theme_mode
                )
            }

            ///////////////////////////////////////////////
            /// Dynamic / application colors
            ///////////////////////////////////////////////
            item {
                ToggleSetting(
                    modifier = Modifier,
                    onToggle = { viewModel.setDynamicColor(!visualSettings.isDynamicColor) },
                    isChecked = visualSettings.isDynamicColor,
                    settingNameRes = Res.string.toggle_dynamic_color
                )
            }

            ///////////////////////////////////////////////
            /// Steam profile background
            ///////////////////////////////////////////////
            item {
                ToggleSetting(
                    modifier = Modifier,
                    onToggle = { viewModel.setPFPBackground(!visualSettings.isSteamPFProfileBackground) },
                    isChecked = visualSettings.isSteamPFProfileBackground,
                    settingNameRes = Res.string.toggle_pfp_background
                )
            }
        }
    }
}