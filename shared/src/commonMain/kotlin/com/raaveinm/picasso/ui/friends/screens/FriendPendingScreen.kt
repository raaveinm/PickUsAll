package com.raaveinm.picasso.ui.friends.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.raaveinm.core.model.social.ContactLevel
import com.raaveinm.picasso.ui.chat.viewmodel.ChatViewModel
import com.raaveinm.picasso.ui.friends.ContentWidth
import com.raaveinm.pickusall.core.designsystem.components.AcceptChatRequest
import com.raaveinm.pickusall.core.designsystem.components.UserMiniProfile
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import com.raaveinm.pickusall.core.designsystem.theme.SpecialColors
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.incoming_requests_left
import pickusall.shared.generated.resources.no_incoming_requests
import pickusall.shared.generated.resources.palette_invite_from

//
// Created by Kirill "Raaveinm" on 10/11/2026.
//

/**
 * Incoming ally requests, answered one at a time (oldest first): accept, decline (silent - the
 * sender is never told) or block. Outgoing requests are withdrawn from the Search screen.
 *
 * Palette (group) invitations sent by the server are listed below the request; each one is
 * accepted or declined on its own, so they don't wait behind the ally requests.
 */
@Composable
fun FriendPendingScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel
) {
    val social by viewModel.socialUiState.collectAsState()
    val current = social.incomingRequests.minByOrNull { it.createdAtEpochMs }
    val invites = social.paletteInvites.sortedBy { it.createdAtEpochMs }

    Column(
        modifier = modifier.fillMaxSize().padding(vertical = Dimensions.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.medium, Alignment.CenterVertically)
    ) {
        if (current == null && invites.isEmpty()) {
            Text(
                text = stringResource(Res.string.no_incoming_requests),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            return@Column
        }

        if (current != null) {
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

        if (invites.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.sizeIn(maxWidth = ContentWidth).weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(Dimensions.small)
            ) {
                items(items = invites, key = { it.conversationRemoteId }) { invite ->
                    UserMiniProfile(
                        modifier = Modifier,
                        iconLink = invite.inviter?.avatarFull ?: "",
                        username = invite.name,
                        isOnline = false,
                        status = stringResource(
                            Res.string.palette_invite_from,
                            invite.inviter?.personaName ?: invite.inviterSteamId.toString()
                        ),
                        hidePFP = true,
                        actionButtons = {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(vertical = Dimensions.sMedium)
                                    .size(36.dp)
                                    .clip(Shapes.circleShape)
                                    .clickable { viewModel.declinePaletteInvite(invite.conversationRemoteId) }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "decline_palette_invite",
                                    tint = SpecialColors.offlineColor
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(all = Dimensions.sMedium)
                                    .size(36.dp)
                                    .clip(Shapes.circleShape)
                                    .clickable { viewModel.acceptPaletteInvite(invite.conversationRemoteId) }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = "accept_palette_invite",
                                    tint = SpecialColors.onlineColor
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
