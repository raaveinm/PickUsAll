package com.raaveinm.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.raaveinm.core.database.entities.chat.ChatWithTitle
import com.raaveinm.core.database.entities.chat.Chats
import com.raaveinm.core.database.entities.chat.Conversations
import com.raaveinm.core.database.entities.chat.PaletteWithMembers
import kotlinx.coroutines.flow.Flow

//
// Created by Kirill "Raaveinm" on 8/23/26.
//

@Dao
interface ChatDao {
    @Insert
    suspend fun insertConversation(conversation: Conversations): Long

    @Insert
    suspend fun insertChat(chat: Chats)

    @Transaction
    @Query("SELECT conversations.*, chats.* FROM conversations JOIN chats ON chats.conversationId = conversations.id ORDER BY conversations.id DESC")
    fun observeChats(): Flow<List<ChatWithTitle>>

    @Transaction
    @Query("SELECT conversations.*, palettes.* FROM conversations JOIN palettes ON palettes.conversationId = conversations.id ORDER BY conversations.id DESC")
    fun observePalettes(): Flow<List<PaletteWithMembers>>

    // Caller (repository) is responsible for making sure `chatTitleSteamId` already
    // exists in Users first - creating that row needs real Steam profile data, which
    // isn't available here, so we don't do it as a side effect of this transaction.
    @Transaction
    suspend fun createNewDM(
        serverId: Long,
        remoteId: Long,
        chatTitleSteamId: Long,
        lastMessage: String? = null
    ): Long {
        val conversationId = insertConversation(
            Conversations(
                serverId = serverId,
                kind = "chat",
                lastMessage = lastMessage,
                remoteId = remoteId
            )
        )
        insertChat(Chats(conversationId = conversationId, chatTitleSteamId = chatTitleSteamId))
        return conversationId
    }
}