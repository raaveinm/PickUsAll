package com.raaveinm.core.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.migration.Migration
import androidx.sqlite.execSQL
import com.raaveinm.core.database.dao.ChatDao
import com.raaveinm.core.database.dao.ContactDao
import com.raaveinm.core.database.dao.GameDao
import com.raaveinm.core.database.dao.ServerDao
import com.raaveinm.core.database.dao.UserDao
import com.raaveinm.core.database.entities.api.game.Categories
import com.raaveinm.core.database.entities.api.game.GameAchievements
import com.raaveinm.core.database.entities.api.game.GameCategories
import com.raaveinm.core.database.entities.api.game.GameDevelopers
import com.raaveinm.core.database.entities.api.game.GameGenres
import com.raaveinm.core.database.entities.api.game.GameMedia
import com.raaveinm.core.database.entities.api.game.GamePublishers
import com.raaveinm.core.database.entities.api.game.Games
import com.raaveinm.core.database.entities.api.game.Genres
import com.raaveinm.core.database.entities.api.user.OwnedGames
import com.raaveinm.core.database.entities.api.user.SteamFriends
import com.raaveinm.core.database.entities.api.user.UserAchievements
import com.raaveinm.core.database.entities.api.user.Users
import com.raaveinm.core.database.entities.chat.Chats
import com.raaveinm.core.database.entities.chat.Conversations
import com.raaveinm.core.database.entities.chat.MessageData
import com.raaveinm.core.database.entities.chat.PaletteMembers
import com.raaveinm.core.database.entities.chat.Palettes
import com.raaveinm.core.database.entities.game.GameQueue
import com.raaveinm.core.database.entities.server.Servers
import com.raaveinm.core.database.entities.social.ContactRequests
import com.raaveinm.core.database.entities.social.Contacts
import com.raaveinm.core.database.entities.social.PaletteInvites

@Database(entities = [
    // server
    Servers::class,
    // chat
    Chats::class, Conversations::class, MessageData::class, PaletteMembers::class, Palettes::class,
    //api - games
    Categories::class, GameAchievements::class, GameCategories::class,
    GameDevelopers::class, GameGenres::class, GameMedia::class, GamePublishers::class, Games::class,
    Genres::class,
    // api - user
    OwnedGames::class, SteamFriends::class, UserAchievements::class, Users::class,
    // game
    GameQueue::class,
    // social - the contact graph, as the server last described it
    Contacts::class, ContactRequests::class, PaletteInvites::class
                     ], version = 5)
@ColumnTypeConverters(RoomConverters::class)
@ConstructedBy(DatabaseConstructor::class)
abstract class PicassoDatabase : RoomDatabase() {
    abstract fun getChatDao(): ChatDao
    abstract fun getContactDao(): ContactDao
    abstract fun getGameDao(): GameDao
    abstract fun getServerDao(): ServerDao
    abstract fun getUserDao(): UserDao
}
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object DatabaseConstructor : RoomDatabaseConstructor<PicassoDatabase> {
    override fun initialize(): PicassoDatabase
}

val MIGRATION_1_2 = Migration(1, 2) { connection ->
    connection.execSQL("DROP TABLE IF EXISTS `CommunityContent`")
}

val MIGRATION_2_3 = Migration(2, 3) { connection ->
    connection.execSQL("ALTER TABLE MessageData ADD COLUMN status TEXT NOT NULL DEFAULT 'SENT'")
}

val MIGRATION_3_4 = Migration(3, 4) { connection ->
    connection.execSQL("ALTER TABLE Servers ADD COLUMN ping INT DEFAULT NULL")
    connection.execSQL(
        """
        CREATE TABLE GameQueue(
            id INTEGER NOT NULL,
            userId INTEGER NOT NULL
                references Users(steamId)
                    on delete cascade,
            gameId INTEGER NOT NULL
                references Games(steamAppId)
                    on delete no action,
            priority INTEGER NOT NULL,
            primary key (id, userId)
        )
        """.trimIndent()
    )
    connection.execSQL("CREATE INDEX index_GameQueue_userId_priority ON GameQueue (userId, priority)")
}

/*
 * v4 -> v5: the chat cache learns the server's identity for messages and conversations
 * (chat-sync-contract.md). The cache is disposable - the server is SSOT - and every
 * Conversations.remoteId written before now is a placeholder (the peer's steamId), which
 * could collide with a real server-issued id. So the cached chat data is wiped rather
 * than migrated: nothing of value is lost, and the first sync refills it.
 *
 * The DDL below is copied from schemas/.../5.json - Room validates the result against it.
 */
val MIGRATION_4_5 = Migration(4, 5) { connection ->
    // children first, so the foreign keys never see a dangling row
    connection.execSQL("DROP TABLE IF EXISTS `MessageData`")
    connection.execSQL("DELETE FROM `PaletteMembers`")
    connection.execSQL("DELETE FROM `Palettes`")
    connection.execSQL("DELETE FROM `Chats`")
    connection.execSQL("DELETE FROM `Conversations`")

    connection.execSQL(
        "CREATE TABLE IF NOT EXISTS `MessageData` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "`conversationId` INTEGER NOT NULL, `senderSteamId` INTEGER NOT NULL, `textMessage` TEXT NOT NULL, " +
            "`timestamp` INTEGER NOT NULL, `status` TEXT NOT NULL, `clientMessageId` TEXT NOT NULL, " +
            "`remoteId` INTEGER, FOREIGN KEY(`conversationId`) REFERENCES `Conversations`(`id`) " +
            "ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`senderSteamId`) REFERENCES `Users`(`steamId`) " +
            "ON UPDATE NO ACTION ON DELETE NO ACTION )"
    )
    connection.execSQL("CREATE INDEX IF NOT EXISTS `index_MessageData_conversationId_timestamp` ON `MessageData` (`conversationId`, `timestamp`)")
    connection.execSQL("CREATE INDEX IF NOT EXISTS `index_MessageData_senderSteamId` ON `MessageData` (`senderSteamId`)")
    connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_MessageData_conversationId_remoteId` ON `MessageData` (`conversationId`, `remoteId`)")
    connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_MessageData_clientMessageId` ON `MessageData` (`clientMessageId`)")

    connection.execSQL("ALTER TABLE `Conversations` ADD COLUMN `historyExhausted` INTEGER NOT NULL DEFAULT 0")
    connection.execSQL("ALTER TABLE `Conversations` ADD COLUMN `writable` INTEGER NOT NULL DEFAULT 1")
    connection.execSQL("ALTER TABLE `Servers` ADD COLUMN `deletedCursor` INTEGER")

    connection.execSQL(
        "CREATE TABLE IF NOT EXISTS `Contacts` (`serverId` INTEGER NOT NULL, `steamId` INTEGER NOT NULL, " +
            "`level` TEXT NOT NULL, `since` INTEGER NOT NULL, PRIMARY KEY(`serverId`, `steamId`), " +
            "FOREIGN KEY(`serverId`) REFERENCES `Servers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
    )
    connection.execSQL(
        "CREATE TABLE IF NOT EXISTS `ContactRequests` (`serverId` INTEGER NOT NULL, `steamId` INTEGER NOT NULL, " +
            "`incoming` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`serverId`, `steamId`, `incoming`), " +
            "FOREIGN KEY(`serverId`) REFERENCES `Servers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
    )
    connection.execSQL(
        "CREATE TABLE IF NOT EXISTS `PaletteInvites` (`serverId` INTEGER NOT NULL, `conversationRemoteId` INTEGER NOT NULL, " +
            "`name` TEXT NOT NULL, `inviterSteamId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, " +
            "PRIMARY KEY(`serverId`, `conversationRemoteId`), " +
            "FOREIGN KEY(`serverId`) REFERENCES `Servers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
    )
}
