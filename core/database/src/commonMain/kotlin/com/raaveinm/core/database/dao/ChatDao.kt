package com.raaveinm.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.raaveinm.core.database.entities.api.user.Users
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

    @Query("SELECT steamId, communityVisibilityState, profileState, personaName, commentPermission, profileUrl, avatar, " +
            "       avatarMedium, avatarFull, avatarHash, lastLogOff, personaState, realName, primaryClanId, timeCreated, " +
            "       personaStateFlags, locCountryCode, locStateCode, locCityId, gameExtraInfo, gameId, fetchedAt " +
            "from SteamFriends JOIN main.Users U on SteamFriends.friendSteamId = U.steamId where userSteamId = :userId;") fun getUserFriends(userId: Long): Flow<List<Users>>
}