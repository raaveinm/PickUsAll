package com.raaveinm.picasso

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.picasso.ui.canvas.CanvasScreen
import com.raaveinm.picasso.ui.canvas.viewmodel.CanvasViewModel
import com.raaveinm.picasso.ui.chat.ChatScreen
import com.raaveinm.picasso.ui.chat.viewmodel.ChatViewModel
import com.raaveinm.picasso.ui.friends.FriendScreen
import com.raaveinm.picasso.ui.navigation.Canvas
import com.raaveinm.picasso.ui.navigation.ChatGraph
import com.raaveinm.picasso.ui.navigation.Friends
import com.raaveinm.picasso.ui.navigation.Settings
import com.raaveinm.picasso.ui.settings.SettingsScreen
import com.raaveinm.picasso.ui.settings.viewmodel.SettingsViewModel
import com.raaveinm.pickusall.core.designsystem.components.NavBar
import com.raaveinm.pickusall.core.designsystem.components.SidebarMenu
import com.raaveinm.pickusall.core.designsystem.components.WarnBox
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.PicassoTheme
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import com.raaveinm.pickusall.core.designsystem.utils.CoilInitializer
import com.raaveinm.pickusall.core.designsystem.utils.WarnLevel
import org.koin.compose.viewmodel.koinViewModel

private const val CanvasTab = 0
private const val ChatTab = 1
private const val FriendsTab = 2
private const val SettingsTab = 3

@Composable
@Preview
fun App(
    navController: NavHostController = rememberNavController()
) {
    val canvasViewModel = koinViewModel<CanvasViewModel>()
    val chatViewModel = koinViewModel<ChatViewModel>()
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val appViewModel = koinViewModel<AppViewModel>()
    val appUiState by appViewModel.uiState.collectAsState()

    val animatedBlur by animateFloatAsState(
        targetValue = if (!appUiState.isSideBarExpanded) 0f else 64f,
        animationSpec = tween(400)
    )

    PicassoTheme {
        CoilInitializer()

        fun openTab(route: Any, tab: Int) {
            navController.navigate(route) {
                popUpTo<Canvas> { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            appViewModel.selectTab(tab)
        }

        fun openChat(chatId: Long) {
            navController.navigate(ChatGraph(selectedChatId = chatId)) {
                popUpTo<Canvas> { saveState = true }
                launchSingleTop = true
            }
            appViewModel.selectTab(ChatTab)
        }

        val gradientBrush = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.inverseOnSurface
            )
        )

        ///////////////////////////////////////////////
        // Main Navigation Screen
        ///////////////////////////////////////////////

        Box(Modifier.background(gradientBrush)) {
            NavHost(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(animatedBlur.dp)
                    .systemBarsPadding(),
                contentAlignment = Alignment.Center,
                startDestination = Canvas
            ) {
                composable<Canvas> {
                    CanvasScreen(
                        modifier = Modifier,
                        canvasViewModel
                    )
                }
                composable<ChatGraph> { backStackEntry ->
                    val route = backStackEntry.toRoute<ChatGraph>()
                    ChatScreen(
                        modifier = Modifier.safeContentPadding(),
                        viewModel = chatViewModel,
                        selectedChatId = route.selectedChatId,
                        nestedNavHostController = rememberNavController()
                    )
                }
                composable<Friends> {
                    FriendScreen(
                        modifier = Modifier.safeContentPadding(),
                        viewModel = chatViewModel,
                        // TODO(start a new DM when there is no conversation with that friend yet)
                        onMessageClick = { chatId -> chatId?.let(::openChat) }
                    )
                }
                composable<Settings> {
                    SettingsScreen(
                        modifier = Modifier.safeContentPadding(),
                        viewModel = settingsViewModel
                    )
                }
            }

            NavBar(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(bottom = Dimensions.medium),
                nestedModifier = Modifier,
                fabModifier = Modifier,
                selectedId = appUiState.selectedTab,
                onItemClick = {
                    if (it == SettingsTab) {
                        appViewModel.toggleSideBar()
                        appViewModel.selectTab(it)
                        return@NavBar
                    }

                    appViewModel.setSideBarExpanded(false)
                    val screen = when (it) {
                        CanvasTab -> Canvas
                        ChatTab -> ChatGraph()
                        FriendsTab -> Friends
                        else -> Canvas
                    }
                    openTab(screen, it)
                },
            )

            ///////////////////////////////////////////////
            // Side Menu
            ///////////////////////////////////////////////

            AnimatedVisibility(
                visible = appUiState.isSideBarExpanded,
                modifier = Modifier
                    .zIndex(2f)
                    .align(Alignment.CenterEnd)
                    .clip(Shapes.sideBarCardShape),
                enter = expandHorizontally(
                    expandFrom = Alignment.End,
                    animationSpec = tween(300)
                ),
                exit = shrinkHorizontally(
                    shrinkTowards = Alignment.End,
                    animationSpec = tween(300)
                )
            ) {
                SidebarMenu(
                    Modifier
                        .size(width = 320.dp, height = 640.dp),
                    onSettingsClick = {
                        openTab(Settings, SettingsTab)
                        appViewModel.setSideBarExpanded(false)
                    }
                )
            }

            ///////////////////////////////////////////////
            // Error Container
            ///////////////////////////////////////////////

            Button( // debug
                onClick = {
                    appViewModel.postMessage(
                        level = listOf(WarnLevel.WARN, WarnLevel.ERROR, WarnLevel.INFO).random(),
                        text = "a long long long long trace message"
                    )
                },
                modifier = Modifier.size(24.dp).align(Alignment.TopEnd).zIndex(2f),
                content = {Text("WL")}
            )

            AnimatedVisibility(
                visible = appUiState.message != null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .sizeIn(maxWidth = 1024.dp)
                    .zIndex(3f)
                    .padding(
                        top = Dimensions.large,
                        start = Dimensions.medium,
                        end = Dimensions.medium
                    )
                    .clip(Shapes.roundedSmall),
                enter = expandHorizontally(
                    expandFrom = Alignment.End,
                    animationSpec = tween(300)
                ),
                exit = shrinkHorizontally(
                    shrinkTowards = Alignment.End,
                    animationSpec = tween(300)
                )
            ) {
                WarnBox(
                    modifier = Modifier,
                    level = appUiState.message?.level ?: WarnLevel.WARN,
                    what = appUiState.message?.text ?: "",
                    onDismissClicked = { appViewModel.dismissMessage() }
                )
            }
        }
    }
}
