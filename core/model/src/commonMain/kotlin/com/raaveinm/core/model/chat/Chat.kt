package com.raaveinm.core.model.chat

import com.raaveinm.core.model.user.User
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    override val id: Long,
    val chatTitle: User,
    override val lastMessage: String? = null,
    override val listMessageData: List<MessageData> = emptyList()
) : Conversation
