package com.raaveinm.picasso.ui.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raaveinm.core.database.dao.ChatDao
import com.raaveinm.core.database.entities.api.user.toDto
import com.raaveinm.core.database.entities.chat.toDto
import com.raaveinm.core.model.chat.Chat
import com.raaveinm.core.model.chat.MessageData
import com.raaveinm.core.model.chat.Palette
import com.raaveinm.core.model.social.ContactLevel
import com.raaveinm.features.impl_webrtc.CallManager
import com.raaveinm.picasso.data.repository.AuthRepository
import com.raaveinm.picasso.data.repository.ChatEvent
import com.raaveinm.picasso.data.repository.ChatRepository
import com.raaveinm.picasso.data.repository.ContactsRepository
import com.raaveinm.picasso.data.repository.ConversationResult
import com.raaveinm.picasso.data.repository.FriendsRepository
import com.raaveinm.picasso.data.repository.SocialEvent
import com.raaveinm.picasso.data.repository.SocialResult
import com.raaveinm.picasso.data.sync.SyncCoordinator
import com.raaveinm.pickusall.core.designsystem.utils.WarnLevel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** How many messages one scroll-up reveals from what is already cached. */
private const val CHAT_HISTORY_PAGE_SIZE = 50

/** The newest [limit] messages of [conversationId], already mapped for the screens. */
private data class HistorySlice(val conversationId: Long?, val limit: Int, val messages: List<MessageData>)

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    val chatDao: ChatDao,
    private val chatRepository: ChatRepository,
    private val contactsRepository: ContactsRepository,
    private val friendsRepository: FriendsRepository,
    private val callManager: CallManager,
    private val authRepository: AuthRepository,
    private val syncCoordinator: SyncCoordinator
) : ViewModel() {
    private val _chatsUiState = MutableStateFlow(ChatUiState())
    private val _friendListUiState = MutableStateFlow(FriendsUiState())
    private val _socialUiState = MutableStateFlow(SocialUiState())
    val chatsUiState = _chatsUiState.asStateFlow()
    val friendsUiState = _friendListUiState.asStateFlow()
    val socialUiState = _socialUiState.asStateFlow()
    val isInCall: StateFlow<Boolean> = callManager.isInCall

    private val selectedChatId = MutableStateFlow<Long?>(null)
    private val historyLimit = MutableStateFlow(CHAT_HISTORY_PAGE_SIZE)

    /**
     * Conversations whose older history the server could not (or would not) give us. Without
     * this, a list shorter than the screen would re-ask on every recomposition. Cleared on
     * selection, so reopening the chat tries once more.
     */
    private val olderUnavailable = mutableSetOf<Long>()

    /** The logged-in steamId, or null when signed out. Keys the friend list. */
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


        combine(selectedChatId, historyLimit) { id, limit -> id to limit }
            .flatMapLatest { (id, limit) ->
                if (id == null) flowOf(HistorySlice(null, limit, emptyList()))
                else chatDao.observeHistory(id, limit).map { rows ->
                    HistorySlice(id, limit, rows.map { it.toDto() })
                }
            }
            .onEach { slice ->
                val exhausted = slice.conversationId?.let { chatDao.isHistoryExhausted(it) } ?: true
                val moreCached = slice.messages.size >= slice.limit
                _chatsUiState.update {
                    it.copy(
                        chatHistory = slice.messages,
                        isLoadingChatHistory = false,
                        hasMoreChatHistory = slice.conversationId != null &&
                            (moreCached || (!exhausted && slice.conversationId !in olderUnavailable))
                    )
                }
            }
            .launchIn(viewModelScope)

        ///////////////////////////////////////////////
        // friend list (Steam)
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
        // Contact graph (Picasso's own, not Steam's)
        ///////////////////////////////////////////////
        contactsRepository.contacts
            .onEach { list -> _socialUiState.update { it.copy(contacts = list) } }
            .launchIn(viewModelScope)
        contactsRepository.incomingRequests
            .onEach { list -> _socialUiState.update { it.copy(incomingRequests = list) } }
            .launchIn(viewModelScope)
        contactsRepository.outgoingRequests
            .onEach { list -> _socialUiState.update { it.copy(outgoingRequests = list) } }
            .launchIn(viewModelScope)
        contactsRepository.paletteInvites
            .onEach { list -> _socialUiState.update { it.copy(paletteInvites = list) } }
            .launchIn(viewModelScope)

        ///////////////////////////////////////////////
        // Things that happen without the user asking
        ///////////////////////////////////////////////
        syncCoordinator.state
            .onEach { state -> _chatsUiState.update { it.copy(connection = state) } }
            .launchIn(viewModelScope)
        contactsRepository.events
            .onEach { event ->
                when (event) {
                    is SocialEvent.IncomingRequest ->
                        warn(WarnLevel.INFO, "${nameOf(event.steamId)} wants to be your ally")
                    is SocialEvent.PaletteInvited ->
                        warn(WarnLevel.INFO, "${nameOf(event.inviterSteamId)} invited you to \"${event.name}\"")
                }
            }
            .launchIn(viewModelScope)
        chatRepository.events
            .onEach { event ->
                when (event) {
                    is ChatEvent.SendRejected -> warn(WarnLevel.WARN, "Message not delivered: ${event.code} (chat_e200)")
                }
            }
            .launchIn(viewModelScope)
    }

    ///////////////////////////////////////////////
    // Calls
    ///////////////////////////////////////////////

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
        viewModelScope.launch {
            val remoteId = chatDao.remoteIdOf(conversationId) ?: return@launch
            callManager.startCall(remoteId, peerSteamId)
        }
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

    ///////////////////////////////////////////////
    // Selection
    ///////////////////////////////////////////////

    fun setSelectedChat(id: Long?) {
        olderUnavailable.clear()
        historyLimit.value = CHAT_HISTORY_PAGE_SIZE
        selectedChatId.value = id
        _chatsUiState.update { state ->
            val chat = id?.let { chatId ->
                state.conversations.filterIsInstance<Chat>().find { it.id == chatId }
            }
            state.copy(
                selectedChat = id,
                selectedUser = chat?.chatTitle,
                chatHistory = emptyList(),
                hasMoreChatHistory = id != null
            )
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

    ///////////////////////////////////////////////
    // Conversations
    ///////////////////////////////////////////////

    /**
     * Opens the dm with [steamId], creating it on the server if it doesn't exist yet. A dm needs
     * the two to be mutual allies; when they aren't, an ally request is sent instead (see
     * ui_miss_todo.md - there is no screen for the answer yet) and [onResult] is not called.
     */
    fun dmWith(steamId: Long, onResult: (Long) -> Unit) {
        val existing = _chatsUiState.value.conversations
            .filterIsInstance<Chat>()
            .firstOrNull { it.chatTitle.steamId == steamId }
            ?.id
        if (existing != null) {
            onResult(existing)
            return
        }
        viewModelScope.launch {
            when (val result = chatRepository.createDm(steamId)) {
                is ConversationResult.Ready -> onResult(result.localId)
                ConversationResult.NotAllowed -> {
                    val request = contactsRepository.request(steamId)
                    warn(
                        WarnLevel.INFO,
                        when (request) {
                            SocialResult.PENDING, SocialResult.OK ->
                                "Not allies yet - an ally request was sent. The chat opens once it is accepted."
                            SocialResult.UNBLOCK_FIRST -> "You blocked ${nameOf(steamId)}. Unblock them first."
                            SocialResult.RATE_LIMITED -> "Too many ally requests today. Try again later."
                            else -> "Can't open this chat: not allies (chat_e403)"
                        }
                    )
                }
                ConversationResult.NoServer -> warn(WarnLevel.ERROR, "No server: add one in Settings first (chat_e100)")
                ConversationResult.Offline -> warn(WarnLevel.WARN, "Offline: the chat can't be created right now (chat_e101)")
                is ConversationResult.Invalid -> warn(WarnLevel.WARN, "Can't open this chat: ${result.message ?: "invalid"} (chat_e422)")
                is ConversationResult.Failed -> warn(WarnLevel.ERROR, "Server error ${result.status} (chat_e${result.status})")
            }
        }
    }

    /**
     * Creates a palette; everyone in [inviteSteamIds] must be an ally of the caller and gets a
     * pending invitation they have to accept. [onResult] receives the new local id.
     */
    fun createPalette(name: String, inviteSteamIds: List<Long>, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            when (val result = chatRepository.createPalette(name, inviteSteamIds)) {
                is ConversationResult.Ready -> onResult(result.localId)
                else -> warn(WarnLevel.WARN, describe(result))
            }
        }
    }

    /** Invites one of the caller's allies into an existing palette. */
    fun invite(conversationId: Long, steamId: Long) {
        viewModelScope.launch {
            val result = chatRepository.invite(conversationId, steamId)
            if (result !is ConversationResult.Ready) warn(WarnLevel.WARN, describe(result))
        }
    }

    ///////////////////////////////////////////////
    // Messages
    ///////////////////////////////////////////////

    /**
     * Loads more of the open conversation. Called by the list when it reaches its last item:
     * first reveals more of what is already cached, and only when the cache is used up asks
     * the server for older messages.
     */
    fun retrieveChatHistory(conversationId: Long) {
        val state = _chatsUiState.value
        if (state.isLoadingChatHistory || !state.hasMoreChatHistory) return
        _chatsUiState.update { it.copy(isLoadingChatHistory = true) }
        viewModelScope.launch {
            try {
                val cached = chatDao.countMessages(conversationId)
                if (cached < historyLimit.value && chatDao.isHistoryExhausted(conversationId) != true) {
                    // The window is wider than the cache: the rest has to come from the server.
                    if (!chatRepository.loadOlder(conversationId)) olderUnavailable += conversationId
                }
                historyLimit.value += CHAT_HISTORY_PAGE_SIZE
            } finally {
                _chatsUiState.update { it.copy(isLoadingChatHistory = false) }
            }
        }
    }

    fun sendMessage(conversationId: Long, text: String) {
        if (text.isBlank()) return
        val conversation = _chatsUiState.value.conversations.find { it.id == conversationId }
        if (conversation is Chat && !conversation.writable) {
            warn(WarnLevel.WARN, "You are no longer allies - this chat is read-only (chat_e403)")
            return
        }
        viewModelScope.launch {
            val self = authRepository.session.first()?.userId ?: return@launch
            chatRepository.sendMessage(conversationId, self, text)
        }
    }

    /** "Try again" on a message that was refused. */
    fun retryMessage(localMessageId: Long) {
        viewModelScope.launch { chatRepository.retryFailed(localMessageId) }
    }

    /** Deletes one of the user's own messages everywhere - silently, no placeholder is left behind. */
    fun deleteMessage(localMessageId: Long) {
        viewModelScope.launch {
            if (!chatRepository.deleteMessage(localMessageId)) {
                warn(WarnLevel.WARN, "Couldn't delete the message right now (chat_e102)")
            }
        }
    }

    ///////////////////////////////////////////////
    // Contacts (functions are wired; screens for them don't exist yet)
    ///////////////////////////////////////////////

    /** Asks [steamId] to become an ally. Works for a Steam friend, a typed steamId, or a palette member alike. */
    fun requestContact(steamId: Long) = socialAction { contactsRepository.request(steamId) }

    fun acceptRequest(fromSteamId: Long) = socialAction { contactsRepository.accept(fromSteamId) }

    /** Silent decline of an incoming request, or withdrawal of an outgoing one. */
    fun declineRequest(steamId: Long) = socialAction { contactsRepository.declineOrWithdraw(steamId) }

    /** [ContactLevel.ALLY] <-> [ContactLevel.FRIEND] on an existing contact, or [ContactLevel.IMPOSTER] to block. */
    fun setContactLevel(steamId: Long, level: ContactLevel) = socialAction { contactsRepository.setLevel(steamId, level) }

    /** Removes a contact, or lifts a block. */
    fun removeContact(steamId: Long) = socialAction { contactsRepository.remove(steamId) }

    fun acceptPaletteInvite(conversationRemoteId: Long) =
        socialAction { contactsRepository.acceptPaletteInvite(conversationRemoteId) }

    fun declinePaletteInvite(conversationRemoteId: Long) =
        socialAction { contactsRepository.declinePaletteInvite(conversationRemoteId) }

    private fun socialAction(action: suspend () -> SocialResult) {
        viewModelScope.launch {
            describe(action())?.let { warn(WarnLevel.WARN, it) }
        }
    }

    ///////////////////////////////////////////////
    // Messages to the user
    ///////////////////////////////////////////////

    private fun warn(level: WarnLevel, text: String) {
        // warningSeq makes two identical messages in a row still count as two events for the screen
        _chatsUiState.update { it.copy(warning = Pair(level, text), warningSeq = it.warningSeq + 1) }
    }

    /** A cached display name, falling back to the steamId for someone not fetched yet. */
    private fun nameOf(steamId: Long): String {
        val social = _socialUiState.value
        val user = social.contacts.find { it.steamId == steamId }?.user
            ?: social.incomingRequests.find { it.steamId == steamId }?.user
            ?: social.paletteInvites.find { it.inviterSteamId == steamId }?.inviter
            ?: _friendListUiState.value.friends.find { it.steamId == steamId }
        return user?.personaName ?: "Artist $steamId"
    }

    private fun describe(result: SocialResult): String? = when (result) {
        SocialResult.OK, SocialResult.PENDING -> null
        SocialResult.NOT_ALLOWED -> "Not allowed (social_e403)"
        SocialResult.UNBLOCK_FIRST -> "You blocked this person. Unblock them first (social_e409)"
        SocialResult.RATE_LIMITED -> "Too many requests. Try again later (social_e429)"
        SocialResult.INVALID -> "That isn't valid (social_e422)"
        SocialResult.NOT_FOUND -> "Nothing to do - it may have expired (social_e404)"
        SocialResult.NO_SERVER -> "No server: add one in Settings first (social_e100)"
        SocialResult.OFFLINE -> "Offline: try again in a moment (social_e101)"
        SocialResult.FAILED -> "Server error (social_e500)"
    }

    private fun describe(result: ConversationResult): String = when (result) {
        is ConversationResult.Ready -> ""
        ConversationResult.NotAllowed -> "Everyone invited must be your ally first (chat_e403)"
        ConversationResult.NoServer -> "No server: add one in Settings first (chat_e100)"
        ConversationResult.Offline -> "Offline: try again in a moment (chat_e101)"
        is ConversationResult.Invalid -> "Not possible: ${result.message ?: "invalid"} (chat_e422)"
        is ConversationResult.Failed -> "Server error ${result.status} (chat_e${result.status})"
    }
}
