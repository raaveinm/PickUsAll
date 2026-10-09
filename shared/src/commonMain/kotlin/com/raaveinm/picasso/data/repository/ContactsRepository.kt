package com.raaveinm.picasso.data.repository

//
// Created by Kirill "Raaveinm" on 10/9/26.
//

import com.raaveinm.core.database.dao.ChatDao
import com.raaveinm.core.database.dao.ContactDao
import com.raaveinm.core.database.entities.api.user.toDto
import com.raaveinm.core.database.entities.social.ContactRequests
import com.raaveinm.core.database.entities.social.Contacts
import com.raaveinm.core.database.entities.social.PaletteInvites
import com.raaveinm.core.model.social.ContactEntry
import com.raaveinm.core.model.social.ContactLevel
import com.raaveinm.core.model.social.ContactRequestEntry
import com.raaveinm.core.model.social.PaletteInviteEntry
import com.raaveinm.picasso.data.server.ServerContextSource
import com.raaveinm.picasso.data.server.ApiResult
import com.raaveinm.picasso.data.server.ChatEnvelope
import com.raaveinm.picasso.data.server.ChatFrameType
import com.raaveinm.picasso.data.server.ContactsApi
import com.raaveinm.picasso.data.server.ConversationsApi
import com.raaveinm.picasso.data.server.ServerContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** What came of a social action. Statuses are decided HERE so no screen has to know the HTTP contract. */
enum class SocialResult {
    OK,             // Done
    PENDING,        // Request accepted by the server and waiting - which is also what a silently dropped one answers
    NOT_ALLOWED,    // 403: not allowed (not allies, or the target blocked the caller - deliberately indistinguishable)
    UNBLOCK_FIRST,  // 409: the caller has blocked that person and has to unblock first
    RATE_LIMITED,   // 429: too many requests; try later
    INVALID,        // The server rejected the input (e.g. yourself, or a level it doesn't know)
    NOT_FOUND,      // There was nothing of that kind to act on (no such request, not a contact, no such invitation)
    NO_SERVER,
    OFFLINE,        // Network failure - nothing was decided, trying again is safe
    FAILED
}

/** Pushed by the server while connected; the shell turns them into a banner. */
sealed interface SocialEvent {
    data class IncomingRequest(val steamId: Long) : SocialEvent
    data class PaletteInvited(val name: String, val inviterSteamId: Long) : SocialEvent
}

/**
 * Picasso's contact graph - ally requests, tiers, the blocklist, palette invitations -
 * with Room as the cache the screens observe. The server is SSOT and keeps the two
 * parties' rows consistent, so there is no local edit to merge: every action goes to
 * the server first, and the cache is then replaced wholesale from `GET /contacts`.
 *
 * Steam friendship plays no role here; a Steam friend is just somebody who can be sent
 * a request.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ContactsRepository(
    private val contactDao: ContactDao,
    private val chatDao: ChatDao,
    private val activeServer: ServerContextSource,
    private val contactsApi: ContactsApi,
    private val conversationsApi: ConversationsApi,
    private val chatRepository: ChatRepository
) {
    private val _events = MutableSharedFlow<SocialEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<SocialEvent> = _events

    ///////////////////////////////////////////////
    // Observed state (empty while signed out / no server)
    ///////////////////////////////////////////////

    val contacts: Flow<List<ContactEntry>> = activeServer.context.flatMapLatest { context ->
        if (context == null) flowOf(emptyList())
        else contactDao.observeContacts(context.serverId).map { rows ->
            rows.mapNotNull { row ->
                val level = ContactLevel.fromWire(row.contact.level) ?: return@mapNotNull null
                ContactEntry(row.contact.steamId, level, row.contact.since, row.user?.toDto())
            }
        }
    }

    val incomingRequests: Flow<List<ContactRequestEntry>> = requests(incoming = true)

    val outgoingRequests: Flow<List<ContactRequestEntry>> = requests(incoming = false)

    val paletteInvites: Flow<List<PaletteInviteEntry>> = activeServer.context.flatMapLatest { context ->
        if (context == null) flowOf(emptyList())
        else contactDao.observePaletteInvites(context.serverId).map { rows ->
            rows.map { row ->
                PaletteInviteEntry(
                    conversationRemoteId = row.invite.conversationRemoteId,
                    name = row.invite.name,
                    inviterSteamId = row.invite.inviterSteamId,
                    createdAtEpochMs = row.invite.createdAt,
                    inviter = row.inviter?.toDto()
                )
            }
        }
    }

    private fun requests(incoming: Boolean): Flow<List<ContactRequestEntry>> =
        activeServer.context.flatMapLatest { context ->
            if (context == null) flowOf(emptyList())
            else contactDao.observeRequests(context.serverId, incoming).map { rows ->
                rows.map { ContactRequestEntry(it.request.steamId, it.request.createdAt, it.user?.toDto()) }
            }
        }

    ///////////////////////////////////////////////
    // Refresh
    ///////////////////////////////////////////////

    /** Replaces the cached graph with the server's. Returns false (cache untouched) if it could not be fetched. */
    suspend fun refresh(context: ServerContext): Boolean {
        val wire = when (val result = contactsApi.contacts(context)) {
            is ApiResult.Ok -> result.value
            is ApiResult.Rejected, is ApiResult.Unavailable -> return false
        }

        val serverId = context.serverId
        val contacts = wire.contacts.mapNotNull {
            val steamId = it.steamId.toLongOrNull() ?: return@mapNotNull null
            if (ContactLevel.fromWire(it.level) == null) return@mapNotNull null
            Contacts(serverId, steamId, it.level, it.since)
        }
        val requests = wire.incoming.mapNotNull {
            it.steamId.toLongOrNull()?.let { id -> ContactRequests(serverId, id, incoming = true, createdAt = it.createdAt) }
        } + wire.outgoing.mapNotNull {
            it.steamId.toLongOrNull()?.let { id -> ContactRequests(serverId, id, incoming = false, createdAt = it.createdAt) }
        }
        val invites = wire.paletteInvites.mapNotNull {
            val palette = it.conversationId.toLongOrNull() ?: return@mapNotNull null
            val inviter = it.inviterSteamId.toLongOrNull() ?: return@mapNotNull null
            PaletteInvites(serverId, palette, it.name, inviter, it.createdAt)
        }

        chatDao.ensureUsers(
            contacts.map { it.steamId } + requests.map { it.steamId } + invites.map { it.inviterSteamId }
        )
        contactDao.replaceAll(serverId, contacts, requests, invites)
        chatDao.refreshDmWritability(serverId)
        return true
    }

    ///////////////////////////////////////////////
    // Actions
    ///////////////////////////////////////////////

    /** Asks [steamId] to become an ally. The answer never reveals a block or whether the person exists here. */
    suspend fun request(steamId: Long): SocialResult =
        act { context -> contactsApi.sendRequest(context, steamId) }

    suspend fun accept(fromSteamId: Long): SocialResult =
        act { context -> contactsApi.accept(context, fromSteamId) }

    /** Silent either way: a decline is not reported to the sender, a withdrawal just vanishes. */
    suspend fun declineOrWithdraw(steamId: Long): SocialResult =
        act { context -> contactsApi.declineOrWithdraw(context, steamId) }

    /** Moves an existing contact between [ContactLevel.ALLY] and [ContactLevel.FRIEND], or blocks with [ContactLevel.IMPOSTER]. */
    suspend fun setLevel(steamId: Long, level: ContactLevel): SocialResult =
        act { context -> contactsApi.setLevel(context, steamId, level.wire) }

    /** Removes a contact, or lifts a block. Both sides stop being contacts - one side's wish is enough. */
    suspend fun remove(steamId: Long): SocialResult =
        act { context -> contactsApi.remove(context, steamId) }

    suspend fun acceptPaletteInvite(conversationRemoteId: Long): SocialResult {
        val outcome = chatRepository.acceptPaletteInvite(conversationRemoteId)
        refreshIfOk(outcome is ConversationResult.Ready)
        return when (outcome) {
            is ConversationResult.Ready -> SocialResult.OK
            ConversationResult.NotAllowed -> SocialResult.NOT_ALLOWED
            is ConversationResult.Invalid -> SocialResult.NOT_FOUND
            ConversationResult.NoServer -> SocialResult.NO_SERVER
            ConversationResult.Offline -> SocialResult.OFFLINE
            is ConversationResult.Failed -> SocialResult.FAILED
        }
    }

    suspend fun declinePaletteInvite(conversationRemoteId: Long): SocialResult =
        act { context -> conversationsApi.declineInvite(context, conversationRemoteId) }

    /** Runs one call, then re-reads the whole graph so the cache reflects exactly what the server now says. */
    private suspend fun act(call: suspend (ServerContext) -> ApiResult<Unit>): SocialResult {
        val context = activeServer.current() ?: return SocialResult.NO_SERVER
        val result = call(context)
        val mapped = when (result) {
            is ApiResult.Ok -> if (result.status == 202) SocialResult.PENDING else SocialResult.OK
            is ApiResult.Rejected -> when (result.status) {
                403 -> SocialResult.NOT_ALLOWED
                404 -> SocialResult.NOT_FOUND
                409 -> SocialResult.UNBLOCK_FIRST
                422 -> SocialResult.INVALID
                429 -> SocialResult.RATE_LIMITED
                else -> SocialResult.FAILED
            }
            is ApiResult.Unavailable -> SocialResult.OFFLINE
        }
        if (result is ApiResult.Ok) refresh(context)
        return mapped
    }

    private suspend fun refreshIfOk(ok: Boolean) {
        if (ok) activeServer.current()?.let { refresh(it) }
    }

    ///////////////////////////////////////////////
    // Live frames
    ///////////////////////////////////////////////

    /**
     * `contact_request`, `contact_updated`, `palette_invite`. The frame says that
     * something changed; GET /contacts says what is true now, so the cache is always
     * replaced from there rather than patched from the frame - a missed or reordered
     * frame can then never leave it wrong.
     */
    suspend fun onFrame(context: ServerContext, envelope: ChatEnvelope) {
        when (envelope.type) {
            ChatFrameType.CONTACT_REQUEST ->
                envelope.contactRequest?.steamId?.toLongOrNull()?.let { _events.tryEmit(SocialEvent.IncomingRequest(it)) }
            ChatFrameType.PALETTE_INVITE -> envelope.paletteInvite?.let { invite ->
                val inviter = invite.inviterSteamId.toLongOrNull()
                if (invite.state == "pending" && inviter != null) {
                    _events.tryEmit(SocialEvent.PaletteInvited(invite.name, inviter))
                }
            }
        }
        refresh(context)
    }
}
