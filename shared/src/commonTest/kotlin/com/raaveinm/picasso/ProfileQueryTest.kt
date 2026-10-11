package com.raaveinm.picasso

import com.raaveinm.picasso.data.repository.ProfileQuery
import com.raaveinm.picasso.data.repository.parseProfileQuery
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

//
// Created by Kirill "Raaveinm" on 10/11/2026.
//

class ProfileQueryTest {
    private val id: Long = 76561198000000042L

    @Test
    fun steamId64IsTakenAsIs() {
        assertEquals(ProfileQuery.ById(id), parseProfileQuery("  $id "))
    }

    @Test
    fun shortNumbersAreNotSteamIds() {
        assertNull(parseProfileQuery("12345"))
    }

    @Test
    fun profilesUrlYieldsTheId() {
        assertEquals(ProfileQuery.ById(id), parseProfileQuery("https://steamcommunity.com/profiles/$id/"))
        assertEquals(ProfileQuery.ById(id), parseProfileQuery("steamcommunity.com/profiles/$id?l=en"))
    }

    @Test
    fun idUrlAndBareNameAreVanities() {
        assertEquals(ProfileQuery.ByVanity("raaveinm"), parseProfileQuery("https://steamcommunity.com/id/raaveinm/"))
        assertEquals(ProfileQuery.ByVanity("raaveinm"), parseProfileQuery("raaveinm"))
    }

    @Test
    fun otherInputIsRejected() {
        assertNull(parseProfileQuery(""))
        assertNull(parseProfileQuery("two words"))
        assertNull(parseProfileQuery("https://steamcommunity.com/groups/picasso"))
        assertNull(parseProfileQuery("https://steamcommunity.com/id/"))
    }
}
