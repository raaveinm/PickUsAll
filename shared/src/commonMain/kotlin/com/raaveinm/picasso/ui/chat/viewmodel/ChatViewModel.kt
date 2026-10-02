package com.raaveinm.picasso.ui.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raaveinm.core.database.dao.ChatDao
import com.raaveinm.core.database.dao.ServerDao
import com.raaveinm.core.database.entities.api.user.toDto
import com.raaveinm.core.database.entities.chat.toDto
import com.raaveinm.core.model.chat.Chat
import com.raaveinm.core.model.chat.Palette
import com.raaveinm.core.model.toWsUrl
import com.raaveinm.features.impl_webrtc.CallManager
import com.raaveinm.picasso.data.repository.AuthRepository
import com.raaveinm.picasso.data.repository.ChatRepository
import com.raaveinm.picasso.data.repository.FriendsRepository
import com.raaveinm.pickusall.core.designsystem.utils.WarnLevel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val CHAT_HISTORY_PAGE_SIZE = 50

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    val chatDao: ChatDao,
    private val serverDao: ServerDao,
    private val chatRepository: ChatRepository,
    private val friendsRepository: FriendsRepository,
    private val callManager: CallManager,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _chatsUiState = MutableStateFlow(ChatUiState())
    private val _friendListUiState = MutableStateFlow(FriendsUiState())
    val chatsUiState = _chatsUiState.asStateFlow()
    val  friendsUiState = _friendListUiState.asStateFlow()
    val isInCall: StateFlow<Boolean> = callManager.isInCall

    private var currentServerId: Long? = null

    /** The logged-in steamId, or null when signed out. Keys the friend list and signaling. */
    private val selfSteamId = authRepository.session
        .map { it?.userId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

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
        selfSteamId
            .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else chatDao.getUserFriends(id) }
            .onEach { friends ->
                _friendListUiState.update { it.copy(friends = friends.map { user -> user.toDto() }) }
            }
            .launchIn(viewModelScope)

        selfSteamId
            .onEach { id ->
                _chatsUiState.update { it.copy(selfSteamId = id) }
                // Re-pull on sign-in, not only at construction.
                if (id != null) refreshFriends()
            }
            .launchIn(viewModelScope)

        ///////////////////////////////////////////////
        // Signaling
        ///////////////////////////////////////////////

        combine(
            serverDao.getAllServers().map { servers -> servers.firstOrNull() },
            authRepository.session
        ) { server, session -> server to session }
            .onEach { (server, _) -> currentServerId = server?.id }
            .distinctUntilChanged()
            .onEach { (server, session) ->
                if (server == null || session == null || server.url.isBlank()) {
                    callManager.disconnectSignaling()
                    return@onEach
                }
                callManager.connectSignaling(session.token, server.url.toWsUrl())
            }
            .launchIn(viewModelScope)
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
            val self = authRepository.session.first()?.userId ?: return@launch
            _friendListUiState.update { it.copy(isRefreshing = true) }
            try {
                friendsRepository.refresh(self)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                viewModelScope.launch {
                    _chatsUiState.update {it.copy(warning = Pair(WarnLevel.ERROR, "FRIEND LIST UPDATE ERR: flu_400")) }
                }
                println("ChatViewModel.refreshFriends failed")
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

    fun dmWith(steamId: Long, onResult: (Long) -> Unit) {
        val existing = _chatsUiState.value.conversations
            .filterIsInstance<Chat>()
            .firstOrNull { it.chatTitle.steamId == steamId }
            ?.id
        if (existing != null) {
            onResult(existing)
            return
        }
        val serverId = currentServerId
        if (serverId == null) {
            _chatsUiState.update {
                it.copy(warning = Pair(WarnLevel.ERROR, "dmWith failed: no known server to create the conversation on"))
            }
            return
        }
        viewModelScope.launch {
            val chatId = chatDao.findOrCreateDm(
                serverId = serverId,
                remoteId = steamId,
                chatTitleSteamId = steamId
            )
            onResult(chatId)
        }
    }

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
            val self = authRepository.session.first()?.userId ?: return@launch
            chatRepository.sendMessage(conversationId, self, text)
            if (_chatsUiState.value.selectedChat == conversationId) {
                _chatsUiState.update { it.copy(chatHistory = emptyList(), hasMoreChatHistory = true) }
                retrieveChatHistory(conversationId)
            }
        }
    }
}
