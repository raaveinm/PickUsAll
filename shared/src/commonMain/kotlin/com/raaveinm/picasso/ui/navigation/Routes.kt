package com.raaveinm.picasso.ui.navigation

import kotlinx.serialization.Serializable

///////////////////////////////////////////////
// Global
///////////////////////////////////////////////

@Serializable
data object Canvas

/** Entry point of the nested chat graph. [selectedChatId] opens that chat straight away. */
@Serializable
data class ChatGraph(val selectedChatId: Long? = null)

@Serializable
data object Friends

@Serializable
data object Settings

///////////////////////////////////////////////
// Nested (chat graph)
///////////////////////////////////////////////

@Serializable
data object ChatList

@Serializable
data class ChatWithUser(val chatId: Long, val groupId: Long? = null)

@Serializable
data class Palette(val groupId: Long)

@Serializable
data class UserProfile(val userId: Long)

///////////////////////////////////////////////
// Nested (Settings)
///////////////////////////////////////////////

@Serializable
data object OptionList

@Serializable
data object Server

@Serializable
data object Application

@Serializable
data object Visual

@Serializable
data object Behaviour