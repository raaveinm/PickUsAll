package com.raaveinm.picasso.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raaveinm.picasso.ui.settings.fragments.SettingsCard
import com.raaveinm.picasso.ui.settings.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel
) {
    val items = remember {
        listOf(
            Triple(Icons.Default.Lan, "Server", {}), // Server connection settings
            Triple(Icons.Default.SettingsApplications, "Application", {}), // What app should do (mic settings)
            Triple(Icons.Default.Palette, "Visual", {}), // How Picasso looks
            Triple(Icons.Default.Psychology, "Behaviour", {}), // Events handling
                                        // (how Picasso should react on outer events (like calls / system startup)
            Triple(Icons.AutoMirrored.Filled.Article, "Docs", {}), // External links on git / docs / troubleshooting
        )
    }

    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = GridCells.FixedSize(216.dp),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.Center
    ) {
        items(items) { item ->
            SettingsCard(
                modifier = Modifier.size(width = 200.dp, height = 100.dp).padding(8.dp),
                icon = item.first,
                name = item.second,
                onClick = item.third
            )
        }
    }
}