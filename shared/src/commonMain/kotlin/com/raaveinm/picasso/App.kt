package com.raaveinm.picasso

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.raaveinm.picasso.ui.chat.ChatScreen
import com.raaveinm.picasso.ui.chat.ChatWithUserScreen
import com.raaveinm.picasso.ui.chat.GroupScreen
import com.raaveinm.picasso.ui.navigation.Canvas
import com.raaveinm.picasso.ui.navigation.ChatList
import com.raaveinm.picasso.ui.navigation.ChatWithUser
import com.raaveinm.picasso.ui.navigation.Group
import com.raaveinm.picasso.ui.navigation.UserProfile
import com.raaveinm.pickusall.core.designsystem.components.NavBar
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.PicassoTheme
import com.raaveinm.pickusall.core.designsystem.utils.CoilInitializer

@Composable
@Preview
fun App(
    navController: NavHostController = rememberNavController()
) {
    PicassoTheme {
        CoilInitializer()

        var navigationSelected by remember { mutableStateOf(0) }

        val gradientBrush = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.inverseOnSurface
            )
        )

        Box {
            NavHost(
                navController = navController,
                modifier = Modifier.fillMaxSize().background(gradientBrush).systemBarsPadding(),
                contentAlignment = Alignment.Center,
                startDestination = Canvas
            ) {
                composable<Canvas> {
                    CanvasScreen(modifier = Modifier)
                }
                composable<ChatList> {
                    ChatScreen(
                        modifier = Modifier,
                        onChatClick = { chatId -> navController.navigate(ChatWithUser(chatId = chatId)) },
                        onGroupClick = { groupId -> navController.navigate(Group(groupId = groupId)) }
                    )
                }
                composable<Group> { backStackEntry ->
                    val route: Group = backStackEntry.toRoute()
                    GroupScreen(
                        modifier = Modifier,
                        groupId = route.groupId,
                        onBack = { navController.popBackStack() },
                        onMemberClick = { memberId ->
                            navController.navigate(ChatWithUser(chatId = memberId, groupId = route.groupId))
                        }
                    )
                }
                composable<ChatWithUser> { backStackEntry ->
                    val route: ChatWithUser = backStackEntry.toRoute()
                    ChatWithUserScreen(
                        modifier = Modifier,
                        chatId = route.chatId,
                        onBack = { navController.popBackStack() },
                        onUserIconClick = { userId -> navController.navigate(UserProfile(userId = userId)) }
                    )
                }
            }

            NavBar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = Dimensions.medium),
                nestedModifier = Modifier,
                fabModifier = Modifier,
                selectedId = navigationSelected,
                onItemClick = {
                    val screen = when (it) {
                        0 -> Canvas
                        1 -> ChatList
                        else -> Canvas
                    }
                    navController.navigate(screen)
                    navigationSelected = it
                },
            )
        }
    }
}
