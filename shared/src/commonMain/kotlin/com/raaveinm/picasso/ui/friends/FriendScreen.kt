package com.raaveinm.picasso.ui.friends

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raaveinm.core.model.chat.Chat
import com.raaveinm.core.model.chat.Conversation
import com.raaveinm.core.model.chat.Palette
import com.raaveinm.core.model.user.User
import com.raaveinm.picasso.ui.chat.viewmodel.ChatViewModel
import com.raaveinm.picasso.ui.friends.fragments.FriendList
import com.raaveinm.picasso.ui.friends.fragments.isOnline
import com.raaveinm.pickusall.core.designsystem.components.PicassoSearchBar
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions

private val ContentWidth = 512.dp

/**
 * Everyone the current user shares a conversation with.
 *
 * @param onMessageClick called with the id of the DM to open. `null` when there is no
 * conversation with that friend yet.
 */
@Composable
fun FriendScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    onMessageClick: (Long?) -> Unit = {}
) {
    val state by viewModel.chatsUiState.collectAsState()
    val searchState = rememberTextFieldState()
    val query = searchState.text.toString()

    val friends = remember(state.conversations) { state.conversations.toFriends() }
    val shownFriends = remember(friends, query) {
        if (query.isBlank()) friends
        else friends.filter { it.personaName.contains(query.trim(), ignoreCase = true) }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = Dimensions.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PicassoSearchBar(
            modifier = Modifier.fillMaxWidth().sizeIn(maxWidth = ContentWidth)
                .padding(top = Dimensions.medium),
            textFieldState = searchState,
            placeholder = "search artists"
        )

        FriendList(
            modifier = Modifier.fillMaxSize().sizeIn(maxWidth = ContentWidth),
            friendList = shownFriends,
            emptyPlaceholder = if (query.isBlank()) "no artists around" else "nobody answers to \"$query\"",
            onMessageClick = { friend -> onMessageClick(state.conversations.dmWith(friend)) }
        )
    }
}

// TODO(replace with the Steam friend list once the API layer lands) - until then the roster is
//  everyone reachable through an existing conversation.
private fun List<Conversation>.toFriends(): List<User> = flatMap { conversation ->
    when (conversation) {
        is Chat -> listOf(conversation.chatTitle)
        is Palette -> conversation.members
    }
}.distinctBy { it.steamId }
    .sortedWith(compareByDescending<User> { it.isOnline() }.thenBy { it.personaName.lowercase() })

private fun List<Conversation>.dmWith(friend: User): Long? = filterIsInstance<Chat>()
    .firstOrNull { it.chatTitle.steamId == friend.steamId }?.id
