package com.raaveinm.picasso.ui.settings.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raaveinm.core.model.preferences.OnCloseAction
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.picasso.ui.settings.fragments.SwitchSetting
import com.raaveinm.picasso.ui.settings.fragments.ToggleSetting
import com.raaveinm.picasso.ui.settings.fragments.behaviour.KeyBindingsSection
import com.raaveinm.picasso.ui.settings.viewmodel.BehaviourState
import com.raaveinm.picasso.ui.settings.viewmodel.SettingsViewModel
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.PlatformSpecificDim
import org.jetbrains.compose.resources.stringArrayResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.hide_tray_icon
import pickusall.shared.generated.resources.on_close_options
import pickusall.shared.generated.resources.on_close_request

@Composable
actual fun BehaviourScreen(
    viewModel: SettingsViewModel,
    appViewModel: AppViewModel,
    modifier: Modifier
) {
    val state: BehaviourState by viewModel.behaviourState.collectAsState()

    Box(
        modifier = modifier.padding(horizontal = Dimensions.medium),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(400.dp),
            contentPadding = PaddingValues(
                top = PlatformSpecificDim.contentPaddingMedium,
                bottom = PlatformSpecificDim.contentPaddingLarge
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.medium),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.medium)
        ) {
            ///////////////////////////////////////////////
            /// on close behavior
            ///////////////////////////////////////////////
            item {
                val backgroundColor by animateColorAsState(
                    targetValue = if (state.isTrayIconHidden && state.onCloseSelectedAction.ordinal == 0)
                        MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainer,
                    animationSpec = tween(durationMillis = 400)
                )
                val foregroundColor by animateColorAsState(
                    targetValue = if (state.isTrayIconHidden && state.onCloseSelectedAction.ordinal == 0)
                        MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
                    animationSpec = tween(durationMillis = 400)
                )
                SwitchSetting(
                    listOfOptions = stringArrayResource(Res.array.on_close_options),
                    selectedOption = state.onCloseSelectedAction.ordinal,
                    onSelected = { selected -> viewModel.setOnCloseRequestAction(OnCloseAction.entries[selected]) },
                    settingNameRes = Res.string.on_close_request,
                    backgroundColor = backgroundColor,
                    foregroundColor = foregroundColor,
                    asColumn = true
                )
            }

            ///////////////////////////////////////////////
            /// on close behavior
            ///////////////////////////////////////////////
            item {
                ToggleSetting(
                    modifier = Modifier,
                    onToggle = { viewModel.setHiddenTrayIcon(isHidden = !state.isTrayIconHidden) },
                    isChecked = state.isTrayIconHidden,
                    settingNameRes = Res.string.hide_tray_icon
                )
            }

            ///////////////////////////////////////////////
            /// Key Bindings
            ///////////////////////////////////////////////
            item {
                Card {
                    KeyBindingsSection(
                        modifier = Modifier.padding(Dimensions.small),
                        state = state,
                        onRebindClick = viewModel::startRecording,
                        onCancelClick = viewModel::cancelRecording,
                        onResetClick = viewModel::resetBinding,
                        onChordCaptured = viewModel::onChordCaptured,
                        onLeave = viewModel::clearKeyBindingFeedback
                    )
                }
            }
        }
    }
}