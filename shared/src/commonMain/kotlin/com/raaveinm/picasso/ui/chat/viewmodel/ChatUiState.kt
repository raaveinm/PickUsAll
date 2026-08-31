package com.raaveinm.picasso.ui.chat.viewmodel

import com.raaveinm.core.model.chat.Conversation
import com.raaveinm.core.model.chat.MessageData
import com.raaveinm.core.model.user.User

data class ChatUiState(
    val conversations: List<Conversation> = emptyList(),
    val chatHistory: List<MessageData> = emptyList(),
    val isLoadingChatHistory: Boolean = false,
    val hasMoreChatHistory: Boolean = true,
    val selectedChat: Long? = null,
    val selectedUser: User? = null
)
