package com.raaveinm.picasso.ui.chat.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.core.model.lib.GameInfo
import com.raaveinm.core.model.user.User
import com.raaveinm.picasso.data.mock.Mock
import com.raaveinm.pickusall.core.designsystem.components.ChatTopBar
import com.raaveinm.pickusall.core.designsystem.components.GameCard
import com.raaveinm.pickusall.core.designsystem.components.UserMiniProfile
import com.raaveinm.pickusall.core.designsystem.components.UserQuickAction
import com.raaveinm.pickusall.core.designsystem.obj.UserStatus
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions
import com.raaveinm.pickusall.core.designsystem.theme.Shapes
import com.raaveinm.pickusall.core.designsystem.utils.ImageUtility

@Composable
fun UserActions(
    modifier: Modifier = Modifier,
    user: User,
    mostPlayed: List<GameInfo> = emptyList(),
    onBack: () -> Unit = {},
    showTopBar: Boolean = false,
    onAddToGroupClick: () -> Unit = {},
    onMessageClick: () -> Unit = {}
) {
    Box(
        modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        if (showTopBar) {
            ChatTopBar(
                modifier = Modifier.zIndex(1f),
                chatName = user.personaName,
                chatIcon = user.avatarMedium,
                onGoBackAction = onBack,
                hideUserIcon = true
            )
        }
        LazyColumn(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimensions.sMedium),
            contentPadding = PaddingValues(vertical = Dimensions.extraLarge)
        ) {
            ///////////////////////////////////////////////
            // PFP
            ///////////////////////////////////////////////
            item {
                ImageUtility(
                    imageUrl = user.avatarFull,
                    modifier = Modifier.aspectRatio(1f),
                    contentDescription = "chat/${user.personaName}/actions",
                    contentScale = ContentScale.Crop,
                    enableGlowEffect = true,
                    cornerRadius = Shapes.averageShape
                )
            }
            ///////////////////////////////////////////////
            // Actions
            ///////////////////////////////////////////////
            item {
                val uriHandler = LocalUriHandler.current
                UserQuickAction(
                    modifier = Modifier,
                    onMessageClick = onMessageClick,
                    onAddToGroupClick = onAddToGroupClick,
                    onSteamProfileRedirectClick = { uriHandler.openUri(user.profileUrl) },
                )
            }
            ///////////////////////////////////////////////
            // username
            ///////////////////////////////////////////////
            item {
                DecoratedText(
                    text = user.personaName.uppercase(),
                    typography = MaterialTheme.typography.titleLarge
                )
                HorizontalDivider(Modifier.fillMaxWidth().padding(horizontal = Dimensions.large))
                if (user.realName != null) {
                    DecoratedText(
                        text = user.realName ?: "",
                        typography = MaterialTheme.typography.labelSmall
                    )
                }
            }
            ///////////////////////////////////////////////
            // Mini profile (online status)
            ///////////////////////////////////////////////
            item {
                UserMiniProfile(
                    modifier = Modifier.sizeIn(maxWidth = 512.dp)
                        .padding(horizontal = Dimensions.medium),
                    iconLink = user.avatarFull,
                    username = user.personaName,
                    isOnline = user.personaState == 1 || user.personaState == 2,
                    status = UserStatus.getOnlineStatus(user.personaState), // TODO(handle in game case by passing game name)
                    hideActionButtons = true
                )
            }
            ///////////////////////////////////////////////
            // Most Played
            ///////////////////////////////////////////////
            item {
                DecoratedText(
                    text =
                        if (user.communityVisibilityState == 1) UserStatus.getPrivateProfileStatus()
                        else "Most Played",
                    typography = MaterialTheme.typography.titleMedium
                )
                LazyRow(Modifier.fillMaxWidth().sizeIn(maxHeight = 256.dp)) {
                    items(3) {
                        val game = mostPlayed.getOrNull(it)
                        if (game != null) {
                            GameCard(
                                modifier = Modifier,
                                gameId = game.appId,
                                text = "${if (game.playtimeForever / 60 > 0) "${game.playtimeForever / 60}h " else ""}${game.playtimeForever % 60}m",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DecoratedText(typography: TextStyle, text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth(),
        fontSize = typography.fontSize,
        fontStyle = typography.fontStyle,
        fontWeight = typography.fontWeight,
        textAlign = TextAlign.Center,
        softWrap = true,
        maxLines = 2,
        minLines = 1
    )
}

@Preview(showBackground = true)
@Composable
fun UserActionsPreview() {
    UserActions(user = Mock.user_1,
        mostPlayed = Mock.mostPlayedGames)
}