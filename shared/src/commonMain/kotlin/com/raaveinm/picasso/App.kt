package com.raaveinm.picasso

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.PicassoTheme
import com.raaveinm.pickusall.core.designsystem.utils.CoilInitializer
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

    PicassoTheme {
        CoilInitializer()

        var navigationSelected by remember { mutableStateOf(CanvasTab) }

        fun openTab(route: Any, tab: Int) {
            navController.navigate(route) {
                popUpTo<Canvas> { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            navigationSelected = tab
        }

        val gradientBrush = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.inverseOnSurface
            )
        )

        Box(Modifier.background(gradientBrush)) {
            NavHost(
                navController = navController,
                modifier = Modifier.fillMaxSize().systemBarsPadding(),
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
                        onMessageClick = { chatId ->
                            if (chatId != null) openTab(ChatGraph(chatId), ChatTab)
                        }
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
                selectedId = navigationSelected,
                onItemClick = {
                    val screen = when (it) {
                        CanvasTab -> Canvas
                        ChatTab -> ChatGraph()
                        FriendsTab -> Friends
                        SettingsTab -> Settings
                        else -> Canvas
                    }
                    openTab(screen, it)
                },
            )
        }
    }
}
