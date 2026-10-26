package com.raaveinm.core.database

//
// Created by Kirill "Raaveinm" on 10/11/26.
//

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.raaveinm.core.database.dao.ChatDao
import com.raaveinm.core.database.dao.ContactDao
import com.raaveinm.core.database.entities.chat.ServerConversation
import com.raaveinm.core.database.entities.chat.ServerMessage
import com.raaveinm.core.database.entities.server.Servers
import com.raaveinm.core.database.entities.social.ContactRequests
import com.raaveinm.core.database.entities.social.Contacts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/*
 * What a sign-out leaves on disk. Real SQLite, because the wipe has to get past the
 * foreign keys between messages, conversations and users.
 */
class AccountDaoTest {
    private lateinit var dbFile: File
    private lateinit var db: PicassoDatabase
    private lateinit var chat: ChatDao
    private lateinit var contacts: ContactDao

    private val self = 100L
    private val friend = 200L
    private val server = 1L

    @BeforeTest
    fun setUp() = runBlocking {
        dbFile = File.createTempFile("picasso-account-test", ".db")
        db = Room.databaseBuilder<PicassoDatabase>(name = dbFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        chat = db.getChatDao()
        contacts = db.getContactDao()
        db.getServerDao().addServer(Servers(id = server, url = "localhost:8000", name = "test", added = 0))
    }

    @AfterTest
    fun tearDown() {
        db.close()
        dbFile.delete()
    }

    @Test
    fun `wiping drops every conversation, message, contact and user but keeps the servers`() = runBlocking {
        val dm = chat.upsertServerConversation(
            server,
            ServerConversation(remoteId = 10, isDm = true, name = null, members = listOf(self, friend), writable = true),
            self
        )
        val palette = chat.upsertServerConversation(
            server,
            ServerConversation(remoteId = 20, isDm = false, name = "squad", members = listOf(self, friend, 300L), writable = true),
            self
        )
        chat.applyServerMessages(dm, listOf(ServerMessage(1, friend, "c1", "secret", 1000)))
        chat.applyServerMessages(palette, listOf(ServerMessage(2, self, "c2", "also secret", 2000)))
        contacts.replaceAll(
            serverId = server,
            contacts = listOf(Contacts(server, friend, "ally", 1)),
            requests = listOf(ContactRequests(server, 400, incoming = true, createdAt = 3)),
            invites = emptyList()
        )

        db.getAccountDao().wipeAccountData()

        assertTrue(chat.observeChats().first().isEmpty())
        assertTrue(chat.observePalettes().first().isEmpty())
        assertTrue(chat.getChatHistory(dm, 0, 100).isEmpty())
        assertTrue(chat.getChatHistory(palette, 0, 100).isEmpty())
        assertTrue(chat.getStubSteamIds().isEmpty())
        assertTrue(contacts.observeContacts(server).first().isEmpty())
        assertTrue(contacts.observeRequests(server, incoming = true).first().isEmpty())
        assertEquals(listOf(server), db.getServerDao().getAllServers().first().map { it.id })   // needed to sign in again
    }

    @Test
    fun `wiping an empty cache is a no-op`() = runBlocking {
        db.getAccountDao().wipeAccountData()

        assertEquals(1, db.getServerDao().getAllServers().first().size)
    }

    @Test
    fun `wiping server data drops the chat side and the sync cursor but keeps the Steam side`() = runBlocking {
        val dm = chat.upsertServerConversation(
            server,
            ServerConversation(remoteId = 10, isDm = true, name = null, members = listOf(self, friend), writable = true),
            self
        )
        chat.applyServerMessages(dm, listOf(ServerMessage(1, friend, "c1", "hello", 1000)))
        contacts.replaceAll(
            serverId = server,
            contacts = listOf(Contacts(server, friend, "ally", 1)),
            requests = emptyList(),
            invites = emptyList()
        )
        db.getServerDao().updateDeletedCursor(server, 42L)

        db.getAccountDao().wipeServerData()

        assertTrue(chat.observeChats().first().isEmpty())
        assertTrue(chat.getChatHistory(dm, 0, 100).isEmpty())
        assertTrue(contacts.observeContacts(server).first().isEmpty())
        assertNull(db.getServerDao().getDeletedCursor(server))
        assertEquals(listOf(server), db.getServerDao().getAllServers().first().map { it.id })
        assertEquals(setOf(self, friend), chat.getStubSteamIds().toSet())      // placeholder user rows are Steam side: not touched
    }
}
