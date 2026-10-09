package com.raaveinm.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.raaveinm.core.database.entities.social.ContactRequests
import com.raaveinm.core.database.entities.social.ContactWithUser
import com.raaveinm.core.database.entities.social.Contacts
import com.raaveinm.core.database.entities.social.InviteWithInviter
import com.raaveinm.core.database.entities.social.PaletteInvites
import com.raaveinm.core.database.entities.social.RequestWithUser
import kotlinx.coroutines.flow.Flow

//
// Created by Kirill "Raaveinm" on 10/9/26.
//

@Dao
interface ContactDao {
    @Transaction
    @Query("SELECT * FROM Contacts WHERE serverId = :serverId ORDER BY since DESC")
    fun observeContacts(serverId: Long): Flow<List<ContactWithUser>>

    @Transaction
    @Query("SELECT * FROM ContactRequests WHERE serverId = :serverId AND incoming = :incoming ORDER BY createdAt DESC")
    fun observeRequests(serverId: Long, incoming: Boolean): Flow<List<RequestWithUser>>

    @Transaction
    @Query("SELECT * FROM PaletteInvites WHERE serverId = :serverId ORDER BY createdAt DESC")
    fun observePaletteInvites(serverId: Long): Flow<List<InviteWithInviter>>

    @Query("SELECT level FROM Contacts WHERE serverId = :serverId AND steamId = :steamId")
    suspend fun levelOf(serverId: Long, steamId: Long): String?

    @Query("SELECT COUNT(*) > 0 FROM ContactRequests WHERE serverId = :serverId AND steamId = :steamId AND incoming = :incoming")
    suspend fun hasRequest(serverId: Long, steamId: Long, incoming: Boolean): Boolean

    @Query("SELECT steamId FROM Contacts WHERE serverId = :serverId UNION " +
            "SELECT steamId FROM ContactRequests WHERE serverId = :serverId UNION " +
            "SELECT inviterSteamId FROM PaletteInvites WHERE serverId = :serverId")
    suspend fun allReferencedSteamIds(serverId: Long): List<Long>

    @Query("DELETE FROM Contacts WHERE serverId = :serverId")
    suspend fun clearContacts(serverId: Long)

    @Query("DELETE FROM ContactRequests WHERE serverId = :serverId")
    suspend fun clearRequests(serverId: Long)

    @Query("DELETE FROM PaletteInvites WHERE serverId = :serverId")
    suspend fun clearPaletteInvites(serverId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<Contacts>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<ContactRequests>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaletteInvites(invites: List<PaletteInvites>)

    @Transaction
    suspend fun replaceAll(
        serverId: Long,
        contacts: List<Contacts>,
        requests: List<ContactRequests>,
        invites: List<PaletteInvites>
    ) {
        clearContacts(serverId)
        clearRequests(serverId)
        clearPaletteInvites(serverId)
        insertContacts(contacts)
        insertRequests(requests)
        insertPaletteInvites(invites)
    }
}
