package com.raaveinm.picasso.ui.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raaveinm.core.database.dao.ChatDao
import com.raaveinm.core.database.entities.api.user.toDto
import com.raaveinm.core.database.entities.chat.toDto
import com.raaveinm.core.model.chat.Chat
import com.raaveinm.core.model.chat.Palette
import com.raaveinm.features.impl_webrtc.CallManager
import com.raaveinm.picasso.AppConfig
import com.raaveinm.picasso.data.repository.ChatRepository
import com.raaveinm.picasso.data.repository.FriendsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val CHAT_HISTORY_PAGE_SIZE = 50

class ChatViewModel(
    val chatDao: ChatDao,
    private val chatRepository: ChatRepository,
    private val friendsRepository: FriendsRepository,
    private val callManager: CallManager
) : ViewModel() {
    private val _chatsUiState = MutableStateFlow(ChatUiState())
    private val _friendListUiState = MutableStateFlow(FriendsUiState())
    val chatsUiState = _chatsUiState.asStateFlow()
    val  friendsUiState = _friendListUiState.asStateFlow()
    val isInCall: StateFlow<Boolean> = callManager.isInCall

    init {
        ///////////////////////////////////////////////
        // Init chat ui state
        ///////////////////////////////////////////////
        combine(chatDao.observeChats(), chatDao.observePalettes()) { chats, palettes ->
            chats.map { it.toDto() } + palettes.map { it.toDto() }
        }.onEach { conversations ->
            _chatsUiState.update { it.copy(conversations = conversations) }
        }.launchIn(viewModelScope)
        ///////////////////////////////////////////////
        // friend list
        ///////////////////////////////////////////////
        viewModelScope.launch{
            chatDao.getUserFriends(AppConfig.USER_ID).onEach { friends ->
                _friendListUiState.update { it.copy(friends = friends.map { user -> user.toDto() }) }
            }.launchIn(viewModelScope)
        }
        refreshFriends()
        // lets an incoming call be received even before the user starts one themselves
        callManager.connectSignaling(AppConfig.USER_ID, AppConfig.SIGNALING_WS_URL)
    }

    /** No-op for a Palette (group calling isn't supported - mesh-only, DM calls only). */
    fun onCallClicked(conversationId: Long) {
        if (callManager.isInCall.value) {
            callManager.endCall()
            return
        }
        val peerSteamId = _chatsUiState.value.conversations
            .filterIsInstance<Chat>()
            .find { it.id == conversationId }
            ?.chatTitle?.steamId
            ?: return
        callManager.startCall(conversationId, peerSteamId)
    }

    /**
     * Pulls the friend list from Steam into Room - the flow above is what actually
     * surfaces it, so nothing is returned here.
     */
    fun refreshFriends() {
        if (_friendListUiState.value.isRefreshing) return
        viewModelScope.launch {
            _friendListUiState.update { it.copy(isRefreshing = true) }
            try {
                friendsRepository.refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // TODO: surface a real error state once there's a UI for it; for now
                // a failed refresh just leaves the last cached friend list in place.
                println("ChatViewModel.refreshFriends failed: $e")
            } finally {
                _friendListUiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun setSelectedChat(id: Long?) {
        _chatsUiState.update { state ->
            val chat = id?.let { chatId ->
                state.conversations.filterIsInstance<Chat>().find { it.id == chatId }
            }
            state.copy(
                selectedChat = id,
                selectedUser = chat?.chatTitle,
                chatHistory = emptyList(),
                hasMoreChatHistory = true
            )
        }
        _chatsUiState.value.selectedChat?.let { retrieveChatHistory(it) }
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

    fun dmWith(steamId: Long): Long? = _chatsUiState.value.conversations
        .filterIsInstance<Chat>()
        .firstOrNull { it.chatTitle.steamId == steamId }
        ?.id

    fun retrieveChatHistory(conversationId: Long) {
        val state = _chatsUiState.value
        if (state.isLoadingChatHistory || !state.hasMoreChatHistory) return
        _chatsUiState.update { it.copy(isLoadingChatHistory = true) }
        viewModelScope.launch {
            val nextPage = chatDao
                .getChatHistory(conversationId, _chatsUiState.value.chatHistory.size, CHAT_HISTORY_PAGE_SIZE)
                .map { it.toDto() }
            _chatsUiState.update {
                it.copy(
                    chatHistory = it.chatHistory + nextPage,
                    isLoadingChatHistory = false,
                    hasMoreChatHistory = nextPage.size == CHAT_HISTORY_PAGE_SIZE
                )
            }
        }
    }

//    fun clearCachedChatHistory() {
//        _chatsUiState.update { it.copy(chatHistory = emptyList(), hasMoreChatHistory = true) }
//    }

    fun sendMessage(conversationId: Long, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(conversationId, AppConfig.USER_ID, text)
            if (_chatsUiState.value.selectedChat == conversationId) {
                _chatsUiState.update { it.copy(chatHistory = emptyList(), hasMoreChatHistory = true) }
                retrieveChatHistory(conversationId)
            }
        }
    }
}
