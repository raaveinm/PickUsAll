package com.raaveinm.core.model.chat

import com.raaveinm.core.model.user.User

data class MessageData(
    val user: User,
    val textMessage: String,
    val timestamp: String
)
