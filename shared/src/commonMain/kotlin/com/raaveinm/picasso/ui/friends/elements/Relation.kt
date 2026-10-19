package com.raaveinm.picasso.ui.friends.elements

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.raaveinm.core.model.social.ContactLevel
import com.raaveinm.picasso.ui.chat.viewmodel.SocialUiState
import com.raaveinm.picasso.ui.friends.ContentWidth
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions

//
// Created by Kirill "Raaveinm" on 10/11/2026.
//

/** Where another Artist stands with the local user, pending requests included (a [ContactLevel] has no room for those). */
enum class Relation { STRANGER, INCOMING, OUTGOING, ALLY, FRIEND, IMPOSTER }

fun SocialUiState.relationOf(steamId: Long): Relation {
    when (contacts.find { it.steamId == steamId }?.level) {
        ContactLevel.FRIEND -> return Relation.FRIEND
        ContactLevel.ALLY -> return Relation.ALLY
        ContactLevel.IMPOSTER -> return Relation.IMPOSTER
        null -> {}
    }
    return when {
        incomingRequests.any { it.steamId == steamId } -> Relation.INCOMING
        outgoingRequests.any { it.steamId == steamId } -> Relation.OUTGOING
        else -> Relation.STRANGER
    }
}

/**
 * The ally-request button that fits [relation]: "send" for a stranger (an accept, when they already
 * asked), "withdraw" for a request still waiting, nothing for contacts and blocked Artists.
 */
@Composable
fun RelationAction(
    relation: Relation,
    onRequest: () -> Unit,
    onWithdraw: () -> Unit
) {
    when (relation) {
        Relation.STRANGER, Relation.INCOMING ->
            MiniProfileButton(Icons.Outlined.PersonAdd, "mini_profile/ally_request", onRequest)
        Relation.OUTGOING ->
            MiniProfileButton(Icons.Outlined.PersonRemove, "mini_profile/withdraw_request", onWithdraw)
        Relation.ALLY, Relation.FRIEND, Relation.IMPOSTER -> {}
    }
}

@Composable
fun SectionHeader(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text,
        modifier = modifier.width(ContentWidth).padding(horizontal = Dimensions.small, vertical = Dimensions.extraSmall),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelLarge
    )
}
