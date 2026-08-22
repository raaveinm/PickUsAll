package com.raaveinm.picasso.ui.chat.viewmodel

import androidx.lifecycle.ViewModel
import com.raaveinm.picasso.data.mock.Mock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel : ViewModel() {
    private val _chatsUiState = MutableStateFlow(ChatUiState())
    val chatsUiState = _chatsUiState.asStateFlow()

    init {
        _chatsUiState.value = ChatUiState(
            conversations = Mock.conversationList,
            selectedChat = null
        )
    }

    fun setSelectedChat(id: Long?) {
        setSelectedUser(id)
        _chatsUiState.value = _chatsUiState.value.copy(selectedChat = id)
    }

    fun setSelectedUser(userId: Long?) {
        val user = Mock.userList.firstOrNull {
            it.steamId == userId
        }
        _chatsUiState.value = _chatsUiState.value.copy(selectedUser = user)
    }
}