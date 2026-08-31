package com.raaveinm.core.database

//
// Created by Claude on 8/31/26.
//

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.raaveinm.core.database.entities.api.user.Users
import com.raaveinm.core.database.entities.chat.Chats
import com.raaveinm.core.database.entities.chat.Conversations
import com.raaveinm.core.database.entities.chat.MessageData
import com.raaveinm.core.database.entities.chat.toDto
import com.raaveinm.core.database.entities.server.Servers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatHistoryPaginationTest {

    private lateinit var dbFile: File
    private lateinit var db: PicassoDatabase

    @BeforeTest
    fun setUp() {
        dbFile = File.createTempFile("picasso-test", ".db")
        db = Room.databaseBuilder<PicassoDatabase>(name = dbFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    @AfterTest
    fun tearDown() {
        db.close()
        dbFile.delete()
    }

    @Test
    fun `getChatHistory pages newest-first and joins sender info`() = runBlocking {
        val chatDao = db.getChatDao()
        val userDao = db.getUserDao()

        val self = testUser(steamId = 1L, name = "Self")
        val friend = testUser(steamId = 2L, name = "Friend")
        userDao.addUser(self)
        userDao.addUser(friend)

        db.getServerDao().addServer(Servers(id = 1, url = "https://example.test", name = "test", added = 0))
        val conversationId = chatDao.insertConversation(
            Conversations(serverId = 1, kind = "chat", lastMessage = null, remoteId = 100)
        )
        chatDao.insertChat(Chats(conversationId = conversationId, chatTitleSteamId = friend.steamId))

        // 25 messages, alternating senders, strictly increasing timestamp -> increasing id
        val messageCount = 25
        repeat(messageCount) { i ->
            chatDao.insertMessage(
                MessageData(
                    conversationId = conversationId,
                    senderSteamId = if (i % 2 == 0) self.steamId else friend.steamId,
                    textMessage = "message #$i",
                    timestamp = 1_700_000_000L + i
                )
            )
        }

        // first page: newest 10 (message #24 down to #15)
        val page1 = chatDao.getChatHistory(conversationId, offset = 0, limit = 10)
        assertEquals(10, page1.size)
        assertEquals("message #24", page1.first().message.textMessage)
        assertEquals("message #15", page1.last().message.textMessage)
        assertEquals(self.steamId, page1.first().sender.steamId) // #24 is even -> sent by self

        // second page: next 10 older (message #14 down to #5)
        val page2 = chatDao.getChatHistory(conversationId, offset = 10, limit = 10)
        assertEquals(10, page2.size)
        assertEquals("message #14", page2.first().message.textMessage)
        assertEquals("message #5", page2.last().message.textMessage)

        // third page: remaining 5 (message #4 down to #0) -> short page signals "no more"
        val page3 = chatDao.getChatHistory(conversationId, offset = 20, limit = 10)
        assertEquals(5, page3.size)
        assertTrue(page3.size < 10)
        assertEquals("message #0", page3.last().message.textMessage)

        // mapper hydrates the correct User and a HH:mm timestamp string
        val dto = page1.first().toDto()
        assertEquals("message #24", dto.textMessage)
        assertEquals(self.steamId, dto.user.steamId) // #24 -> i=24, even -> self
        assertTrue(Regex("""\d{2}:\d{2}""").matches(dto.timestamp))

        // accumulating pages the way the ViewModel does reconstructs the full, newest-first history
        val accumulated = (page1 + page2 + page3).map { it.message.textMessage }
        assertEquals((0 until messageCount).reversed().map { "message #$it" }, accumulated)
    }

    private fun testUser(steamId: Long, name: String) = Users(
        steamId = steamId,
        communityVisibilityState = 3,
        personaName = name,
        commentPermission = false,
        profileUrl = "https://example.test/$steamId",
        avatar = "",
        avatarMedium = "",
        avatarFull = "",
        avatarHash = "",
        personaState = 1,
        fetchedAt = 0
    )
}
