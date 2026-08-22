package com.raaveinm.picasso.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.raaveinm.picasso.data.mock.Mock
import com.raaveinm.pickusall.core.designsystem.components.ChatTopBar
import com.raaveinm.pickusall.core.designsystem.components.UserMiniProfile
import com.raaveinm.pickusall.core.designsystem.obj.UserStatus
import com.raaveinm.pickusall.core.designsystem.theme.Dimensions

@Composable
fun GroupScreen(
    modifier: Modifier = Modifier,
    groupId: Long,
    onBack: () -> Unit = {},
    onMemberClick: (Long) -> Unit = {}
) {
    val group = Mock.paletteList.firstOrNull { it.id == groupId }

    Column(modifier.fillMaxSize()) {
        if (group != null) {
            ChatTopBar(
                chatName = group.name,
                chatIcon = group.members.firstOrNull()?.avatarMedium,
                onGoBackAction = onBack
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(Dimensions.small),
                verticalArrangement = Arrangement.spacedBy(Dimensions.small)
            ) {
                items(group.members) { member ->
                    UserMiniProfile(
                        iconLink = member.avatarMedium,
                        username = member.personaName,
                        isOnline = member.personaState == 1 || member.personaState == 2,
                        status = UserStatus.getOnlineStatus(member.personaState),
                        onMessageClick = { onMemberClick(member.steamId) }
                    )
                }
            }
        }
    }
}
