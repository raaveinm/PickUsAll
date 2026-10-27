package com.raaveinm.picasso

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.raaveinm.picasso.data.ApiClient
import com.raaveinm.picasso.ui.app.ProvideKeyMap
import com.raaveinm.picasso.ui.app.QuickDestination
import com.raaveinm.picasso.ui.app.SteamPFB
import com.raaveinm.picasso.ui.app.WindowActions
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.picasso.ui.canvas.CanvasScreen
import com.raaveinm.picasso.ui.canvas.viewmodel.CanvasViewModel
import com.raaveinm.picasso.ui.chat.ChatScreen
import com.raaveinm.picasso.ui.chat.viewmodel.ChatViewModel
import com.raaveinm.picasso.ui.friends.FriendScreen
import com.raaveinm.picasso.ui.navigation.Application
import com.raaveinm.picasso.ui.navigation.Behaviour
import com.raaveinm.picasso.ui.navigation.Canvas
import com.raaveinm.picasso.ui.navigation.ChatGraph
import com.raaveinm.picasso.ui.navigation.Friends
import com.raaveinm.picasso.ui.navigation.OptionList
import com.raaveinm.picasso.ui.navigation.Profile
import com.raaveinm.picasso.ui.navigation.Server
import com.raaveinm.picasso.ui.navigation.Settings
import com.raaveinm.picasso.ui.navigation.Visual
import com.raaveinm.picasso.ui.profile.ProfileScreen
import com.raaveinm.picasso.ui.settings.SettingsScreen
import com.raaveinm.picasso.ui.settings.viewmodel.SettingsViewModel
import com.raaveinm.pickusall.core.designsystem.components.NavBar
import com.raaveinm.pickusall.core.designsystem.components.SidebarMenu
import com.raaveinm.pickusall.core.designsystem.components.WarnBox
import com.raaveinm.pickusall.core.designsystem.keybinding.Commands
import com.raaveinm.pickusall.core.designsystem.keybinding.KeyMap
import com.raaveinm.pickusall.core.designsystem.keybinding.LocalKeyMap
import com.raaveinm.pickusall.core.designsystem.keybinding.toChordOrNull
import com.raaveinm.pickusall.core.designsystem.obj.ColorBrushes.backgroundGradientBrush
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.PicassoTheme
import com.raaveinm.pickusall.core.designsystem.theme.PlatformSpecificDim
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import com.raaveinm.pickusall.core.designsystem.utils.CoilInitializer
import com.raaveinm.pickusall.core.designsystem.utils.WarnLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
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
    navController: NavHostController = rememberNavController(),
    windowActions: WindowActions = WindowActions.Unsupported,
    quickNavRequests: Flow<QuickDestination> = emptyFlow()
) { ProvideKeyMap { AppContent(navController, windowActions, quickNavRequests) } }

@Composable
private fun AppContent(
    navController: NavHostController,
    windowActions: WindowActions,
    quickNavRequests: Flow<QuickDestination>
) {
    val canvasViewModel = koinViewModel<CanvasViewModel>()
    val chatViewModel = koinViewModel<ChatViewModel>()
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val appViewModel = koinViewModel<AppViewModel>()
    val appUiState by appViewModel.uiState.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val keyMap: KeyMap = LocalKeyMap.current
    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }

    ///////////////////////////////////////////////
    // Steam login hand-off
    ///////////////////////////////////////////////
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(appUiState.pendingLoginUrl) {
        val url = appUiState.pendingLoginUrl ?: return@LaunchedEffect
        uriHandler.openUri(url)
        appViewModel.onLoginUrlOpened()
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val isChatDestination: Boolean = currentBackStackEntry?.destination?.hasRoute<ChatGraph>() == true
    LaunchedEffect(isChatDestination) { if (!isChatDestination) appViewModel.showBottomBar() }

    var pendingSettings by remember { mutableStateOf<Any?>(null) }

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

        LaunchedEffect(quickNavRequests) {
            quickNavRequests.collect { destination ->
                appViewModel.setSideBarExpanded(false)
                when (destination) {
                    QuickDestination.Library -> openTab(Canvas, CanvasTab)
                    QuickDestination.Chat -> openTab(ChatGraph(), ChatTab)
                    QuickDestination.Friends -> openTab(Friends, FriendsTab)
                    else -> {
                        pendingSettings = when (destination) {
                            QuickDestination.ServerSettings -> Server
                            QuickDestination.ApplicationSettings -> Application
                            QuickDestination.VisualSettings -> Visual
                            else -> Behaviour
                        }
                        openTab(Settings, SettingsTab)
                    }
                }
            }
        }

        fun openChat(chatId: Long) {
            navController.navigate(ChatGraph(selectedChatId = chatId)) {
                popUpTo<Canvas> { saveState = true }
                launchSingleTop = true
            }
            appViewModel.selectTab(ChatTab)
        }

        ///////////////////////////////////////////////
        // Main Navigation Screen
        ///////////////////////////////////////////////

        Box(
            Modifier
                .background(MaterialTheme.colorScheme.backgroundGradientBrush)
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { event ->
                    val chord = event.toChordOrNull() ?: return@onPreviewKeyEvent false
                    val action: (() -> Unit)? = keyMap.bindingsFor(chord)
                        .firstNotNullOfOrNull { binding ->
                            when (binding.command) {
                                Commands.QUIT_APPLICATION -> windowActions.onQuit
                                Commands.MINIMIZE_APPLICATION -> windowActions.onMinimize
                                Commands.REFRESH -> null
                            }
                        }
                    action?.invoke()
                    action != null
                }
                .focusable()
        ) {
            val animatedBlur by animateFloatAsState(
                targetValue = if (!appUiState.isSideBarExpanded) 0f else 64f,
                animationSpec = tween(400)
            )
            val apiClient: ApiClient = koinInject()
            val backgroundOwner = appUiState.user?.steamId

            NavHost(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(animatedBlur.dp),
                contentAlignment = Alignment.Center,
                startDestination = Canvas
            ) {
                composable<Canvas> {
                    SteamPFB(backgroundOwner, apiClient)
                    CanvasScreen(
                        modifier = Modifier,
                        topNavModifier = Modifier.padding(PlatformSpecificDim.systemBarsPadding),
                        viewModel = canvasViewModel
                    )
                }
                composable<ChatGraph> { backStackEntry ->
                    val route = backStackEntry.toRoute<ChatGraph>()
                    SteamPFB(backgroundOwner, apiClient)

                    ChatScreen(
                        modifier = Modifier.fillMaxSize(),
                        systemBarsModifier = Modifier.padding(PlatformSpecificDim.systemBarsPadding),
                        viewModel = chatViewModel,
                        appViewModel = appViewModel,
                        selectedChatId = route.selectedChatId,
                        nestedNavHostController = rememberNavController()
                    )
                }
                composable<Friends> {
                    SteamPFB(backgroundOwner, apiClient)

                    FriendScreen(
                        modifier = Modifier
                            .fillMaxSize()
//                            .background(MaterialTheme.colorScheme.backgroundGradientBrush) // TODO
                            .padding(PlatformSpecificDim.systemBarsPadding),
                        viewModel = chatViewModel,
                        appViewModel = appViewModel,
                        onMessageClick = ::openChat
                    )
                }
                composable<Settings> {
                    val settingsNavController = rememberNavController()
                    LaunchedEffect(pendingSettings) {
                        val target = pendingSettings ?: return@LaunchedEffect
                        settingsNavController.popBackStack(OptionList, inclusive = false)
                        settingsNavController.navigate(target)
                        pendingSettings = null
                    }
                    SettingsScreen(
                        nestedNavController = settingsNavController,
                        modifier = Modifier.padding(PlatformSpecificDim.systemBarsPadding),
                        viewModel = settingsViewModel,
                        appViewModel = appViewModel
                    )
                }
                composable<Profile> {
                    ProfileScreen()
                }
            }

            val offsetY by animateDpAsState(
                targetValue = if (!appUiState.isBottomBarHidden) 0.dp else (120).dp,
                animationSpec = tween(240),
                label = "bottom_nav_offset",
            )

            ///////////////////////////////////////////////
            // Bottom Nav Bar
            ///////////////////////////////////////////////

            AnimatedVisibility(
                visible = !appUiState.isBottomBarHidden,
                enter = expandVertically(clip = false) + fadeIn(),
                exit = shrinkVertically(clip = false) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                NavBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(3f)
                        .padding(bottom = Dimensions.medium)
                        .graphicsLayer{ translationY = offsetY.toPx() },
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
            }
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
                        onAuthClick = {
                            if (appUiState.isLoggedIn) appViewModel.logout() else appViewModel.login()
                        },
                        isAuthInProgress = appUiState.isLoggingIn,
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

