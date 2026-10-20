package com.raaveinm.picasso.ui.friends.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.raaveinm.core.model.chat.Palette
import com.raaveinm.pickusall.core.designsystem.components.UserMiniProfile

//
// Created by Kirill "Raaveinm" on 10/19/2026.
//

@Composable
fun FriendGroupsScreen(
    modifier: Modifier = Modifier,
    groups: List<Palette>,
    onMessageClick: (Long) -> Unit = {},
    onGroupClick: (Long) -> Unit = {},
    onGroupCreate: () -> Unit = {},
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(items = groups, key = { it.id }) { g ->
            UserMiniProfile(
                modifier = Modifier,
                iconLink = "",
                username = g.name,
                isOnline = true,
                status = "",
                onMessageClick = { onMessageClick(0L) },
                hidePFP = false,
                hideActionButtons = false
            )
        }
    }
}