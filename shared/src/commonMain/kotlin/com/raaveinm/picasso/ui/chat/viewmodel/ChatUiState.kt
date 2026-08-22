package com.raaveinm.picasso.ui.chat.viewmodel

import com.raaveinm.core.model.chat.Conversation
import com.raaveinm.core.model.user.User

data class ChatUiState(
    val conversations: List<Conversation> = emptyList(),
    val selectedChat: Long? = null,
    val selectedUser: User? = null
)
