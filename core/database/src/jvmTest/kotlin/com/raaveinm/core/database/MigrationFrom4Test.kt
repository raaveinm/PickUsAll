package com.raaveinm.core.database

//
// Created by Kirill "Raaveinm" on 10/9/26.
//

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.raaveinm.core.database.entities.chat.ServerConversation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MigrationFrom4Test {
    private val schemaDir = File("schemas/com.raaveinm.core.database.PicassoDatabase")

    private fun statementsOf(version: Int): List<String> {
        val lines = File(schemaDir, "$version.json").readLines()
        val statements = mutableListOf<String>()
        var table = ""
        for (line in lines) {
            val trimmed = line.trim().removeSuffix(",")
            Regex("\"tableName\": \"(.*)\"").matchEntire(trimmed)?.let { table = it.groupValues[1] }
            Regex("\"createSql\": \"(.*)\"").matchEntire(trimmed)?.let {
                statements += it.groupValues[1].replace("\${TABLE_NAME}", table)
            }
            if (trimmed.startsWith("\"CREATE TABLE IF NOT EXISTS room_master_table") ||
                trimmed.startsWith("\"INSERT OR REPLACE INTO room_master_table")
            ) {
                statements += trimmed.removePrefix("\"").removeSuffix("\"")
            }
        }
        return statements
    }

    @Test
    fun `a v4 database upgrades to v5, drops the placeholder chat cache and keeps everything else`() = runBlocking {
        val file = File.createTempFile("picasso-migration", ".db")
        val driver = BundledSQLiteDriver()
        try {
            driver.open(file.absolutePath).use { connection ->
                statementsOf(4).forEach { connection.execSQL(it) }
                connection.execSQL("PRAGMA user_version = 4")
                connection.execSQL("INSERT INTO Servers (id, url, name, added) VALUES (1, 'localhost:8000', 'dev', 0)")
                connection.execSQL(
                    "INSERT INTO Users (steamId, communityVisibilityState, personaName, commentPermission, profileUrl, " +
                        "avatar, avatarMedium, avatarFull, avatarHash, personaState, fetchedAt) " +
                        "VALUES (7, 1, 'peer', 0, '', '', '', '', '', 0, 1)"
                )
                connection.execSQL("INSERT INTO Conversations (id, serverId, kind, lastMessage, remoteId) VALUES (1, 1, 'chat', 'hey', 7)")
                connection.execSQL("INSERT INTO Chats (conversationId, chatTitleSteamId) VALUES (1, 7)")
                connection.execSQL(
                    "INSERT INTO MessageData (id, conversationId, senderSteamId, textMessage, timestamp, status) " +
                        "VALUES (1, 1, 7, 'old', 1700000000, 'FAILED')"
                )
            }

            val db = Room.databaseBuilder<PicassoDatabase>(name = file.absolutePath)
                .setDriver(driver)
                .setQueryCoroutineContext(Dispatchers.IO)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
            try {
                // the first query opens the database: migration, then Room's schema validation
                assertTrue(db.getChatDao().getCursors(1).isEmpty(), "the placeholder conversations are gone")
                assertTrue(db.getChatDao().observeChats().first().isEmpty())
                assertEquals(0, db.getChatDao().countMessages(1))

                // what is not chat cache survives
                assertEquals(listOf("localhost:8000"), db.getServerDao().getAllServers().first().map { it.url })
                assertEquals(null, db.getServerDao().getDeletedCursor(1))
                assertEquals(7L, db.getUserDao().observeUser(7).first()?.steamId)

                // and the upgraded database takes the new shape: a server-issued conversation id that
                // happens to equal the old placeholder must not collide with anything
                val id = db.getChatDao().upsertServerConversation(
                    1, ServerConversation(7, isDm = true, name = null, members = listOf(7, 8), writable = true), selfSteamId = 8
                )
                assertEquals(id, db.getChatDao().localIdOf(1, 7))
                assertTrue(db.getChatDao().observeChats().first().single().conversation.writable)
                assertTrue(db.getContactDao().observeContacts(1).first().isEmpty())
            } finally {
                db.close()
            }
        } finally {
            file.delete()
        }
    }
}
