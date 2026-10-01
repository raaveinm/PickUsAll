package com.raaveinm.picasso

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.raaveinm.picasso.ui.navigation.Profile
import com.raaveinm.picasso.ui.navigation.Settings
import com.raaveinm.picasso.ui.profile.ProfileScreen
import com.raaveinm.picasso.ui.settings.SettingsScreen
import com.raaveinm.picasso.ui.settings.viewmodel.SettingsViewModel
import com.raaveinm.pickusall.core.designsystem.components.NavBar
import com.raaveinm.pickusall.core.designsystem.components.SidebarMenu
import com.raaveinm.pickusall.core.designsystem.components.WarnBox
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.PicassoTheme
import com.raaveinm.pickusall.core.designsystem.theme.PlatformSpecificDim
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import com.raaveinm.pickusall.core.designsystem.utils.CoilInitializer
import com.raaveinm.pickusall.core.designsystem.utils.WarnLevel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.unauthorized

private const val CanvasTab = 0
private const val ChatTab = 1
private const val FriendsTab = 2
private const val SettingsTab = 3
private const val ProfileTab = 4

@Composable
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
                    .blur(animatedBlur.dp),
                contentAlignment = Alignment.Center,
                startDestination = Canvas
            ) {
                composable<Canvas> {
                    CanvasScreen(
                        modifier = Modifier,
                        topNavModifier = Modifier.padding(PlatformSpecificDim.systemBarsPadding),
                        viewModel = canvasViewModel
                    )
                }
                composable<ChatGraph> { backStackEntry ->
                    val route = backStackEntry.toRoute<ChatGraph>()
                    ChatScreen(
                        modifier = Modifier.padding(PlatformSpecificDim.systemBarsPadding),
                        viewModel = chatViewModel,
                        appViewModel = appViewModel,
                        selectedChatId = route.selectedChatId,
                        nestedNavHostController = rememberNavController()
                    )
                }
                composable<Friends> {
                    FriendScreen(
                        modifier = Modifier.padding(PlatformSpecificDim.systemBarsPadding),
                        viewModel = chatViewModel,
                        appViewModel = appViewModel,
                        onMessageClick = ::openChat
                    )
                }
                composable<Settings> {
                    SettingsScreen(
                        modifier = Modifier.padding(PlatformSpecificDim.systemBarsPadding),
                        viewModel = settingsViewModel,
                        appViewModel = appViewModel
                    )
                }
                composable<Profile> {
                    ProfileScreen()
                }
            }

            NavBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(3f)
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
                        ProfileTab -> Profile
                        else -> Canvas
                    }
                    openTab(screen, it)
                },
            )

            ///////////////////////////////////////////////
            // Side Menu
            ///////////////////////////////////////////////

            val sideNavBoxModifier =
                if (appUiState.isSideBarExpanded)
                    Modifier.fillMaxSize().zIndex(2f).clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { appViewModel.setSideBarExpanded(false) }
                else
                    Modifier.fillMaxSize()

            Box(sideNavBoxModifier) {
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
                        modifier = Modifier.size(width = 320.dp, height = 640.dp),
                        onProfileClick = {
                            openTab(Profile, ProfileTab)
                            appViewModel.setSideBarExpanded(false)
                        },
                        onSettingsClick = {
                            openTab(Settings, SettingsTab)
                            appViewModel.setSideBarExpanded(false)
                        },
                        profileId = appUiState.user?.steamId,
                        profileIcon = appUiState.user?.avatarMedium?:"",
                        profileName = appUiState.user?.personaName?:stringResource(Res.string.unauthorized),
                        personaState = appUiState.user?.personaState?:0
                    )
                }
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
                modifier = Modifier.zIndex(2f).padding(top = 48.dp).size(24.dp).align(Alignment.TopEnd),
                content = {Text("WL")}
            )

            AnimatedVisibility(
                visible = appUiState.message != null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .sizeIn(maxWidth = 1024.dp)
                    .zIndex(3f)
                    .padding(
                        top = Dimensions.extraLarge,
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
                    onCopyClicked = { appViewModel.copyMessage() },
                    onDismissClicked = { appViewModel.dismissMessage() }
                )
            }
        }
    }
}
