package com.raaveinm.picasso.ui.friends.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.raaveinm.core.model.social.ContactLevel
import com.raaveinm.picasso.ui.chat.viewmodel.ChatViewModel
import com.raaveinm.pickusall.core.designsystem.components.AcceptChatRequest
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.incoming_requests_left
import pickusall.shared.generated.resources.no_incoming_requests

//
// Created by Kirill "Raaveinm" on 10/11/2026.
//

/**
 * Incoming ally requests, answered one at a time (oldest first): accept, decline (silent - the
 * sender is never told) or block. Outgoing requests are withdrawn from the Search screen.
 */
@Composable
fun FriendPendingScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel
) {
    val social by viewModel.socialUiState.collectAsState()
    val current = social.incomingRequests.minByOrNull { it.createdAtEpochMs }

    Column(
        modifier = modifier.fillMaxSize().padding(vertical = Dimensions.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.medium, Alignment.CenterVertically)
    ) {
        if (current == null) {
            Text(
                text = stringResource(Res.string.no_incoming_requests),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            return@Column
        }

        // keyed, so the block prompt of one request never carries over to the next
        key(current.steamId) {
            AcceptChatRequest(
                modifier = Modifier.weight(1f, fill = false),
                inviteeName = current.user?.personaName ?: current.steamId.toString(),
                inviteeIcon = current.user?.avatarFull,
                onAccept = { viewModel.acceptRequest(current.steamId) },
                onReject = { viewModel.declineRequest(current.steamId) },
                onBlock = { viewModel.setContactLevel(current.steamId, ContactLevel.IMPOSTER) }
            )
        }

        val left = social.incomingRequests.size - 1
        if (left > 0) {
            Text(
                text = stringResource(Res.string.incoming_requests_left, left),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
