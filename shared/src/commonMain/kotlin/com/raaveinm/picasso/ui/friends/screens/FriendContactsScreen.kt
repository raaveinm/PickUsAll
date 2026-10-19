package com.raaveinm.picasso.ui.friends.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.raaveinm.core.model.social.ContactLevel
import com.raaveinm.core.model.user.User
import com.raaveinm.picasso.ui.chat.viewmodel.ChatViewModel
import com.raaveinm.picasso.ui.friends.ContentWidth
import com.raaveinm.picasso.ui.friends.elements.ArtistAliasAction
import com.raaveinm.picasso.ui.friends.elements.SectionHeader
import com.raaveinm.picasso.ui.friends.fragments.isOnline
import com.raaveinm.picasso.ui.friends.fragments.statusText
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.contacts_allies
import pickusall.shared.generated.resources.contacts_blocked
import pickusall.shared.generated.resources.contacts_friends
import pickusall.shared.generated.resources.contacts_incoming
import pickusall.shared.generated.resources.contacts_outgoing
import pickusall.shared.generated.resources.contacts_steam_friends

//
// Created by Kirill "Raaveinm" on 10/11/2026.
//

/** One block of the contact list: who is in it and which level button shows as current. */
private data class ContactSection(
    val title: StringResource,
    val key: String,
    val level: ContactLevel?,       // null = stranger (pending requests and plain Steam friends)
    val users: List<User>
)

/**
 * Everyone the user has a relationship with, grouped by it, each row carrying the four level
 * buttons (block / stranger / ally / friend) - `ChatViewModel.changeRelation` decides which
 * server call a tap turns into. The "Blocked" section is the blocklist: "stranger" there unblocks.
 */
@Composable
fun FriendContactList(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel
) {
    val social by viewModel.socialUiState.collectAsState()
    val friends by viewModel.friendsUiState.collectAsState()

    val sections = remember(social, friends.friends) {
        fun level(level: ContactLevel) = social.contacts.filter { it.level == level }.mapNotNull { it.user }
        val related = social.contacts.map { it.steamId }.toSet() +
            social.incomingRequests.map { it.steamId } +
            social.outgoingRequests.map { it.steamId }
        listOf(
            ContactSection(Res.string.contacts_friends, "friends", ContactLevel.FRIEND, level(ContactLevel.FRIEND)),
            ContactSection(Res.string.contacts_allies, "allies", ContactLevel.ALLY, level(ContactLevel.ALLY)),
            ContactSection(Res.string.contacts_incoming, "incoming", null, social.incomingRequests.mapNotNull { it.user }),
            ContactSection(Res.string.contacts_outgoing, "outgoing", null, social.outgoingRequests.mapNotNull { it.user }),
            ContactSection(Res.string.contacts_steam_friends, "steam", null, friends.friends.filter { it.steamId !in related }),
            ContactSection(Res.string.contacts_blocked, "blocked", ContactLevel.IMPOSTER, level(ContactLevel.IMPOSTER))
        )
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = Dimensions.medium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.small),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        sections.forEach { section ->
            contactSection(section) { user, level -> viewModel.changeRelation(user.steamId, level) }
        }
    }
}

private fun LazyListScope.contactSection(
    section: ContactSection,
    onSetLevel: (User, ContactLevel?) -> Unit
) {
    if (section.users.isEmpty()) return
    item(key = "${section.key}/header") { SectionHeader(text = stringResource(section.title)) }
    items(section.users, key = { "${section.key}/${it.steamId}" }) { user ->
        ArtistAliasAction(
            modifier = Modifier.sizeIn(maxWidth = ContentWidth + Dimensions.extraLarge).fillMaxWidth(),
            iconLink = user.avatarMedium,
            username = user.personaName,
            isOnline = user.isOnline(),
            status = user.statusText(),
            currentLevel = section.level,
            onSetStatusClicker = { level -> onSetLevel(user, level) }
        )
    }
}
