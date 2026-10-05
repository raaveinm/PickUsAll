package com.raaveinm.picasso.ui.settings.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.picasso.ui.settings.viewmodel.SettingsViewModel

@Composable
actual fun BehaviourScreen(
    viewModel: SettingsViewModel,
    appViewModel: AppViewModel,
    modifier: Modifier
) {
    Text("Android Settings")
}