package com.raaveinm.picasso.ui.friends.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.DoNotDisturbOn
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raaveinm.core.model.chat.Palette
import com.raaveinm.core.model.user.User
import com.raaveinm.pickusall.core.designsystem.components.PicassoSearchBar
import com.raaveinm.pickusall.core.designsystem.components.UserMiniProfile
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import com.raaveinm.pickusall.core.designsystem.theme.SpecialColors
import org.jetbrains.compose.resources.stringResource
import pickusall.shared.generated.resources.Res
import pickusall.shared.generated.resources.add_user_to_group
import pickusall.shared.generated.resources.create_group
import pickusall.shared.generated.resources.group_name

//
// Created by Kirill "Raaveinm" on 10/19/2026.
//


///////////////////////////////////////////////
// Common
///////////////////////////////////////////////
@Composable
fun FriendGroupsScreen(
    modifier: Modifier = Modifier,
    groups: List<Palette>,
    candidates: List<User> = emptyList(), // contacts that can be invited (the server only accepts allies)
    onMessageClick: (Long) -> Unit = {},
    onGroupCreate: (Palette, List<User>) -> Unit,
) {
    var isNewGroupCreationMode by remember { mutableStateOf(false) }
    Box(modifier) {
        val blurAnimation by animateDpAsState(
            targetValue = if (isNewGroupCreationMode) 64.dp else 0.dp,
            animationSpec = tween(durationMillis = 200),
            label = "animate_blur_transition",
        )

        MainContent(
            modifier =
                if (isNewGroupCreationMode) Modifier.fillMaxSize().blur(radius = blurAnimation)
                else Modifier.fillMaxSize(),
            groups = groups,
            onMessageClick = onMessageClick,
            onGroupCreate = { isNewGroupCreationMode = true }
        )

        AnimatedVisibility(
            visible = isNewGroupCreationMode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CreateNewGroup(
                modifier = Modifier.fillMaxSize().align(Alignment.Center),
                candidates = candidates,
                onGroupCreate = { group, invitees ->
                    onGroupCreate(group, invitees)
                    isNewGroupCreationMode = false
                },
                onGroupCancel = { isNewGroupCreationMode = false }
            )
        }
    }
}

///////////////////////////////////////////////
// Main Content
///////////////////////////////////////////////

@Preview
@Composable
private fun MainContent(
    modifier: Modifier = Modifier,
    groups: List<Palette> = emptyList(),
    onMessageClick: (Long) -> Unit = {},
    onGroupCreate: () -> Unit = {},
) {
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.small),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = Dimensions.large)
        ) {
            items(items = groups, key = { it.id }) { g ->
                UserMiniProfile(
                    modifier = Modifier.sizeIn(maxWidth = 512.dp).clickable { onMessageClick(g.id) },
                    iconLink = "",
                    username = g.name,
                    isOnline = true,
                    status = "",
                    hidePFP = false,
                    hideActionButtons = true
                )
            }
            item {
                Card(modifier = Modifier
                    .sizeIn(maxWidth = 512.dp)
                    .fillMaxWidth()
                    .height(72.dp)
                ) {
                    Box(
                        Modifier
                            .padding(Dimensions.extraSmall)
                            .fillMaxSize()
                            .clip(Shapes.roundedSmall)
                            .clickable { onGroupCreate() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "create_new_group",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

///////////////////////////////////////////////
// New Group Creation
///////////////////////////////////////////////

@Preview
@Composable
private fun CreateNewGroup(
    modifier: Modifier = Modifier,
    onGroupCreate: (Palette, List<User>) -> Unit = { _, _ -> },
    onGroupCancel: () -> Unit = {},
    candidates: List<User> = emptyList(),
) {
    // the creator is implicit: the server adds the caller as the palette's owner
    val groupMembers = remember { mutableStateListOf<User>() }

    Box(modifier.clickable(interactionSource = MutableInteractionSource(), indication = null) { onGroupCancel() },
        contentAlignment = Alignment.Center
    ) {
        Card(modifier = Modifier.width(528.dp).clickable(indication = null, interactionSource = MutableInteractionSource()) {}) {
            Column(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimensions.medium)
            ) {
                val textFieldState = rememberTextFieldState()
                val searchTextField = rememberTextFieldState()
                var isInitialized by remember { mutableStateOf(false) }

                // candidates matching the query that aren't invited yet; at most 3 are shown
                val query: String = searchTextField.text.toString().trim()
                val foundedUsers: List<User> = candidates
                    .filter { candidate ->
                        candidate !in groupMembers &&
                            (query.isEmpty() || candidate.personaName.contains(query, ignoreCase = true))
                    }
                    .take(3)

                OutlinedTextField(
                    state = textFieldState,
                    modifier = Modifier.fillMaxWidth().padding(PaddingValues(all = Dimensions.medium)),
                    label = { Text(stringResource(Res.string.group_name)) },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = textFieldState.text.isNotEmpty(),
                            enter = expandHorizontally(expandFrom = Alignment.Start)
                                    + fadeIn(animationSpec = tween(durationMillis = 200)),
                            exit = shrinkHorizontally(shrinkTowards = Alignment.Start)
                                    + fadeOut(animationSpec = tween(durationMillis = 200))
                        ) {
                            isInitialized = true
                            Box(Modifier.size(32.dp).clickable { textFieldState.clearText() }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "clear_text",
                                    modifier = Modifier.align(Alignment.Center),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    isError = (textFieldState.text.isEmpty() && isInitialized) || textFieldState.text.length > 24
                )

                PicassoSearchBar(
                    modifier = Modifier.fillMaxWidth().padding(PaddingValues(all = Dimensions.medium)),
                    textFieldState = searchTextField,
                    placeholder = stringResource(Res.string.add_user_to_group),
                    backgroundColor = MaterialTheme.colorScheme.inverseOnSurface
                )

                LazyColumn(
                    modifier = Modifier.padding(Dimensions.small).sizeIn(maxWidth = 512.dp),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.extraSmall)
                ) {
                    items(items = foundedUsers, key = { it.steamId }) { member ->
                        UserMiniProfile(
                            modifier = Modifier,
                            iconLink = member.avatarFull,
                            username = member.personaName,
                            isOnline = member.personaState == 1 || member.personaState == 2,
                            status = if (member.personaState == 1 || member.personaState == 2) "online" else "offline",
                            hidePFP = true,
                            actionButtons = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "add_member",
                                    modifier = Modifier
                                        .padding(Dimensions.sMedium)
                                        .size(24.dp)
                                        .clickable { groupMembers.add(member) },
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.padding(Dimensions.small).sizeIn(maxWidth = 512.dp),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.extraSmall)
                ) {
                    items(groupMembers) { member ->
                        UserMiniProfile(
                            modifier = Modifier,
                            iconLink = member.avatarFull,
                            username = member.personaName,
                            isOnline = member.personaState == 1 || member.personaState == 2,
                            status = if (member.personaState == 1 || member.personaState == 2) "online" else "offline",
                            hidePFP = true,
                            actionButtons = {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .padding(vertical = Dimensions.sMedium)
                                        .size(36.dp)
                                        .clip(Shapes.circleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DoneAll,
                                        contentDescription = "add_member",
                                        modifier = Modifier,
                                        tint = SpecialColors.onlineColor
                                    )
                                }
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .padding(all = Dimensions.sMedium)
                                        .size(36.dp)
                                        .clip(Shapes.circleShape)
                                        .clickable { groupMembers.remove(element = member) },
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DoNotDisturbOn,
                                        contentDescription = "add_member",
                                        tint = SpecialColors.offlineColor
                                    )
                                }
                            }
                        )
                    }
                }

                val groupName: String = textFieldState.text.toString().trim()
                Button(
                    modifier = Modifier.padding(bottom = Dimensions.medium),
                    enabled = groupName.isNotEmpty() && groupName.length <= 24,
                    onClick = {
                        onGroupCreate(
                            Palette(id = 0L, name = groupName, members = groupMembers.toList()), // id is assigned by the server
                            groupMembers.toList()
                        )
                    }
                ) {
                    Text(stringResource(Res.string.create_group))
                }
            }
        }
    }
}