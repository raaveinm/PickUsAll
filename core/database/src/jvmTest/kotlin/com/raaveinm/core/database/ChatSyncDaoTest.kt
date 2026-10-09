package com.raaveinm.core.database

//
// Created by Kirill "Raaveinm" on 10/9/26.
//

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.raaveinm.core.database.dao.ChatDao
import com.raaveinm.core.database.dao.ContactDao
import com.raaveinm.core.database.entities.chat.MessageData
import com.raaveinm.core.database.entities.chat.ServerConversation
import com.raaveinm.core.database.entities.chat.ServerMessage
import com.raaveinm.core.database.entities.server.Servers
import com.raaveinm.core.database.entities.social.ContactRequests
import com.raaveinm.core.database.entities.social.Contacts
import com.raaveinm.core.database.entities.social.PaletteInvites
import com.raaveinm.core.model.chat.MessageStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/*
 * The cache half of chat sync (chat-sync-contract.md, section 8): how a sync page, a live
 * frame and an ack for the same message fold into Room without duplicating or losing
 * anything. Runs on the real SQLite driver because the interesting failures are
 * constraint failures (the unique indices, the foreign keys), which a fake would not have.
 */
class ChatSyncDaoTest {
    private lateinit var dbFile: File
    private lateinit var db: PicassoDatabase
    private lateinit var chat: ChatDao
    private lateinit var contacts: ContactDao

    private val self = 100L
    private val friend = 200L
    private val server = 1L

    @BeforeTest
    fun setUp() = runBlocking {
        dbFile = File.createTempFile("picasso-sync-test", ".db")
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

    private fun dm(remoteId: Long = 10, writable: Boolean = true) = ServerConversation(
        remoteId = remoteId,
        isDm = true,
        name = null,
        members = listOf(self, friend),
        writable = writable
    )

    private fun serverMessage(remoteId: Long, sender: Long = friend, client: String = "c$remoteId", at: Long = remoteId * 1000) =
        ServerMessage(remoteId, sender, client, "text $remoteId", at)

    private fun unsent(conversationId: Long, text: String, at: Long, status: MessageStatus, client: String) =
        MessageData(
            conversationId = conversationId, senderSteamId = self, textMessage = text, timestamp = at,
            status = status, clientMessageId = client
        )

    private suspend fun texts(conversationId: Long) =
        chat.getChatHistory(conversationId, 0, 100).map { it.message.textMessage }

    ///////////////////////////////////////////////
    // Conversations
    ///////////////////////////////////////////////

    @Test
    fun `a server dm becomes a conversation with the other participant as its title`() = runBlocking {
        val localId = chat.upsertServerConversation(server, dm(), self)

        val chats = chat.observeChats().first()
        assertEquals(1, chats.size)
        assertEquals(localId, chats[0].conversation.id)
        assertEquals(10L, chats[0].conversation.remoteId)       // the SERVER's id, not the peer's steamId
        assertEquals(friend, chats[0].chat.chatTitleSteamId)    // only the other participant is stored
        assertEquals("chat", chats[0].conversation.kind)        // client spelling, mapped from the wire's "dm"
        // the participants had never been fetched from Steam: placeholders keep the foreign keys satisfied
        assertEquals(setOf(self, friend), chat.getStubSteamIds().toSet())
    }

    @Test
    fun `upserting the same conversation twice is idempotent and follows writable`() = runBlocking {
        val first = chat.upsertServerConversation(server, dm(writable = true), self)
        val second = chat.upsertServerConversation(server, dm(writable = false), self)

        assertEquals(first, second)
        assertEquals(1, chat.observeChats().first().size)
        assertFalse(chat.observeChats().first()[0].conversation.writable)   // a frozen dm
    }

    @Test
    fun `a palette keeps its members and a rewrite replaces them`() = runBlocking {
        val palette = ServerConversation(20, isDm = false, name = "squad", members = listOf(self, friend), writable = true)
        chat.upsertServerConversation(server, palette, self)
        chat.upsertServerConversation(server, palette.copy(members = listOf(self, friend, 300)), self)

        val stored = chat.observePalettes().first().single()
        assertEquals("squad", stored.palette.name)
        assertEquals(setOf(self, friend, 300L), stored.members.map { it.steamId }.toSet())
    }

    @Test
    fun `conversations absent from a sync are dropped`() = runBlocking {
        chat.upsertServerConversation(server, dm(10), self)
        chat.upsertServerConversation(server, dm(11).copy(members = listOf(self, 300)), self)

        chat.deleteConversationsExcept(server, listOf(10))

        assertEquals(listOf(10L), chat.getCursors(server).map { it.remoteId })
    }

    ///////////////////////////////////////////////
    // Messages
    ///////////////////////////////////////////////

    @Test
    fun `server messages are inserted once however many times they arrive`() = runBlocking {
        val id = chat.upsertServerConversation(server, dm(), self)
        val page = listOf(serverMessage(1), serverMessage(2), serverMessage(3))

        chat.applyServerMessages(id, page)
        chat.applyServerMessages(id, page)                  // the same page again: a sync overlapping a live frame
        chat.applyServerMessages(id, listOf(serverMessage(3)))

        assertEquals(listOf("text 3", "text 2", "text 1"), texts(id))
        assertEquals("text 3", chat.observeChats().first()[0].conversation.lastMessage)
    }

    @Test
    fun `a message from someone never fetched from Steam still lands`() = runBlocking {
        val id = chat.upsertServerConversation(server, dm(), self)

        chat.applyServerMessages(id, listOf(serverMessage(1, sender = 999)))

        assertEquals(1, chat.getChatHistory(id, 0, 10).size)
        assertTrue(999L in chat.getStubSteamIds())
    }

    @Test
    fun `an ack resolves the pending row instead of adding a second one`() = runBlocking {
        val id = chat.upsertServerConversation(server, dm(), self)
        val localId = chat.insertMessage(
            MessageData(
                conversationId = id, senderSteamId = self, textMessage = "hello", timestamp = 5,
                status = MessageStatus.PENDING, clientMessageId = "mine-1"
            )
        )

        // the server's copy of the same message comes back carrying our clientMessageId
        chat.applyServerMessages(id, listOf(ServerMessage(7, self, "mine-1", "hello", 9_000)))

        val rows = chat.getChatHistory(id, 0, 10)
        assertEquals(1, rows.size)
        assertEquals(localId, rows[0].message.id)
        assertEquals(7L, rows[0].message.remoteId)
        assertEquals(MessageStatus.SENT, rows[0].message.status)
        assertEquals(9_000L, rows[0].message.timestamp)         // the server's clock replaces the device's
    }

    @Test
    fun `unsent messages sort ahead of confirmed ones`() = runBlocking {
        val id = chat.upsertServerConversation(server, dm(), self)
        chat.applyServerMessages(id, listOf(serverMessage(1), serverMessage(2)))
        chat.insertMessage(
            MessageData(
                conversationId = id, senderSteamId = self, textMessage = "typing", timestamp = 1,
                status = MessageStatus.PENDING, clientMessageId = "p"
            )
        )

        // even though its timestamp is the smallest, a message you just typed is the newest in the chat
        assertEquals(listOf("typing", "text 2", "text 1"), texts(id))
        chat.refreshLastMessage(id)         // the repository does this after every local insert
        assertEquals("typing", chat.observeChats().first()[0].conversation.lastMessage)
    }

    @Test
    fun `a reset replaces the confirmed range but keeps what was never sent`() = runBlocking {
        val id = chat.upsertServerConversation(server, dm(), self)
        chat.applyServerMessages(id, listOf(serverMessage(1), serverMessage(2)))
        chat.insertMessage(
            MessageData(
                conversationId = id, senderSteamId = self, textMessage = "queued", timestamp = 3,
                status = MessageStatus.PENDING, clientMessageId = "q"
            )
        )

        chat.resetConversationMessages(id, listOf(serverMessage(50), serverMessage(51)), historyExhausted = false)

        assertEquals(listOf("queued", "text 51", "text 50"), texts(id))
        assertEquals(false, chat.isHistoryExhausted(id))
    }

    @Test
    fun `cursors report the highest confirmed id per conversation`() = runBlocking {
        val a = chat.upsertServerConversation(server, dm(10), self)
        chat.upsertServerConversation(server, dm(11).copy(members = listOf(self, 300)), self)
        chat.applyServerMessages(a, listOf(serverMessage(4), serverMessage(9), serverMessage(6)))
        chat.insertMessage(
            MessageData(
                conversationId = a, senderSteamId = self, textMessage = "unsent", timestamp = 1,
                status = MessageStatus.PENDING, clientMessageId = "u"
            )
        )

        val cursors = chat.getCursors(server).associate { it.remoteId to it.newestMessageRemoteId }

        assertEquals(9L, cursors[10])
        assertNull(cursors[11])                                 // nothing held: the server sends a reset
    }

    @Test
    fun `a delete removes the message and the preview follows`() = runBlocking {
        val id = chat.upsertServerConversation(server, dm(), self)
        chat.applyServerMessages(id, listOf(serverMessage(1), serverMessage(2)))

        chat.deleteByRemoteId(id, 2)
        chat.refreshLastMessage(id)

        assertEquals(listOf("text 1"), texts(id))
        assertEquals("text 1", chat.observeChats().first()[0].conversation.lastMessage)
    }

    @Test
    fun `outbox queries see only what still has to be sent`() = runBlocking {
        val id = chat.upsertServerConversation(server, dm(), self)
        chat.insertMessage(unsent(id, "a", 1, MessageStatus.PENDING, "ca"))
        chat.insertMessage(unsent(id, "b", 2, MessageStatus.FAILED, "cb"))
        chat.applyServerMessages(id, listOf(serverMessage(1)))

        val pending = chat.getPendingOutgoing(server)

        assertEquals(1, pending.size)
        assertEquals("ca", pending[0].clientMessageId)
        assertEquals(10L, pending[0].conversationRemoteId)      // the id the server knows, not the Room one

        chat.updateStatusByClientId("ca", MessageStatus.FAILED)
        assertTrue(chat.getPendingOutgoing(server).isEmpty())
        // a confirmed message can never be marked failed by a late nack
        chat.updateStatusByClientId("c1", MessageStatus.FAILED)
        assertEquals(MessageStatus.SENT, chat.findByClientMessageId("c1")!!.status)
    }

    @Test
    fun `a failed message can be queued again by the user`() = runBlocking {
        val id = chat.upsertServerConversation(server, dm(), self)
        val rowId = chat.insertMessage(unsent(id, "a", 1, MessageStatus.FAILED, "ca"))

        chat.requeueFailed(rowId)

        assertEquals(MessageStatus.PENDING, chat.getMessage(rowId)!!.status)
    }

    @Test
    fun `deleting an unsent message never touches a confirmed one`() = runBlocking {
        val id = chat.upsertServerConversation(server, dm(), self)
        val unsent = chat.insertMessage(unsent(id, "a", 1, MessageStatus.FAILED, "ca"))
        chat.applyServerMessages(id, listOf(serverMessage(1)))
        val confirmed = chat.findByClientMessageId("c1")!!.id

        chat.deleteUnsent(unsent)
        chat.deleteUnsent(confirmed)

        assertNull(chat.getMessage(unsent))
        assertNotNull(chat.getMessage(confirmed))
        Unit        // JUnit needs a void test method; assertNotNull returns its argument
    }

    ///////////////////////////////////////////////
    // Contacts
    ///////////////////////////////////////////////

    @Test
    fun `contacts are replaced wholesale per server`() = runBlocking {
        contacts.replaceAll(
            server,
            contacts = listOf(Contacts(server, friend, "ally", 1), Contacts(server, 300, "imposter", 2)),
            requests = listOf(ContactRequests(server, 400, incoming = true, createdAt = 3)),
            invites = listOf(PaletteInvites(server, 20, "squad", friend, 4))
        )
        contacts.replaceAll(
            server,
            contacts = listOf(Contacts(server, friend, "friend", 5)),
            requests = emptyList(),
            invites = emptyList()
        )

        val stored = contacts.observeContacts(server).first()
        assertEquals(1, stored.size)
        assertEquals("friend", stored[0].contact.level)
        assertNull(stored[0].user)                              // no Steam profile fetched: shown as such, not dropped
        assertTrue(contacts.observeRequests(server, incoming = true).first().isEmpty())
        assertTrue(contacts.observePaletteInvites(server).first().isEmpty())
        assertEquals("friend", contacts.levelOf(server, friend))
        assertEquals(setOf(friend), contacts.allReferencedSteamIds(server).toSet())
    }
}
