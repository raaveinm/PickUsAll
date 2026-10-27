package com.raaveinm.picasso.ui.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.picasso.ui.settings.fragments.ToggleSetting
import com.raaveinm.picasso.ui.settings.viewmodel.BehaviourState
import com.raaveinm.picasso.ui.settings.viewmodel.SettingsViewModel
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.PlatformSpecificDim
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

//
// Created by Kirill "Raaveinm" on 10/26/2026.
//

@Composable
fun VisualScreen(
    viewModel: SettingsViewModel,
    appViewModel: AppViewModel,
    modifier: Modifier
) {
    val state: BehaviourState by viewModel.behaviourState.collectAsState()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 528.dp),
            contentPadding = PaddingValues(
                top = PlatformSpecificDim.contentPaddingMedium,
                bottom = PlatformSpecificDim.contentPaddingLarge
            ),
            verticalArrangement = Arrangement.Top,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.medium)
        ) {

            ///////////////////////////////////////////////
            /// Steam Client Background
            ///////////////////////////////////////////////
            item {
                var toggle by remember { mutableStateOf(false) }
                var progress by remember { mutableStateOf(false) }
                Card {
                    LaunchedEffect(toggle) {
                        progress = true
                        delay(600.milliseconds)
                        progress = false
                    }

                    ToggleSetting(
                        modifier = Modifier.padding(Dimensions.medium),
                        onToggle = { toggle = !toggle },
                        isChecked = toggle,
                        isInProgress = progress
                    )
                }
            }
        }
    }
}