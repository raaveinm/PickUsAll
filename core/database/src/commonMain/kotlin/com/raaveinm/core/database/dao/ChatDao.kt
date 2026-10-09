package com.raaveinm.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import androidx.room3.Upsert
import com.raaveinm.core.database.entities.api.user.Users
import com.raaveinm.core.database.entities.chat.ChatWithTitle
import com.raaveinm.core.database.entities.chat.Chats
import com.raaveinm.core.database.entities.chat.Conversations
import com.raaveinm.core.database.entities.chat.ConversationCursor
import com.raaveinm.core.database.entities.chat.MessageData
import com.raaveinm.core.database.entities.chat.MessageWithSender
import com.raaveinm.core.database.entities.chat.PaletteMembers
import com.raaveinm.core.database.entities.chat.PaletteWithMembers
import com.raaveinm.core.database.entities.chat.Palettes
import com.raaveinm.core.database.entities.chat.PendingOutgoing
import com.raaveinm.core.database.entities.chat.ServerConversation
import com.raaveinm.core.database.entities.chat.ServerMessage
import com.raaveinm.core.model.chat.MessageStatus
import kotlinx.coroutines.flow.Flow

//
// Created by Kirill "Raaveinm" on 8/23/26.
//

/*
 * Message order, newest first, used by every history query: unsent rows (no server id
 * yet) come before confirmed ones, because a message you just typed is always the
 * newest thing in the chat; confirmed ones follow the server's id, which is the
 * conversation's total order.
 */
private const val NEWEST_FIRST = "(remoteId IS NULL) DESC, remoteId DESC, timestamp DESC, id DESC"

@Dao
interface ChatDao {
    @Insert
    suspend fun insertConversation(conversation: Conversations): Long

    @Insert
    suspend fun insertChat(chat: Chats)

    @Insert
    suspend fun insertMessage(message: MessageData): Long

    @Query("UPDATE MessageData SET status = :status WHERE id = :id")
    suspend fun updateMessageStatus(id: Long, status: MessageStatus)

    @Transaction
    @Query("SELECT conversations.*, chats.* FROM conversations JOIN chats" +
            " ON chats.conversationId = conversations.id ORDER BY conversations.id DESC")
    fun observeChats(): Flow<List<ChatWithTitle>>

    @Transaction
    @Query("SELECT conversations.*, palettes.* FROM conversations JOIN palettes" +
            " ON palettes.conversationId = conversations.id ORDER BY conversations.id DESC")
    fun observePalettes(): Flow<List<PaletteWithMembers>>

    @Query("SELECT conversationId FROM Chats WHERE chatTitleSteamId = :steamId LIMIT 1")
    suspend fun findDmConversationId(steamId: Long): Long?

    @Query(
        "SELECT steamId, communityVisibilityState, profileState, personaName, commentPermission, " +
                "profileUrl, avatar, avatarMedium, avatarFull, avatarHash, lastLogOff, personaState, " +
                "realName, primaryClanId, timeCreated, personaStateFlags, locCountryCode," +
                " locStateCode, locCityId, gameExtraInfo, gameId, fetchedAt from SteamFriends " +
                "JOIN main.Users U on SteamFriends.friendSteamId = U.steamId where userSteamId = :userId;"
    )
    fun getUserFriends(userId: Long): Flow<List<Users>>

    ///////////////////////////////////////////////
    // History
    ///////////////////////////////////////////////

    @Transaction
    @Query("SELECT * FROM MessageData WHERE conversationId = :conversationId ORDER BY $NEWEST_FIRST LIMIT :limit OFFSET :offset")
    suspend fun getChatHistory(conversationId: Long, offset: Int, limit: Int): List<MessageWithSender>

    /** Live view of the newest [limit] messages - re-emits on every send, ack, live frame and sync. */
    @Transaction
    @Query("SELECT * FROM MessageData WHERE conversationId = :conversationId ORDER BY $NEWEST_FIRST LIMIT :limit")
    fun observeHistory(conversationId: Long, limit: Int): Flow<List<MessageWithSender>>

    @Query("SELECT COUNT(*) FROM MessageData WHERE conversationId = :conversationId")
    suspend fun countMessages(conversationId: Long): Int

    /** The scroll-up cursor: everything older than this has to come from the server. */
    @Query("SELECT MIN(remoteId) FROM MessageData WHERE conversationId = :conversationId")
    suspend fun oldestRemoteId(conversationId: Long): Long?

    @Query("SELECT historyExhausted FROM Conversations WHERE id = :conversationId")
    suspend fun isHistoryExhausted(conversationId: Long): Boolean?

    @Query("UPDATE Conversations SET historyExhausted = :exhausted WHERE id = :conversationId")
    suspend fun setHistoryExhausted(conversationId: Long, exhausted: Boolean)

    ///////////////////////////////////////////////
    // Conversations (server-issued ids)
    ///////////////////////////////////////////////

    @Query("SELECT * FROM Conversations WHERE serverId = :serverId AND remoteId = :remoteId LIMIT 1")
    suspend fun findConversation(serverId: Long, remoteId: Long): Conversations?

    @Query("SELECT remoteId FROM Conversations WHERE id = :conversationId")
    suspend fun remoteIdOf(conversationId: Long): Long?

    @Query("SELECT id FROM Conversations WHERE serverId = :serverId AND remoteId = :remoteId")
    suspend fun localIdOf(serverId: Long, remoteId: Long): Long?

    @Update
    suspend fun updateConversation(conversation: Conversations)

    // Upsert, not REPLACE: REPLACE deletes the row first, and deleting a palette cascades to its members.
    @Upsert
    suspend fun upsertChat(chat: Chats)

    @Upsert
    suspend fun upsertPalette(palette: Palettes)

    @Query("DELETE FROM PaletteMembers WHERE paletteConversationId = :conversationId")
    suspend fun clearMembers(conversationId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMembers(members: List<PaletteMembers>)

    /**
     * Messages reference Users through a foreign key, and a conversation can be full of
     * people whose Steam profile was never fetched. This inserts a placeholder (fetchedAt
     * = 0) for each; a real row that already exists is left untouched.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserStubs(users: List<Users>)

    /** [insertUserStubs] for plain steamIds - the entry point for callers outside this module. */
    @Transaction
    suspend fun ensureUsers(steamIds: List<Long>) = insertUserStubs(steamIds.distinct().map(::stubUser))

    /** Placeholders still waiting for a real Steam profile. */
    @Query("SELECT steamId FROM Users WHERE fetchedAt = 0")
    suspend fun getStubSteamIds(): List<Long>

    /**
     * The server's description of a conversation, written into Room. Idempotent: it is
     * called for every conversation of every sync and for every `conversation_*` frame.
     * Returns the local id.
     */
    @Transaction
    suspend fun upsertServerConversation(serverId: Long, conversation: ServerConversation, selfSteamId: Long): Long {
        insertUserStubs(conversation.members.map(::stubUser))
        val kind = if (conversation.isDm) "chat" else "palette"
        val existing = findConversation(serverId, conversation.remoteId)
        val localId = if (existing == null) {
            insertConversation(
                Conversations(
                    serverId = serverId,
                    kind = kind,
                    lastMessage = null,
                    remoteId = conversation.remoteId,
                    writable = conversation.writable
                )
            )
        } else {
            if (existing.kind != kind || existing.writable != conversation.writable) {
                updateConversation(existing.copy(kind = kind, writable = conversation.writable))
            }
            existing.id
        }

        if (conversation.isDm) {
            // Chats stores only the OTHER participant; the local user is implicit from the session.
            val other = conversation.members.firstOrNull { it != selfSteamId } ?: selfSteamId
            upsertChat(Chats(conversationId = localId, chatTitleSteamId = other))
        } else {
            upsertPalette(Palettes(conversationId = localId, name = conversation.name.orEmpty()))
            clearMembers(localId)
            insertMembers(conversation.members.map { PaletteMembers(localId, it) })
        }
        return localId
    }

    /**
     * A dm is writable exactly while the local user regards the peer as ally/friend. That
     * is derivable because the server keeps the two rows symmetric - a removal deletes
     * both, a block deletes the blocked side's - so this can follow a contact change
     * without waiting for the next sync (whose `writable` is authoritative and agrees).
     */
    @Query(
        "UPDATE Conversations SET writable = EXISTS (" +
                "SELECT 1 FROM Chats ch JOIN Contacts k ON k.steamId = ch.chatTitleSteamId " +
                "AND k.serverId = Conversations.serverId " +
                "WHERE ch.conversationId = Conversations.id AND k.level IN ('ally', 'friend')) " +
                "WHERE kind = 'chat' AND serverId = :serverId"
    )
    suspend fun refreshDmWritability(serverId: Long)

    @Query("DELETE FROM Conversations WHERE serverId = :serverId AND remoteId NOT IN (:keepRemoteIds)")
    suspend fun deleteConversationsExcept(serverId: Long, keepRemoteIds: List<Long>)

    @Query(
        "SELECT c.remoteId AS remoteId, " +
                "(SELECT MAX(m.remoteId) FROM MessageData m WHERE m.conversationId = c.id) AS newestMessageRemoteId " +
                "FROM Conversations c WHERE c.serverId = :serverId"
    )
    suspend fun getCursors(serverId: Long): List<ConversationCursor>

    ///////////////////////////////////////////////
    // Messages from the server
    ///////////////////////////////////////////////

    @Query("SELECT * FROM MessageData WHERE clientMessageId = :clientMessageId LIMIT 1")
    suspend fun findByClientMessageId(clientMessageId: String): MessageData?

    @Query("SELECT * FROM MessageData WHERE conversationId = :conversationId AND remoteId = :remoteId LIMIT 1")
    suspend fun findByRemoteId(conversationId: Long, remoteId: Long): MessageData?

    @Query("UPDATE MessageData SET remoteId = :remoteId, timestamp = :timestamp, status = 'SENT' WHERE id = :id")
    suspend fun resolveMessage(id: Long, remoteId: Long, timestamp: Long)

    @Query("UPDATE Conversations SET lastMessage = " +
            "(SELECT textMessage FROM MessageData WHERE conversationId = :conversationId ORDER BY $NEWEST_FIRST LIMIT 1) " +
            "WHERE id = :conversationId")
    suspend fun refreshLastMessage(conversationId: Long)

    /**
     * Folds server messages into the local cache. Three cases, one per kind of overlap
     * the reconnect procedure deliberately produces (a sync page and the live frames
     * that arrived while it was in flight):
     *  - the message is ours and still PENDING -> resolve that row (this is also how a
     *    lost ack heals: the next sync carries the message back with our clientMessageId);
     *  - we already hold it -> nothing;
     *  - otherwise -> insert.
     */
    @Transaction
    suspend fun applyServerMessages(conversationId: Long, messages: List<ServerMessage>) {
        if (messages.isEmpty()) return
        insertUserStubs(messages.map { it.senderSteamId }.distinct().map(::stubUser))
        for (message in messages) {
            val local = findByClientMessageId(message.clientMessageId)
            when {
                local != null && local.remoteId == null ->
                    resolveMessage(local.id, message.remoteId, message.createdAtMs)
                local != null -> Unit
                findByRemoteId(conversationId, message.remoteId) != null -> Unit
                else -> insertMessage(
                    MessageData(
                        conversationId = conversationId,
                        senderSteamId = message.senderSteamId,
                        textMessage = message.body,
                        timestamp = message.createdAtMs,
                        status = MessageStatus.SENT,
                        clientMessageId = message.clientMessageId,
                        remoteId = message.remoteId
                    )
                )
            }
        }
        refreshLastMessage(conversationId)
    }

    @Query("DELETE FROM MessageData WHERE conversationId = :conversationId AND remoteId IS NOT NULL")
    suspend fun deleteConfirmedMessages(conversationId: Long)

    /**
     * `reset` from POST /sync: too much happened since the cursor, so the cached range is
     * thrown away and replaced by the newest page. Keeps the invariant that a
     * conversation's confirmed messages are one contiguous range ending at the newest.
     * Unsent rows are not touched - they are the one thing the server cannot give back.
     */
    @Transaction
    suspend fun resetConversationMessages(
        conversationId: Long,
        messages: List<ServerMessage>,
        historyExhausted: Boolean
    ) {
        deleteConfirmedMessages(conversationId)
        setHistoryExhausted(conversationId, historyExhausted)
        applyServerMessages(conversationId, messages)
        refreshLastMessage(conversationId)
    }

    @Query("DELETE FROM MessageData WHERE conversationId = :conversationId AND remoteId = :remoteId")
    suspend fun deleteByRemoteId(conversationId: Long, remoteId: Long)

    ///////////////////////////////////////////////
    // Outbox
    ///////////////////////////////////////////////

    @Query("SELECT * FROM MessageData WHERE id = :id")
    suspend fun getMessage(id: Long): MessageData?

    @Query(
        "SELECT m.id AS id, m.clientMessageId AS clientMessageId, c.remoteId AS conversationRemoteId, " +
                "m.textMessage AS textMessage FROM MessageData m JOIN Conversations c ON c.id = m.conversationId " +
                "WHERE c.serverId = :serverId AND m.status = 'PENDING' ORDER BY m.timestamp ASC, m.id ASC"
    )
    suspend fun getPendingOutgoing(serverId: Long): List<PendingOutgoing>

    /** Only touches a row that has no server id yet - a confirmed message never goes back to FAILED. */
    @Query("UPDATE MessageData SET status = :status WHERE clientMessageId = :clientMessageId AND remoteId IS NULL")
    suspend fun updateStatusByClientId(clientMessageId: String, status: MessageStatus)

    /** The user's "try again" on a FAILED message. */
    @Query("UPDATE MessageData SET status = 'PENDING' WHERE id = :id AND status = 'FAILED'")
    suspend fun requeueFailed(id: Long)

    /** Drops a message that never reached the server. A confirmed one has to be deleted server-side first. */
    @Query("DELETE FROM MessageData WHERE id = :id AND remoteId IS NULL")
    suspend fun deleteUnsent(id: Long)
}

/**
 * A placeholder for a user whose Steam profile has not been fetched. fetchedAt = 0 is
 * the marker [ChatDao.getStubSteamIds] looks for; the name is overwritten by the real
 * profile as soon as one arrives (UserDao.upsertUsers replaces the row).
 */
internal fun stubUser(steamId: Long): Users = Users(
    steamId = steamId,
    communityVisibilityState = 1,
    personaName = "Artist ${steamId.toString().takeLast(4)}",
    commentPermission = 0,
    profileUrl = "",
    avatar = "",
    avatarMedium = "",
    avatarFull = "",
    avatarHash = "",
    personaState = 0,
    fetchedAt = 0L
)
