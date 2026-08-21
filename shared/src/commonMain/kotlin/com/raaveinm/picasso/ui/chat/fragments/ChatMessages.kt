package com.raaveinm.picasso.ui.chat.fragments

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.raaveinm.core.model.chat.MessageData
import com.raaveinm.core.model.user.User
import com.raaveinm.picasso.data.mock.Mock
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

    ChatMessages(
        user = Mock.user_1,
        messages = listOf(
            MessageData(user = Mock.user_1, textMessage = "Mornin'", timestamp = "11:09"),
            MessageData(user = Mock.user_1, textMessage = "<3", timestamp = "11:09"),
            MessageData(user = Mock.user_2, textMessage = "Mornin' <3", timestamp = "11:10"),
        )
    )
}