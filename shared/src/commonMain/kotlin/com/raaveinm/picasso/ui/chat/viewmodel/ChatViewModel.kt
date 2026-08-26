package com.raaveinm.picasso.ui.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raaveinm.core.database.dao.ChatDao
import com.raaveinm.core.database.entities.chat.toDto
import com.raaveinm.core.model.chat.Chat
import com.raaveinm.core.model.chat.Palette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class ChatViewModel(chatDao: ChatDao) : ViewModel() {
    private val _chatsUiState = MutableStateFlow(ChatUiState())
    val chatsUiState = _chatsUiState.asStateFlow()

    init {
        combine(chatDao.observeChats(), chatDao.observePalettes()) { chats, palettes ->
            chats.map { it.toDto() } + palettes.map { it.toDto() }
        }.onEach { conversations ->
            _chatsUiState.update { it.copy(conversations = conversations) }
        }.launchIn(viewModelScope)
    }

    fun setSelectedChat(id: Long?) {
        _chatsUiState.update { state ->
            val chat = id?.let { chatId ->
                state.conversations.filterIsInstance<Chat>().find { it.id == chatId }
            }
            state.copy(selectedChat = id, selectedUser = chat?.chatTitle)
        }
    }

    fun setSelectedUser(userId: Long?) {
        _chatsUiState.update { state ->
            val user = userId?.let { id ->
                state.conversations.flatMap { conversation ->
                    when (conversation) {
                        is Chat -> listOf(conversation.chatTitle)
                        is Palette -> conversation.members
                    }
                }.find { it.steamId == id }
            }
            state.copy(selectedUser = user)
        }
    }
}
