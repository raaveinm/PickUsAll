package com.raaveinm.picasso.ui.friends.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.raaveinm.core.model.user.User
import com.raaveinm.picasso.data.mock.Mock
import com.raaveinm.pickusall.core.designsystem.components.UserMiniProfile
import com.raaveinm.pickusall.core.designsystem.obj.UserStatus
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions

@Composable
fun FriendList(
    modifier: Modifier = Modifier,
    friendList: List<User>,
    emptyPlaceholder: String = "no artists around",
    onMessageClick: (User) -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = Dimensions.large),
        verticalArrangement = Arrangement.spacedBy(Dimensions.small)
    ) {
        if (friendList.isEmpty()) {
            item {
                Text(
                    text = emptyPlaceholder,
                    modifier = Modifier.fillMaxWidth().padding(Dimensions.large),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }

        items(friendList, key = { it.steamId }) { friend ->
            val status = remember(friend) { friend.statusText() }
            UserMiniProfile(
                modifier = Modifier.fillMaxWidth(),
                iconLink = friend.avatarMedium,
                username = friend.personaName,
                isOnline = friend.isOnline(),
                status = status,
                onMessageClick = { onMessageClick(friend) },
                onSteamProfileRedirectClick = { uriHandler.openUri(friend.profileUrl) }
            )
        }
    }
}

/** Steam treats `1` (online) and `2` (busy) as "reachable", everything else is dimmed out. */
internal fun User.isOnline(): Boolean = personaState == 1 || personaState == 2

internal fun User.statusText(): String = when {
    gameExtraInfo != null -> "in game - $gameExtraInfo"
    communityVisibilityState == 1 -> UserStatus.getPrivateProfileStatus()
    else -> UserStatus.getOnlineStatus(personaState)
}

@Preview
@Composable
fun FriendListPreview() {
    FriendList(friendList = Mock.userList)
}
