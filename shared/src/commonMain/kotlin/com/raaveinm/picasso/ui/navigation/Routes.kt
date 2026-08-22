package com.raaveinm.picasso.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Canvas

@Serializable
data object ChatList

@Serializable
data class ChatWithUser(val chatId: Long, val groupId: Long? = null)

@Serializable
data class Group(val groupId: Long)

@Serializable
data class UserProfile(val userId: Long)
