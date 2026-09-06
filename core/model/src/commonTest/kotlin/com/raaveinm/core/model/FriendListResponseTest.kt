package com.raaveinm.core.model

import com.raaveinm.core.model.responses.GetFriendListResponse
import com.raaveinm.core.model.responses.GetPlayerSummariesResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Same configuration the Ktor client is installed with in shared's Koin module.
private val json = Json { ignoreUnknownKeys = true }

class FriendListResponseTest {

    @Test
    fun `parses a friend list`() {
        val body = """
            {"friendslist":{"friends":[
              {"steamid":"76561197960265731","relationship":"friend","friend_since":1503073844},
              {"steamid":"76561198966516520","relationship":"friend","friend_since":0}
            ]}}
        """.trimIndent()

        val friends = json.decodeFromString<GetFriendListResponse>(body).friendsList?.friends

        assertEquals(2, friends?.size)
        // Steam quotes the 64-bit ids; they still have to land as Longs.
        assertEquals(76561197960265731L, friends?.first()?.steamId)
        assertEquals(1503073844L, friends?.first()?.friendSince)
    }

    @Test
    fun `a hidden friend list is an empty list, not a failure`() {
        val friends = json.decodeFromString<GetFriendListResponse>("{}").friendsList?.friends

        assertTrue(friends.isNullOrEmpty())
    }

    @Test
    fun `parses a batched player summaries response`() {
        val body = """
            {"response":{"players":[
              {"steamid":"76561198966516520","communityvisibilitystate":3,"profilestate":1,
               "personaname":"raaveinm","commentpermission":1,
               "profileurl":"https://steamcommunity.com/id/raaveinm/","avatar":"a.jpg",
               "avatarmedium":"a_medium.jpg","avatarfull":"a_full.jpg","avatarhash":"a",
               "personastate":0},
              {"steamid":"76561197960265731","communityvisibilitystate":1,"personaname":"private",
               "profileurl":"https://steamcommunity.com/profiles/76561197960265731/",
               "avatar":"b.jpg","avatarmedium":"b_medium.jpg","avatarfull":"b_full.jpg",
               "avatarhash":"b","personastate":0}
            ]}}
        """.trimIndent()

        val players = json.decodeFromString<GetPlayerSummariesResponse>(body).response.players

        assertEquals(2, players.size)
        assertEquals(76561198966516520L, players.first().steamId)
        // A private profile drops most fields - it still has to map, or its friend row
        // would be thrown away by FriendsRepository for having no cached profile.
        assertEquals("private", players[1].personaName)
        assertEquals(0, players[1].commentPermission)
    }
}
