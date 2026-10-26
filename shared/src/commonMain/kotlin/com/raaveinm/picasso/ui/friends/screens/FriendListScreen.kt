package com.raaveinm.picasso.ui.friends.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.raaveinm.core.model.social.ContactLevel
import com.raaveinm.picasso.ui.chat.viewmodel.ChatViewModel
import com.raaveinm.picasso.ui.friends.ContentWidth
import com.raaveinm.picasso.ui.friends.fragments.FriendList
import com.raaveinm.pickusall.core.designsystem.components.RefreshBox
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.no_artist_in_list

//
// Created by Kirill "Raaveinm" on 10/11/2026.
//

/**
 * Picasso contacts the user can talk to - friends first, then allies. Steam friends who are
 * not contacts live in Search / Contacts, where they can be sent an ally request.
 *
 * @param onMessageClick called with the local id of the DM to open.
 */
@Composable
fun FriendListScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    onMessageClick: (Long) -> Unit = {}
) {
    val social by viewModel.socialUiState.collectAsState()
    val friendsState by viewModel.friendsUiState.collectAsState()

    val contacts = remember(social.contacts) {
        social.contacts
            .filter { it.level.allowsCommunication }
            .sortedBy { if (it.level == ContactLevel.FRIEND) 0 else 1 }
            .mapNotNull { it.user }
    }

    RefreshBox(
        modifier = modifier.fillMaxSize(),
        onRefresh = { viewModel.refreshFriends() },
        isRefreshing = friendsState.isRefreshing
    ) {
        Box(contentAlignment = Alignment.Center) {
            FriendList(
                modifier = Modifier.sizeIn(maxWidth = ContentWidth + Dimensions.extraLarge),
                friendList = contacts,
                emptyPlaceholder = stringResource(Res.string.no_artist_in_list),
                onMessageClick = { friend -> viewModel.dmWith(friend.steamId, onMessageClick) }
            )
        }
    }
}
