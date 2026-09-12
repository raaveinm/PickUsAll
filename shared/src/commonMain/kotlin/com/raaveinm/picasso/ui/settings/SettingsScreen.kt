package com.raaveinm.picasso.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.raaveinm.picasso.ui.navigation.Application
import com.raaveinm.picasso.ui.navigation.Behaviour
import com.raaveinm.picasso.ui.navigation.OptionList
import com.raaveinm.picasso.ui.navigation.Server
import com.raaveinm.picasso.ui.navigation.Visual
import com.raaveinm.picasso.ui.settings.fragments.SettingsCard
import com.raaveinm.picasso.ui.settings.screens.ServerScreen
import com.raaveinm.picasso.ui.settings.viewmodel.SettingsViewModel
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    nestedNavController: NavHostController = rememberNavController()
) {
    val items = remember {
        listOf(
            Triple(Icons.Default.Lan, "Server") { // Server connection settings
                nestedNavController.navigate(Server)
            },
            Triple(Icons.Default.SettingsApplications, "Application") { // What app should do (mic settings)
                nestedNavController.navigate(Application)
            },
            Triple(Icons.Default.Palette, "Visual") { // How Picasso looks
                nestedNavController.navigate(Visual)
            },
            Triple(Icons.Default.Psychology, "Behaviour") { // Events handling
                nestedNavController.navigate(Behaviour) // (how Picasso should react
            },                                  // on outer events (like calls / system startup)
            Triple(Icons.AutoMirrored.Filled.Article, "Docs") { // External links on git / docs / troubleshooting
            }
        )
    }

    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val isOptionList = navBackStackEntry?.destination?.hasRoute<OptionList>() == true

    BoxWithConstraints(modifier) {
        val isCompact = maxWidth < 700.dp
        NavHost(
            navController = nestedNavController,
            startDestination = OptionList
        ) {
            composable<OptionList> {
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

            composable<Server> {
                ServerScreen(
                    modifier = Modifier.padding(start = if (isCompact) 0.dp else 164.dp).fillMaxSize(),
                    viewModel = viewModel
                )
            }
            composable<Application> {
            }
            composable<Visual> {
            }
            composable<Behaviour> {
            }
        }

        if (!isCompact && !isOptionList) {
            Column(
                Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .zIndex(1f)
                    .sizeIn(maxWidth = 152.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                verticalArrangement = Arrangement.Center
            ) {
                items.forEach {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 36.dp)
                            .padding(horizontal = Dimensions.medium)
                            .clickable(onClick = it.third),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = it.first,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            contentDescription = "${it.second}_nested_nav_destination"
                        )
                        Text(
                            text = it.second,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}