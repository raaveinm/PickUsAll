package com.raaveinm.core.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import com.raaveinm.core.database.dao.ChatDao
import com.raaveinm.core.database.dao.GameDao
import com.raaveinm.core.database.dao.ServerDao
import com.raaveinm.core.database.dao.UserDao
import com.raaveinm.core.database.entities.api.game.Categories
import com.raaveinm.core.database.entities.api.game.CommunityContent
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
import com.raaveinm.core.database.entities.server.Servers

@Database(entities = [
    // server
    Servers::class,
    // chat
    Chats::class, Conversations::class, MessageData::class, PaletteMembers::class, Palettes::class,
    //api - games
    Categories::class, CommunityContent::class, GameAchievements::class, GameCategories::class,
    GameDevelopers::class, GameGenres::class, GameMedia::class, GamePublishers::class, Games::class,
    Genres::class,
    // api - user
    OwnedGames::class, SteamFriends::class, UserAchievements::class, Users::class
                     ], version = 1)
@ConstructedBy(DatabaseConstructor::class)
abstract class Database : RoomDatabase() {
    abstract fun getChatDao(): ChatDao
    abstract fun getGameDao(): GameDao
    abstract fun getServerDao(): ServerDao
    abstract fun getUserDao(): UserDao
}
@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object DatabaseConstructor : RoomDatabaseConstructor<RoomDatabase> {
    override fun initialize(): RoomDatabase
}