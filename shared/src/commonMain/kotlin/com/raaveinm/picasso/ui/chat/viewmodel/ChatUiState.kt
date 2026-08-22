package com.raaveinm.picasso.ui.chat.viewmodel

import com.raaveinm.core.model.chat.Conversation

data class ChatUiState(
    val conversations: List<Conversation> = emptyList(),
    val selectedChat: Conversation? = null,
)
