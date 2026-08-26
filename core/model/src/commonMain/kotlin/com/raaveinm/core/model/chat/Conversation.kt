package com.raaveinm.core.model.chat

import kotlinx.serialization.Serializable

@Serializable
sealed interface Conversation {
    val id: Long
    val lastMessage: String?
    val listMessageData: List<MessageData>
}
