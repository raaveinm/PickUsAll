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
import com.raaveinm.picasso.ui.navigation.Palette
import com.raaveinm.picasso.ui.navigation.UserProfile
import org.koin.compose.viewmodel.koinViewModel
import com.raaveinm.core.model.chat.Chat
import com.raaveinm.core.model.chat.Palette as PaletteConversation

private val CompactWidthBreakpoint = 700.dp
private val ThirdColumnBreakpoint = 1100.dp
private val SidebarWidth = 320.dp

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = koinViewModel(),
    selectedChatId: Long? = null,
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
                        onGroupClick = {
                            viewModel.setSelectedChat(it)
                            nestedNavHostController.navigate(ChatWithUser(chatId = it, groupId = it))
                        }
                    )
                }
                composable<ChatWithUser> { backStackEntry ->
                    val route = backStackEntry.toRoute<ChatWithUser>()
                    ChatWithUserScreen(
                        modifier = Modifier.fillMaxSize(),
                        chat = state.conversations.filterIsInstance<Chat>().find { it.id == route.chatId },
                        onBack = {
                            viewModel.setSelectedChat(null)
                            nestedNavHostController.popBackStack()
                        },
                        // in a palette the icon leads to the member list, in a DM straight to the profile
                        onUserIconClick = { userId ->
                            if (route.groupId != null) {
                                nestedNavHostController.navigate(Palette(groupId = route.groupId))
                            } else {
                                viewModel.setSelectedUser(userId)
                                nestedNavHostController.navigate(UserProfile(userId = userId))
                            }
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
                composable<Palette> { backStackEntry ->
                    val route = backStackEntry.toRoute<Palette>()
                    GroupScreen(
                        modifier = Modifier.fillMaxSize(),
                        group = state.conversations.filterIsInstance<PaletteConversation>().find { it.id == route.groupId },
                        onBack = { nestedNavHostController.popBackStack() },
                        onMemberClick = { userId ->
                            viewModel.setSelectedUser(userId)
                            nestedNavHostController.navigate(UserProfile(userId = userId))
                        }
                    )
                }
            }
        } else {
            val selectedPalette = state.conversations
                .filterIsInstance<PaletteConversation>()
                .firstOrNull { it.id == state.selectedChat }

            Row(modifier = Modifier.fillMaxSize()) {
                AllChats(
                    modifier = Modifier.fillMaxHeight().width(SidebarWidth),
                    conversations = state.conversations,
                    onChatClick = { viewModel.setSelectedChat(it) },
                    onGroupClick = { viewModel.setSelectedChat(it) }
                )
                AnimatedVisibility(
                    visible = state.selectedChat != null,
                    modifier = Modifier.fillMaxHeight().weight(1f)
                ) {
                    ChatWithUserScreen(
                        modifier = Modifier.fillMaxSize(),
                        chat = state.conversations.filterIsInstance<Chat>().find { it.id == state.selectedChat },
                        onBack = { viewModel.setSelectedChat(null) },
                        onUserIconClick = { viewModel.setSelectedUser(it) },
                        messageData = emptyList()
                    )
                }
                if (showThirdColumn) {
                    // the trailing column shows the selected member, or the palette roster while
                    // nobody is picked - a DM always has its user selected, so it never lands here
                    AnimatedVisibility(
                        visible = state.selectedUser != null || selectedPalette != null,
                        modifier = Modifier.fillMaxHeight().sizeIn(minWidth = 128.dp, maxWidth = SidebarWidth)
                    ) {
                        val selectedUser = state.selectedUser
                        when {
                            selectedUser != null -> UserActions(
                                modifier = Modifier.fillMaxSize(),
                                user = selectedUser,
                                mostPlayed = listOf(),
                                onBack = { viewModel.setSelectedUser(null) },
                                onAddToGroupClick = {} // TODO(same)
                            )
                            selectedPalette != null -> GroupScreen(
                                modifier = Modifier.fillMaxSize(),
                                group = selectedPalette,
                                onBack = { viewModel.setSelectedChat(null) },
                                onMemberClick = { viewModel.setSelectedUser(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
