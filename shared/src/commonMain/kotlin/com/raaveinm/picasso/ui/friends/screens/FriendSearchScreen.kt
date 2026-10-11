package com.raaveinm.picasso.ui.friends.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import com.raaveinm.core.model.user.User
import com.raaveinm.picasso.ui.chat.viewmodel.ChatViewModel
import com.raaveinm.picasso.ui.chat.viewmodel.SocialUiState
import com.raaveinm.picasso.ui.friends.ContentWidth
import com.raaveinm.picasso.ui.friends.elements.RelationAction
import com.raaveinm.picasso.ui.friends.elements.SectionHeader
import com.raaveinm.picasso.ui.friends.elements.relationOf
import com.raaveinm.picasso.ui.friends.fragments.isOnline
import com.raaveinm.picasso.ui.friends.fragments.statusText
import com.raaveinm.pickusall.core.designsystem.components.PicassoSearchBar
import com.raaveinm.pickusall.core.designsystem.components.UserMiniProfile
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.contacts_outgoing
import pickusall.shared.generated.resources.contacts_steam_friends
import pickusall.shared.generated.resources.search_found_on_steam
import pickusall.shared.generated.resources.search_known_artists
import pickusall.shared.generated.resources.search_not_found
import pickusall.shared.generated.resources.search_profile_hint
import pickusall.shared.generated.resources.search_searching

//
// Created by Kirill "Raaveinm" on 10/11/2026.
//

/** Typing pause before a query goes to Steam - one lookup per word, not per keystroke. */
private const val SEARCH_DEBOUNCE_MS: Long = 500L

/**
 * Finding Artists to ally with.
 *
 * - blank query: the user's Steam friends (to send an ally request) and the requests still waiting (to withdraw)
 * - a query: known Artists whose name matches, plus the Steam profile the query resolves to
 *   (SteamID64 / profile URL / vanity name - see `ProfilesRepository.lookup`)
 *
 * Messaging a stranger asks them to be allies on its own (`ChatViewModel.dmWith`).
 *
 * @param onMessageClick called with the local id of the DM to open.
 */
@Composable
fun FriendSearchScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    onMessageClick: (Long) -> Unit = {}
) {
    //TODO(search by display name: Steam's Web API has none, only the community page's SearchCommunityAjax)
    val searchState = rememberTextFieldState()
    val query = searchState.text.toString().trim()

    val search by viewModel.searchUiState.collectAsState()
    val social by viewModel.socialUiState.collectAsState()
    val friends by viewModel.friendsUiState.collectAsState()

    LaunchedEffect(query) {
        if (query.isNotEmpty()) delay(SEARCH_DEBOUNCE_MS)
        viewModel.searchArtist(query)
    }

    // everyone already cached: Steam friends + Picasso contacts + people with pending requests
    val known = remember(friends.friends, social) {
        (friends.friends +
            social.contacts.mapNotNull { it.user } +
            social.incomingRequests.mapNotNull { it.user } +
            social.outgoingRequests.mapNotNull { it.user })
            .distinctBy { it.steamId }
    }
    val knownMatches = remember(known, query) {
        if (query.isEmpty()) emptyList()
        else known.filter { it.personaName.contains(query, ignoreCase = true) }
    }
    val outgoing = remember(social.outgoingRequests) { social.outgoingRequests.mapNotNull { it.user } }

    val rowActions = ArtistRowActions(
        social = social,
        onRequest = { viewModel.requestContact(it.steamId) },
        onWithdraw = { viewModel.declineRequest(it.steamId) },
        onMessage = { viewModel.dmWith(it.steamId, onMessageClick) }
    )

    val outgoingTitle = stringResource(Res.string.contacts_outgoing)
    val steamFriendsTitle = stringResource(Res.string.contacts_steam_friends)
    val knownTitle = stringResource(Res.string.search_known_artists)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        PicassoSearchBar(
            modifier = Modifier
                .zIndex(zIndex = .5f)
                .sizeIn(maxWidth = ContentWidth)
                .fillMaxWidth()
                .padding(top = Dimensions.large),
            textFieldState = searchState,
            placeholder = stringResource(Res.string.search_profile_hint)
        )
        LazyColumn(
            modifier = Modifier.sizeIn(maxWidth = ContentWidth + Dimensions.extraLarge).fillMaxSize(),
            contentPadding = PaddingValues(top = Dimensions.paddingAboveAverage, bottom = Dimensions.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.small)
        ) {
            if (query.isEmpty()) {
                artistSection(outgoingTitle, outgoing, rowActions, "outgoing")
                artistSection(steamFriendsTitle, friends.friends, rowActions, "steam")
            } else {
                artistSection(knownTitle, knownMatches, rowActions, "known")
                item(key = "steam_header") { SectionHeader(text = stringResource(Res.string.search_found_on_steam)) }
                val found = search.result.takeIf { search.query == query && !search.isSearching }
                when {
                    found != null -> item(key = "steam_result/${found.steamId}") { ArtistRow(found, rowActions) }
                    else -> item(key = "steam_status") {
                        Text(
                            text = stringResource(
                                if (search.notFound && search.query == query) Res.string.search_not_found
                                else Res.string.search_searching
                            ),
                            modifier = Modifier.fillMaxWidth().padding(Dimensions.large),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

///////////////////////////////////////////////
/// Rows
///////////////////////////////////////////////

private class ArtistRowActions(
    val social: SocialUiState,
    val onRequest: (User) -> Unit,
    val onWithdraw: (User) -> Unit,
    val onMessage: (User) -> Unit
)

/** A header and one row per Artist; nothing at all when [users] is empty. [scope] keeps keys unique across sections. */
private fun LazyListScope.artistSection(
    title: String,
    users: List<User>,
    actions: ArtistRowActions,
    scope: String
) {
    if (users.isEmpty()) return
    item(key = "$scope/header") { SectionHeader(text = title) }
    items(users, key = { "$scope/${it.steamId}" }) { user -> ArtistRow(user, actions) }
}

@Composable
private fun ArtistRow(user: User, actions: ArtistRowActions) {
    val uriHandler = LocalUriHandler.current
    val relation = actions.social.relationOf(user.steamId)
    UserMiniProfile(
        modifier = Modifier.fillMaxWidth(),
        iconLink = user.avatarMedium,
        username = user.personaName,
        isOnline = user.isOnline(),
        status = user.statusText(),
        onMessageClick = { actions.onMessage(user) },
        onSteamProfileRedirectClick = { uriHandler.openUri(user.profileUrl) },
        leadingActions = {
            RelationAction(
                relation = relation,
                onRequest = { actions.onRequest(user) },
                onWithdraw = { actions.onWithdraw(user) }
            )
        }
    )
}
