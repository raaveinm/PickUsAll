package com.raaveinm.picasso

//
// Created by Kirill "Raaveinm" on 10/9/26.
//

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.raaveinm.core.database.PicassoDatabase
import com.raaveinm.core.database.entities.server.Servers
import com.raaveinm.core.model.chat.MessageStatus
import com.raaveinm.core.model.social.ContactLevel
import com.raaveinm.features.impl_webrtc.signaling.SignalingClient
import com.raaveinm.picasso.data.repository.ChatRepository
import com.raaveinm.picasso.data.repository.ContactsRepository
import com.raaveinm.picasso.data.repository.ConversationResult
import com.raaveinm.picasso.data.repository.ProfileHydrator
import com.raaveinm.picasso.data.repository.SocialResult
import com.raaveinm.picasso.data.server.ApiResult
import com.raaveinm.picasso.data.server.ChatEnvelope
import com.raaveinm.picasso.data.server.ChatFrameType
import com.raaveinm.picasso.data.server.ChatSocket
import com.raaveinm.picasso.data.server.ContactsApi
import com.raaveinm.picasso.data.server.ConversationsApi
import com.raaveinm.picasso.data.server.OutgoingChat
import com.raaveinm.picasso.data.server.ServerContext
import com.raaveinm.picasso.data.server.ServerContextSource
import com.raaveinm.picasso.data.server.SyncRequest
import com.raaveinm.picasso.data.server.toServerConversation
import com.raaveinm.picasso.data.server.toServerMessage
import com.raaveinm.picasso.data.sync.ConnectionState
import com.raaveinm.picasso.data.sync.SyncCoordinator
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.junit.Assume.assumeTrue
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class ChatEndToEndTest {
    private val address: String? = TestServer.address
    private val devices = mutableListOf<Device>()

    @BeforeTest
    fun requireServer() {
        assumeTrue("PICASSO_TEST_SERVER is not set", address != null)
    }

    @AfterTest
    fun cleanUp() {
        devices.forEach { it.close() }
        devices.clear()
    }

    ///////////////////////////////////////////////
    // Live delivery
    ///////////////////////////////////////////////

    @Test
    fun `a message travels live from one user to another and both ends settle`() = runBlocking {
        val (alice, bob, conversation) = alliesWithDm()

        val localId = alice.chat.sendMessage(conversation.aliceLocalId, alice.steamId, "hello bob")

        eventually("alice's message is acknowledged") {
            alice.db.getChatDao().getMessage(localId)?.status == MessageStatus.SENT
        }
        val sent = alice.db.getChatDao().getMessage(localId)!!
        assertNotNull(sent.remoteId)
        assertTrue(sent.timestamp > 1_700_000_000_000L)
        assertEquals(1, alice.db.getChatDao().countMessages(conversation.aliceLocalId))

        eventually("bob receives it") { bob.db.getChatDao().countMessages(conversation.bobLocalId) == 1 }
        val received = bob.db.getChatDao().getChatHistory(conversation.bobLocalId, 0, 10).single()
        assertEquals("hello bob", received.message.textMessage)
        assertEquals(alice.steamId, received.message.senderSteamId)       // stamped by the server from alice's connection
        assertEquals(sent.remoteId, received.message.remoteId)
        assertEquals("hello bob", bob.db.getChatDao().observeChats().first().single().conversation.lastMessage)

        bob.chat.sendMessage(conversation.bobLocalId, bob.steamId, "hi alice")
        eventually("alice receives the reply") { alice.db.getChatDao().countMessages(conversation.aliceLocalId) == 2 }
        assertEquals("hi alice", alice.db.getChatDao().getChatHistory(conversation.aliceLocalId, 0, 1).single().message.textMessage)
    }

    @Test
    fun `the senders other device receives the message and the sending device does not get it twice`() = runBlocking {
        val (alice, bob, conversation) = alliesWithDm()
        val alicesTablet = device(alice.number, deviceNumber = 2)
        alicesTablet.contacts.refresh(alicesTablet.context)
        alicesTablet.chat.sync(alicesTablet.context)                       // the second device learns the conversation
        val tabletConversation = alicesTablet.db.getChatDao().localIdOf(1, conversation.remoteId)!!

        alice.chat.sendMessage(conversation.aliceLocalId, alice.steamId, "from the phone")

        eventually("the tablet gets the phone's message") { alicesTablet.db.getChatDao().countMessages(tabletConversation) == 1 }
        eventually("bob gets it") { bob.db.getChatDao().countMessages(conversation.bobLocalId) == 1 }
        delay(500.milliseconds)                                                          // long enough for a wrong second delivery to land
        assertEquals(1, alice.db.getChatDao().countMessages(conversation.aliceLocalId))
        assertEquals(1, alicesTablet.db.getChatDao().countMessages(tabletConversation))
    }

    @Test
    fun `a message resent with the same key is stored once and acked twice`() = runBlocking {
        val (alice, bob, conversation) = alliesWithDm()
        val localId = alice.chat.sendMessage(conversation.aliceLocalId, alice.steamId, "once")
        eventually("first ack") { alice.db.getChatDao().getMessage(localId)?.status == MessageStatus.SENT }
        val key = alice.db.getChatDao().getMessage(localId)!!.clientMessageId

        // the lost-ack case: the client never saw the ack and sends the very same frame again
        alice.socket.send(
            ChatEnvelope(
                type = ChatFrameType.CHAT_MESSAGE,
                chatMessage = OutgoingChat(conversation.remoteId.toString(), key, "once")
            )
        )
        delay(700)

        assertEquals(1, bob.db.getChatDao().countMessages(conversation.bobLocalId))        // not delivered a second time
        assertEquals(1, alice.db.getChatDao().countMessages(conversation.aliceLocalId))    // and no second row
    }

    @Test
    fun `a delete reaches the other side silently`() = runBlocking {
        val (alice, bob, conversation) = alliesWithDm()
        val localId = alice.chat.sendMessage(conversation.aliceLocalId, alice.steamId, "oops")
        eventually("delivered") { bob.db.getChatDao().countMessages(conversation.bobLocalId) == 1 }
        eventually("acked") { alice.db.getChatDao().getMessage(localId)?.status == MessageStatus.SENT }

        assertTrue(alice.chat.deleteMessage(localId))

        assertEquals(0, alice.db.getChatDao().countMessages(conversation.aliceLocalId))
        eventually("bob's copy is gone") { bob.db.getChatDao().countMessages(conversation.bobLocalId) == 0 }
        assertEquals(null, bob.db.getChatDao().observeChats().first().single().conversation.lastMessage)
    }

    @Test
    fun `only the sender can delete a message`() = runBlocking {
        val (alice, bob, conversation) = alliesWithDm()
        val localId = alice.chat.sendMessage(conversation.aliceLocalId, alice.steamId, "mine")
        eventually("delivered") { bob.db.getChatDao().countMessages(conversation.bobLocalId) == 1 }
        eventually("acked") { alice.db.getChatDao().getMessage(localId)?.status == MessageStatus.SENT }
        val bobsRow = bob.db.getChatDao().getChatHistory(conversation.bobLocalId, 0, 1).single().message

        assertTrue(!bob.chat.deleteMessage(bobsRow.id))                     // the server refuses: not his message

        assertEquals(1, bob.db.getChatDao().countMessages(conversation.bobLocalId))
        assertEquals(1, alice.db.getChatDao().countMessages(conversation.aliceLocalId))
    }

    ///////////////////////////////////////////////
    // Catching up
    ///////////////////////////////////////////////

    @Test
    fun `a device that was offline catches up on messages and deletions when it reconnects`() = runBlocking {
        val (alice, bob, conversation) = alliesWithDm()
        val doomed = alice.chat.sendMessage(conversation.aliceLocalId, alice.steamId, "will be deleted")
        eventually("bob has the first message") { bob.db.getChatDao().countMessages(conversation.bobLocalId) == 1 }
        eventually("acked") { alice.db.getChatDao().getMessage(doomed)?.status == MessageStatus.SENT }

        bob.goOffline()
        alice.chat.sendMessage(conversation.aliceLocalId, alice.steamId, "missed 1")
        alice.chat.sendMessage(conversation.aliceLocalId, alice.steamId, "missed 2")
        eventually("alice's messages are acked") {
            alice.db.getChatDao().getPendingOutgoing(1).isEmpty()
        }
        assertTrue(alice.chat.deleteMessage(doomed))                        // and one deletion bob also missed
        assertEquals(1, bob.db.getChatDao().countMessages(conversation.bobLocalId))   // bob saw none of it

        bob.goOnline()

        eventually("bob caught up") {
            bob.db.getChatDao().getChatHistory(conversation.bobLocalId, 0, 10).map { it.message.textMessage } ==
                listOf("missed 2", "missed 1")
        }
        assertEquals("missed 2", bob.db.getChatDao().observeChats().first().single().conversation.lastMessage)
    }

    @Test
    fun `a message typed while offline waits and goes out after the reconnect`() = runBlocking {
        val (alice, bob, conversation) = alliesWithDm()
        alice.goOffline()

        val localId = alice.chat.sendMessage(conversation.aliceLocalId, alice.steamId, "typed offline")
        assertEquals(MessageStatus.PENDING, alice.db.getChatDao().getMessage(localId)!!.status)   // waiting, NOT failed
        delay(300)
        assertEquals(0, bob.db.getChatDao().countMessages(conversation.bobLocalId))

        alice.goOnline()

        eventually("it is acknowledged") { alice.db.getChatDao().getMessage(localId)?.status == MessageStatus.SENT }
        eventually("bob receives it") { bob.db.getChatDao().countMessages(conversation.bobLocalId) == 1 }
        assertEquals(1, alice.db.getChatDao().countMessages(conversation.aliceLocalId))
    }

    @Test
    fun `a new conversation appears on the other device without any action`() = runBlocking {
        val alice = device()
        val bob = device()
        befriend(alice, bob)

        assertTrue(alice.chat.createDm(bob.steamId) is ConversationResult.Ready)

        eventually("bob has the dm") { bob.db.getChatDao().observeChats().first().size == 1 }
    }

    @Test
    fun `older history is fetched on demand when the cache runs out`() = runBlocking {
        val (alice, bob, conversation) = alliesWithDm()
        repeat(5) { i -> alice.chat.sendMessage(conversation.aliceLocalId, alice.steamId, "m${i + 1}") }
        eventually("all five reach bob") { bob.db.getChatDao().countMessages(conversation.bobLocalId) == 5 }

        val fresh = device(bob.number, deviceNumber = 2)
        fresh.contacts.refresh(fresh.context)
        val page = (fresh.conversations.sync(fresh.context, SyncRequest(cursors = emptyList(), limit = 2)) as ApiResult.Ok).value
        val entry = page.conversations.single()
        assertEquals("reset", entry.mode)
        assertTrue(entry.hasMoreBefore)
        val local = fresh.db.getChatDao().upsertServerConversation(
            1, entry.conversation.toServerConversation()!!, fresh.steamId
        )
        fresh.db.getChatDao().resetConversationMessages(
            local, entry.messages.mapNotNull { it.toServerMessage() }, historyExhausted = !entry.hasMoreBefore
        )
        assertEquals(2, fresh.db.getChatDao().countMessages(local))

        assertTrue(fresh.chat.loadOlder(local))                             // scroll-up: the rest comes from the server

        assertEquals(5, fresh.db.getChatDao().countMessages(local))
        assertEquals(true, fresh.db.getChatDao().isHistoryExhausted(local))   // and it stops asking
        assertEquals(
            listOf("m5", "m4", "m3", "m2", "m1"),
            fresh.db.getChatDao().getChatHistory(local, 0, 10).map { it.message.textMessage }
        )
    }

    ///////////////////////////////////////////////
    // Permissions
    ///////////////////////////////////////////////

    @Test
    fun `after the pair stops being allies a send is refused for good and the dm shows as read-only`() = runBlocking {
        val (alice, bob, conversation) = alliesWithDm()
        assertEquals(SocialResult.OK, bob.contacts.remove(alice.steamId))
        eventually("alice's dm freezes") {
            alice.db.getChatDao().observeChats().first().single().conversation.writable == false
        }

        val localId = alice.chat.sendMessage(conversation.aliceLocalId, alice.steamId, "anyone there?")

        eventually("the server's nack fails the message") {
            alice.db.getChatDao().getMessage(localId)?.status == MessageStatus.FAILED
        }
        assertEquals(0, bob.db.getChatDao().countMessages(conversation.bobLocalId))
    }

    @Test
    fun `a blocked person cannot reach the blocker and is not told`() = runBlocking {
        val (alice, bob, conversation) = alliesWithDm()
        assertEquals(SocialResult.OK, bob.contacts.setLevel(alice.steamId, ContactLevel.IMPOSTER))
        eventually("alice's dm freezes") {
            alice.db.getChatDao().observeChats().first().single().conversation.writable == false
        }

        val localId = alice.chat.sendMessage(conversation.aliceLocalId, alice.steamId, "let me in")

        eventually("refused") { alice.db.getChatDao().getMessage(localId)?.status == MessageStatus.FAILED }
        assertEquals(0, bob.db.getChatDao().countMessages(conversation.bobLocalId))
        // from alice's side the contact simply vanished - exactly what a removal looks like
        assertTrue(alice.db.getContactDao().observeContacts(1).first().isEmpty())
    }

    ///////////////////////////////////////////////
    // Contacts pushed live
    ///////////////////////////////////////////////

    @Test
    fun `an ally request and its acceptance are pushed to the other side`() = runBlocking {
        val alice = device()
        val bob = device()

        assertEquals(SocialResult.PENDING, alice.contacts.request(bob.steamId))
        eventually("bob sees the request") { bob.db.getContactDao().observeRequests(1, incoming = true).first().size == 1 }

        assertEquals(SocialResult.OK, bob.contacts.accept(alice.steamId))
        eventually("alice sees the acceptance") { alice.db.getContactDao().observeContacts(1).first().size == 1 }
        assertTrue(bob.db.getContactDao().observeRequests(1, incoming = true).first().isEmpty())
    }

    ///////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////

    private data class Dm(val remoteId: Long, val aliceLocalId: Long, val bobLocalId: Long)

    private suspend fun alliesWithDm(): Triple<Device, Device, Dm> {
        val alice = device()
        val bob = device()
        befriend(alice, bob)
        val aliceReady = alice.chat.createDm(bob.steamId) as ConversationResult.Ready
        val bobReady = bob.chat.createDm(alice.steamId) as ConversationResult.Ready
        val remoteId = alice.db.getChatDao().remoteIdOf(aliceReady.localId)!!
        return Triple(alice, bob, Dm(remoteId, aliceReady.localId, bobReady.localId))
    }

    private suspend fun befriend(first: Device, second: Device) {
        assertEquals(SocialResult.PENDING, first.contacts.request(second.steamId))
        assertEquals(SocialResult.OK, second.contacts.accept(first.steamId))
        first.contacts.refresh(first.context)
    }

    private suspend fun device(userNumber: Int = TestServer.nextUser(), deviceNumber: Int = 1): Device =
        Device(userNumber, deviceNumber, address!!).also {
            devices += it
            it.open()
        }

    /** Polls until [condition] holds; a hung wait fails the test with [what] instead of hanging it. */
    private suspend fun eventually(what: String, timeoutMs: Long = 15_000, condition: suspend () -> Boolean) {
        try {
            withTimeout(timeoutMs) {
                while (!condition()) delay(50)
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            throw AssertionError("timed out waiting for: $what")
        }
    }

    /**
     * One user's app instance: own Room database, own HTTP client, own WebSocket, and the real
     * SyncCoordinator driving them. [deviceNumber] 2 is the same account on a second device.
     */
    private class Device(val number: Int, deviceNumber: Int, address: String) {
        val steamId = TestServer.steamIdOf(number)
        val context = ServerContext(
            serverId = 1,
            baseUrl = "http://$address",
            wsUrl = "ws://$address/ws",
            token = "tok-$number",
            selfSteamId = steamId
        )
        private val source = object : ServerContextSource {
            override val context: Flow<ServerContext?> = MutableStateFlow<ServerContext?>(this@Device.context)
        }

        private val dbFile: File = File.createTempFile("picasso-e2e-$number-$deviceNumber", ".db")
        val db: PicassoDatabase = Room.databaseBuilder<PicassoDatabase>(name = dbFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        private val http = HttpClient(OkHttp) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        private val signaling = SignalingClient()
        val socket = ChatSocket(signaling)
        val conversations = ConversationsApi(http)
        val chat = ChatRepository(db.getChatDao(), db.getServerDao(), source, conversations, socket)
        val contacts = ContactsRepository(
            db.getContactDao(), db.getChatDao(), source, ContactsApi(http), conversations, chat
        )

        private var scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        private var coordinator = newCoordinator()

        private fun newCoordinator() = SyncCoordinator(
            activeServer = source,
            socket = socket,
            chatRepository = chat,
            contactsRepository = contacts,
            profiles = ProfileHydrator { },
            scope = scope
        )

        suspend fun open() {
            db.getServerDao().addServer(Servers(id = 1, url = context.baseUrl.removePrefix("http://"), name = "e2e", added = 0))
            goOnline()
        }

        /** Starts the connection and waits until the device is caught up and live. */
        suspend fun goOnline() {
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            coordinator = newCoordinator()
            coordinator.start()
            withTimeout(15_000) { coordinator.state.first { it == ConnectionState.LIVE } }
        }

        /** Drops the socket and stops reconnecting: the app was closed / lost its network. */
        fun goOffline() {
            scope.cancel()
            signaling.close()
        }

        fun close() {
            goOffline()
            http.close()
            db.close()
            dbFile.delete()
        }
    }
}
