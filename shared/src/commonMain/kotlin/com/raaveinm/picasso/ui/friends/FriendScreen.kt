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
import com.raaveinm.picasso.ui.chat.viewmodel.ChatViewModel
import com.raaveinm.picasso.ui.friends.fragments.FriendList
import com.raaveinm.pickusall.core.designsystem.components.PicassoSearchBar
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.search_bar_hint

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
    val state by viewModel.friendsUiState.collectAsState()
    val searchState = rememberTextFieldState()
    val query = searchState.text.toString()

    val friends = remember(state.friends) { state.friends }
    val shownFriends = remember(friends, query) {
        if (query.isBlank()) friends
        else friends.filter { it.personaName.contains(query.trim(), ignoreCase = true) }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = Dimensions.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PicassoSearchBar(
            modifier = Modifier.sizeIn(maxWidth = ContentWidth).fillMaxWidth()
                .padding(top = Dimensions.medium),
            textFieldState = searchState,
            placeholder = stringResource(Res.string.search_bar_hint)
        )

        FriendList(
            modifier = Modifier.fillMaxSize().sizeIn(maxWidth = ContentWidth),
            friendList = shownFriends,
            emptyPlaceholder = if (query.isBlank()) "no artists around" else "nobody answers to \"$query\"",
            onMessageClick = { friend -> onMessageClick(viewModel.dmWith(friend.steamId)) }
        )
    }
}
