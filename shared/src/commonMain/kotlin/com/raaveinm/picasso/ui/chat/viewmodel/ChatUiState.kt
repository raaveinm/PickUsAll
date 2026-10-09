package com.raaveinm.picasso.ui.chat.viewmodel

import com.raaveinm.core.model.chat.Conversation
import com.raaveinm.core.model.chat.MessageData
import com.raaveinm.core.model.social.ContactEntry
import com.raaveinm.core.model.social.ContactRequestEntry
import com.raaveinm.core.model.social.PaletteInviteEntry
import com.raaveinm.core.model.user.User
import com.raaveinm.picasso.data.sync.ConnectionState
import com.raaveinm.pickusall.core.designsystem.utils.WarnLevel

data class ChatUiState(
    val conversations: List<Conversation> = emptyList(),
    val chatHistory: List<MessageData> = emptyList(),
    val isLoadingChatHistory: Boolean = false,
    val hasMoreChatHistory: Boolean = true,
    val selectedChat: Long? = null,
    val selectedUser: User? = null,
    val warning: Pair<WarnLevel, String>? = null,
    val warningSeq: Int = 0,
    val selfSteamId: Long? = null,
    val connection: ConnectionState = ConnectionState.DISCONNECTED
)

data class SocialUiState(
    val contacts: List<ContactEntry> = emptyList(),
    val incomingRequests: List<ContactRequestEntry> = emptyList(),
    val outgoingRequests: List<ContactRequestEntry> = emptyList(),
    val paletteInvites: List<PaletteInviteEntry> = emptyList()
)
