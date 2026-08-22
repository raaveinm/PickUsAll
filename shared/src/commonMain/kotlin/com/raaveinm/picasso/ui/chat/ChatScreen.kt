package com.raaveinm.picasso.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.raaveinm.picasso.ui.chat.fragments.AllChats
import com.raaveinm.picasso.ui.chat.fragments.UserActions
import com.raaveinm.picasso.ui.chat.viewmodel.ChatViewModel
import com.raaveinm.picasso.ui.navigation.ChatList
import com.raaveinm.picasso.ui.navigation.ChatWithUser
import com.raaveinm.picasso.ui.navigation.UserProfile

private val CompactWidthBreakpoint = 700.dp
private val ThirdColumnBreakpoint = 1100.dp
private val SidebarWidth = 320.dp

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = ChatViewModel(),
    selectedChatId: Long? = null,
    onGroupClick: (Long) -> Unit = {},
    nestedNavHostController: NavHostController
) {
    val state by viewModel.chatsUiState.collectAsState()

    LaunchedEffect(selectedChatId) {
        if (selectedChatId != null) viewModel.setSelectedChat(selectedChatId)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isCompact = maxWidth < CompactWidthBreakpoint
        val showThirdColumn = maxWidth >= ThirdColumnBreakpoint

        if (isCompact) {
            NavHost(
                navController = nestedNavHostController,
                startDestination = if (selectedChatId != null) ChatWithUser(chatId = selectedChatId) else ChatList
            ) {
                composable<ChatList> {
                    AllChats(
                        modifier = Modifier.fillMaxSize(),
                        conversations = state.conversations,
                        onChatClick = {
                            viewModel.setSelectedChat(it)
                            nestedNavHostController.navigate(ChatWithUser(chatId = it))
                        },
                        onGroupClick = onGroupClick
                    )
                }
                composable<ChatWithUser> { backStackEntry ->
                    val route = backStackEntry.toRoute<ChatWithUser>()
                    ChatWithUserScreen(
                        modifier = Modifier.fillMaxSize(),
                        chatId = route.chatId,
                        onBack = {
                            viewModel.setSelectedChat(null)
                            nestedNavHostController.popBackStack()
                        },
                        onUserIconClick = {
                            viewModel.setSelectedUser(it)
                            nestedNavHostController.navigate(UserProfile(userId = it))
                        },
                        messageData = emptyList()
                    )
                }
                composable<UserProfile> {
                    state.selectedUser?.let {
                        UserActions(
                            modifier = Modifier.fillMaxSize(),
                            user = it,
                            mostPlayed = listOf(),
                            onBack = { nestedNavHostController.popBackStack() },
                            showTopBar = true,
                            onAddToGroupClick = {}, //TODO(complete add group logic)
                            onMessageClick = { viewModel.setSelectedChat(state.selectedChat) }
                        )
                    }
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                AllChats(
                    modifier = Modifier.fillMaxHeight().width(SidebarWidth),
                    conversations = state.conversations,
                    onChatClick = { viewModel.setSelectedChat(it) },
                    onGroupClick = onGroupClick
                )
                AnimatedVisibility(
                    visible = state.selectedChat != null,
                    modifier = Modifier.fillMaxHeight().weight(1f)
                ) {
                    ChatWithUserScreen(
                        modifier = Modifier.fillMaxSize(),
                        chatId = state.selectedChat ?: -1,
                        onBack = { viewModel.setSelectedChat(null) },
                        onUserIconClick = { viewModel.setSelectedUser(it) },
                        emptyList()
                    )
                }
                if (showThirdColumn) {
                    AnimatedVisibility(
                        visible = state.selectedUser != null,
                        modifier = Modifier.fillMaxHeight().sizeIn(minWidth = 128.dp, maxWidth = SidebarWidth)
                    ) {
                        state.selectedUser?.let {
                            UserActions(
                                modifier = Modifier.fillMaxSize(),
                                user = it,
                                mostPlayed = listOf(),
                                onAddToGroupClick = {} // TODO(same)
                            )
                        }
                    }
                }
            }
        }
    }
}
