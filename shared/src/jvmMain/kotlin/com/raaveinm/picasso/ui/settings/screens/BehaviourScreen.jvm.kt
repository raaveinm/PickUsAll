package com.raaveinm.picasso.ui.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.picasso.ui.settings.fragments.KeyBindingsSection
import com.raaveinm.picasso.ui.settings.viewmodel.BehaviourState
import com.raaveinm.picasso.ui.settings.viewmodel.SettingsViewModel
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions

@Composable
actual fun BehaviourScreen(
    viewModel: SettingsViewModel,
    appViewModel: AppViewModel,
    modifier: Modifier
) {
    val state: BehaviourState by viewModel.behaviourState.collectAsState()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn( //TODO grid when item size more then 728.dp
            modifier = Modifier
                .fillMaxSize()
                .sizeIn(maxWidth = 1024.dp)
                .padding(horizontal = Dimensions.medium),
            contentPadding = PaddingValues(vertical = Dimensions.extraLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.large),
        ) {

            ///////////////////////////////////////////////
            /// Key Bindings
            ///////////////////////////////////////////////
            item {
                KeyBindingsSection(
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