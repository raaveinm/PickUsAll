package com.raaveinm.picasso.ui.chat.fragments

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.raaveinm.core.model.chat.MessageData
import com.raaveinm.core.model.user.User
import com.raaveinm.pickusall.core.designsystem.components.Messages

@Composable
fun ChatMessages(
    modifier: Modifier = Modifier,
    messages: List<MessageData> = emptyList(),
    user: User
) {
    LazyColumn(
        modifier
    ) {
        itemsIndexed(messages) { index, message ->
            Messages(
                iconLink = message.user.avatarMedium,
                username = message.user.personaName,
                textMessage = message.textMessage,
                timestamp = message.timestamp,
                isSender = user == message.user,
                modifier = Modifier,
                previousExisted = index > 0 && messages[index - 1].user == message.user,
                isLast = index == messages.lastIndex || messages[index + 1].user != message.user
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatMessagesPreview() {
    val me = User(
        steamId = 1,
        communityVisibilityState = 3,
        personaName = "raaveinm",
        commentPermission = true,
        profileUrl = "",
        avatar = "",
        avatarMedium = "https://avatars.steamstatic.com/b606d0c9249cbeb8ed8ce1c57c0fd0f3c9058c79_medium.jpg",
        avatarFull = "",
        avatarHash = "",
        personaState = 1
    )
    val friend = User(
        steamId = 2,
        communityVisibilityState = 3,
        personaName = "Nick",
        commentPermission = true,
        profileUrl = "",
        avatar = "",
        avatarMedium = "https://avatars.steamstatic.com/6f4944ce1cd6bc9848125d6bc82d380853df9253_medium.jpg",
        avatarFull = "",
        avatarHash = "",
        personaState = 1
    )
    ChatMessages(
        user = me,
        messages = listOf(
            MessageData(user = friend, textMessage = "Mornin'", timestamp = "11:09"),
            MessageData(user = friend, textMessage = "<3", timestamp = "11:09"),
            MessageData(user = me, textMessage = "Mornin' <3", timestamp = "11:10"),
        )
    )
}