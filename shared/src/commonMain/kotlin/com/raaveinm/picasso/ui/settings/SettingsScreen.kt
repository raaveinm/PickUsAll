package com.raaveinm.picasso.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.picasso.ui.navigation.Application
import com.raaveinm.picasso.ui.navigation.Behaviour
import com.raaveinm.picasso.ui.navigation.OptionList
import com.raaveinm.picasso.ui.navigation.Server
import com.raaveinm.picasso.ui.navigation.Visual
import com.raaveinm.picasso.ui.settings.fragments.SettingsCard
import com.raaveinm.picasso.ui.settings.screens.BehaviourScreen
import com.raaveinm.picasso.ui.settings.screens.ServerScreen
import com.raaveinm.picasso.ui.settings.screens.VisualScreen
import com.raaveinm.picasso.ui.settings.viewmodel.SettingsViewModel
import com.raaveinm.pickusall.core.designsystem.components.ScrollBar
import com.raaveinm.pickusall.core.designsystem.obj.ColorBrushes.backgroundGradientBrush
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.utils.WarnLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.no_reachable_servers
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    appViewModel: AppViewModel,
    nestedNavController: NavHostController = rememberNavController()
) {
    val uriHandler = LocalUriHandler.current
    val errorServerNotFound = stringResource(Res.string.no_reachable_servers)
    val coroutineScope = rememberCoroutineScope()

    val items = remember {
        listOf<Triple<ImageVector, String, () -> Unit>>(
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
            },                                  // on outer events (like calls / system startup / notification)
            Triple(Icons.AutoMirrored.Filled.Article, "Docs") { // External links on git / docs / troubleshooting
                coroutineScope.launch {
                    val server = viewModel.getReachableServer()
                    if (server == null) {
                        appViewModel.postMessage(WarnLevel.ERROR, errorServerNotFound)
                        return@launch
                    }
                    uriHandler.openUri("http://${server.url}/docs")
                }
            }
        )
    }

    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val isOptionList = navBackStackEntry?.destination?.hasRoute<OptionList>() == true

    BoxWithConstraints(modifier) {
        val isCompact = maxWidth < 700.dp
        val nestedModifier = Modifier
            .background(MaterialTheme.colorScheme.backgroundGradientBrush)
            .padding(start = if (isCompact) 0.dp else 164.dp)
            .fillMaxSize()

        NavHost(
            navController = nestedNavController,
            startDestination = OptionList
        ) {
            composable<OptionList> {
                if (isCompact) {
                    LazyColumn(
                        modifier = modifier
                            .padding(bottom = Dimensions.paddingLarge)
                            .fillMaxSize()
                            .background(color = MaterialTheme.colorScheme.surface),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(items) { item ->
                            SettingsCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimensions.medium)
                                    .height(96.dp)
                                    .padding(bottom = Dimensions.small),
                                icon = item.first,
                                name = item.second,
                                onClick = item.third
                            )
                        }
                    }
                } else {
                    val state = rememberLazyListState()
                    LazyRow(
                        modifier = modifier
                            .fillMaxSize()
                            .background(color = MaterialTheme.colorScheme.surface),
                        state = state,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
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
                    if (maxWidth < 1000.dp) {
                        var autoHide by remember { mutableStateOf(false) }
                        LaunchedEffect(maxWidth) {
                            delay(3000.milliseconds)
                            autoHide = true
                        }
                        ScrollBar(
                            modifier = Modifier
                                .padding(
                                    top = maxHeight / 2,
                                    start = Dimensions.medium,
                                    end = Dimensions.medium
                                )
                                .height(120.dp),
                            state = state,
                            orientation = Orientation.Horizontal,
                            autoHide = autoHide
                        )
                    }
                }
            }

            composable<Server> {
                ServerScreen(
                    modifier = nestedModifier,
                    viewModel = viewModel,
                    appViewModel = appViewModel
                )
            }
            composable<Application> {
            }
            composable<Visual> {
                VisualScreen(
                    viewModel = viewModel,
                    appViewModel = appViewModel,
                    modifier = nestedModifier
                )
            }
            composable<Behaviour> {
                BehaviourScreen(
                    viewModel = viewModel,
                    appViewModel = appViewModel,
                    modifier = nestedModifier
                )
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
                            .clickable(onClick = it.third)
                            .padding(horizontal = Dimensions.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = it.first,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            contentDescription = "${it.second}_nested_nav_destination"
                        )
                        Text(
                            text = it.second,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(start = Dimensions.extraSmall)
                        )
                    }
                }
            }
        }
    }
}