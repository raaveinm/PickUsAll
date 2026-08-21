package com.raaveinm.core.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val chatTitle: String,
    val iconLink: String,
    val lastMessage: String? = null
)
