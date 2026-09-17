package com.raaveinm.core.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.migration.Migration
import androidx.sqlite.execSQL
import com.raaveinm.core.database.dao.ChatDao
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
    GameQueue::class
                     ], version = 4)
@ColumnTypeConverters(RoomConverters::class)
@ConstructedBy(DatabaseConstructor::class)
abstract class PicassoDatabase : RoomDatabase() {
    abstract fun getChatDao(): ChatDao
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