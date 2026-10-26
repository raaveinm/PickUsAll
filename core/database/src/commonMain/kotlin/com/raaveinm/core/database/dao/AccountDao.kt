package com.raaveinm.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction

//
// Created by Kirill "Raaveinm" on 10/11/26.
// Copyright (c) 2026 RetrogradeMercury. All rights reserved.
//

/**
 * Everything the cache holds about the signed-in account, as one unit to throw away.
 *
 * Kept: `Servers` (the user's own list of backends, needed to sign in again) and the
 * public Steam store catalog (`Games` and its category/genre/media/achievement-schema
 * tables) - neither says anything about who was signed in.
 */
@Dao
interface AccountDao {
    @Query("DELETE FROM MessageData")
    suspend fun clearMessages()

    @Query("DELETE FROM Chats")
    suspend fun clearChats()

    @Query("DELETE FROM PaletteMembers")
    suspend fun clearPaletteMembers()

    @Query("DELETE FROM Palettes")
    suspend fun clearPalettes()

    @Query("DELETE FROM Conversations")
    suspend fun clearConversations()

    @Query("DELETE FROM Contacts")
    suspend fun clearContacts()

    @Query("DELETE FROM ContactRequests")
    suspend fun clearContactRequests()

    @Query("DELETE FROM PaletteInvites")
    suspend fun clearPaletteInvites()

    @Query("DELETE FROM GameQueue")
    suspend fun clearGameQueue()

    @Query("DELETE FROM SteamFriends")
    suspend fun clearSteamFriends()

    @Query("DELETE FROM UserAchievements")
    suspend fun clearUserAchievements()

    @Query("DELETE FROM OwnedGames")
    suspend fun clearOwnedGames()

    @Query("DELETE FROM Users")
    suspend fun clearUsers()

    /** Forgets how far any server was synced, so the next connection pulls everything again. */
    @Query("UPDATE Servers SET deletedCursor = NULL")
    suspend fun clearSyncCursors()

    /**
     * Only what is tied to *a server*: conversations, messages, palettes, contacts and the sync
     * cursors. The Steam side (`Users`, friends, libraries, `GameQueue`) is kept, because it
     * describes the Steam account and not the server it was chatted on. Unsent messages go too.
     * Children before parents, so no foreign key is relied on to cascade.
     */
    @Transaction
    suspend fun wipeServerData() {
        clearMessages()
        clearChats()
        clearPaletteMembers()
        clearPalettes()
        clearConversations()
        clearContacts()
        clearContactRequests()
        clearPaletteInvites()
        clearSyncCursors()
    }

    @Transaction
    suspend fun wipeAccountData() {
        wipeServerData()
        clearGameQueue()
        clearSteamFriends()
        clearUserAchievements()
        clearOwnedGames()
        clearUsers()
    }
}
