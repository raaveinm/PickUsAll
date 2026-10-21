package com.raaveinm.picasso.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.raaveinm.core.model.social.ContactLevel
import com.raaveinm.picasso.ui.app.viewmodel.AppViewModel
import com.raaveinm.picasso.ui.chat.fragments.AllChats
import com.raaveinm.picasso.ui.chat.fragments.UserActions
import com.raaveinm.picasso.ui.chat.viewmodel.ChatViewModel
import com.raaveinm.picasso.ui.navigation.ChatList
import com.raaveinm.picasso.ui.navigation.ChatWithUser
import com.raaveinm.picasso.ui.navigation.Palette
import com.raaveinm.picasso.ui.navigation.UserProfile
import com.raaveinm.pickusall.core.designsystem.obj.ColorBrushes.backgroundGradientBrush
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import org.koin.compose.viewmodel.koinViewModel
import com.raaveinm.core.model.chat.Palette as PaletteConversation

private val CompactWidthBreakpoint = 700.dp
private val ThirdColumnBreakpoint = 1600.dp
private val SidebarWidth = 360.dp

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = koinViewModel(),
    appViewModel: AppViewModel,
    selectedChatId: Long? = null,
    nestedNavHostController: NavHostController
) {
    val state by viewModel.chatsUiState.collectAsState()
    val isInCall by viewModel.isInCall.collectAsState()

    LaunchedEffect(selectedChatId) {
        if (selectedChatId != null) viewModel.setSelectedChat(selectedChatId)
    }

    LaunchedEffect(state.warningSeq) {
        val warning = state.warning ?: return@LaunchedEffect
        appViewModel.postMessage(warning.first, warning.second)
        viewModel.consumeWarning()
    }

    LaunchedEffect(state.selectedChat) {
        if (state.selectedChat == null) appViewModel.showBottomBar() else appViewModel.hideBottomBar()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.backgroundGradientBrush)) {
        val isCompact = maxWidth < CompactWidthBreakpoint
        val showThirdColumn = maxWidth >= ThirdColumnBreakpoint

        if (isCompact) {
            NavHost(
                navController = nestedNavHostController,
                startDestination = if (selectedChatId != null) ChatWithUser(chatId = selectedChatId) else ChatList
            ) {
                composable<ChatList> {
                    val backStackEntry by nestedNavHostController.currentBackStackEntryFlow.collectAsState(initial = null)
                    LaunchedEffect(backStackEntry == null) { viewModel.setSelectedChat(null) }
                    AllChats(
                        modifier = modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.backgroundGradientBrush)
                            .padding(horizontal = Dimensions.small),
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
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.backgroundGradientBrush)
                            .padding(horizontal = Dimensions.medium),
                        systemBarModifier = modifier,
                        conversation = state.conversations.find { it.id == route.chatId },
                        onBack = {
                            viewModel.setSelectedChat(null)
                            nestedNavHostController.popBackStack()
                        },
                        onUserIconClick = { userId ->
                            if (route.groupId != null) {
                                nestedNavHostController.navigate(Palette(groupId = route.groupId))
                            } else {
                                viewModel.setSelectedUser(userId)
                                nestedNavHostController.navigate(UserProfile(userId = userId))
                            }
                        },
                        onLoadMoreHistory = { viewModel.retrieveChatHistory(route.chatId) },
                        onSendMessage = { text -> viewModel.sendMessage(route.chatId, text) },
                        onCallClicked = { viewModel.onCallClicked(route.chatId) },
                        onResumeDm = viewModel::resumeDm,
                        onDeclineDm = viewModel::declineFrozenDm,
                        onBlockDm = { viewModel.setContactLevel(it, ContactLevel.IMPOSTER) },
                        callState = isInCall,
                        messageData = state.chatHistory,
                        selfSteamId = state.selfSteamId,
                        onCopyMessageData = { viewModel.copyMessageChord(it) },
                        onDeleteMessages = { viewModel.deleteMessages(it) }
                    )
                }
                composable<UserProfile> { backStackEntry ->
                    val route = backStackEntry.toRoute<UserProfile>()
                    state.selectedUser?.let {
                        UserActions(
                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.backgroundGradientBrush),
                            systemBarModifier = modifier,
                            user = it,
                            mostPlayed = listOf(),
                            onBack = { nestedNavHostController.popBackStack() },
                            showTopBar = true,
                            onAddToGroupClick = {}, //TODO(complete add group logic)
                            onMessageClick = {
                                viewModel.dmWith(route.userId) { chatId ->
                                    viewModel.setSelectedChat(chatId)
                                    val returned = nestedNavHostController.popBackStack(
                                        route = ChatWithUser(chatId = chatId),
                                        inclusive = false
                                    )
                                    if (!returned) {
                                        nestedNavHostController.navigate(ChatWithUser(chatId = chatId))
                                    }
                                }
                            }
                        )
                    }
                }
                composable<Palette> { backStackEntry ->
                    val route = backStackEntry.toRoute<Palette>()
                    GroupScreen(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.backgroundGradientBrush),
                        group = state.conversations.filterIsInstance<PaletteConversation>().find { it.id == route.groupId },
                        onBack = { nestedNavHostController.popBackStack() },
                        onMemberClick = { userId ->
                            viewModel.setSelectedUser(userId)
                            nestedNavHostController.navigate(UserProfile(userId = userId))
                        }
                    )
                }
            }

            LaunchedEffect(selectedChatId) {
                if (selectedChatId == null) return@LaunchedEffect
                val current = nestedNavHostController.currentBackStackEntry
                val alreadyOpen = current?.destination?.hasRoute<ChatWithUser>() == true &&
                        current.toRoute<ChatWithUser>().chatId == selectedChatId
                if (!alreadyOpen) {
                    nestedNavHostController.navigate(ChatWithUser(chatId = selectedChatId))
                }
            }
        } else {
            val selectedPalette = state.conversations
                .filterIsInstance<PaletteConversation>()
                .firstOrNull { it.id == state.selectedChat }

            Row(modifier = Modifier.fillMaxSize()) {
                Card(Modifier.padding(Dimensions.medium)) {
                    AllChats(
                        modifier = Modifier.fillMaxHeight().width(SidebarWidth).padding(Dimensions.small),
                        conversations = state.conversations,
                        onChatClick = { viewModel.setSelectedChat(it) },
                        onGroupClick = { viewModel.setSelectedChat(it) },
                        selectedChatId = state.selectedChat
                    )
                }
                AnimatedVisibility(
                    visible = state.selectedChat != null,
                    modifier = Modifier.fillMaxHeight().weight(1f)
                ) {
                    ChatWithUserScreen(
                        modifier = Modifier.fillMaxSize().padding(horizontal = Dimensions.large),
                        conversation = state.conversations.find { it.id == state.selectedChat },
                        onBack = { viewModel.setSelectedChat(null) },
                        onUserIconClick = { userId ->
                            viewModel.setSelectedUser(if (selectedPalette != null) null else userId)
                        },
                        onLoadMoreHistory = { state.selectedChat?.let(viewModel::retrieveChatHistory) },
                        onSendMessage = { text -> state.selectedChat?.let { viewModel.sendMessage(it, text) } },
                        onCallClicked = { state.selectedChat?.let { viewModel.onCallClicked(it) } },
                        onResumeDm = viewModel::resumeDm,
                        onDeclineDm = viewModel::declineFrozenDm,
                        onBlockDm = { viewModel.setContactLevel(it, ContactLevel.IMPOSTER) },
                        callState = isInCall,
                        messageData = state.chatHistory,
                        selfSteamId = state.selfSteamId,
                        onCopyMessageData = { viewModel.copyMessageChord(it) },
                        onDeleteMessages = { viewModel.deleteMessages(it) }
                    )
                }
                if (showThirdColumn) {
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
                                onAddToGroupClick = {}, // TODO(same)
                                // opening the DM in the middle column also loads its history
                                onMessageClick = {
                                    viewModel.dmWith(selectedUser.steamId, viewModel::setSelectedChat)
                                }
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
