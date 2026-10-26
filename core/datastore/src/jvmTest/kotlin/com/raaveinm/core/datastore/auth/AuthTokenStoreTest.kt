package com.raaveinm.core.datastore.auth

import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

//
// Created by Kirill "Raaveinm" on 10/26/26.
//

/** A token is only ever valid on the server that issued it, so sessions must never leak across servers. */
class AuthTokenStoreTest {

    private fun newStore(): AuthTokenStore = AuthTokenStore(
        createAuthDataStore {
            File.createTempFile("auth", ".preferences_pb").also {
                it.delete()
                it.deleteOnExit()
            }.absolutePath
        }
    )

    private val tokenA = UserAuthToken(token = "token-a", userId = 11L, expiresAtEpochMs = 5_000L)
    private val tokenB = UserAuthToken(token = "token-b", userId = 22L)

    @Test
    fun eachServerKeepsItsOwnSession() = runBlocking {
        val store = newStore()
        store.save(serverId = 1L, session = tokenA)
        store.save(serverId = 2L, session = tokenB)

        assertEquals(tokenA, store.current(serverId = 1L))
        assertEquals(tokenB, store.current(serverId = 2L))
        assertNull(store.current(serverId = 3L))
    }

    @Test
    fun clearingOneServerLeavesTheOthersSignedIn() = runBlocking {
        val store = newStore()
        store.save(serverId = 1L, session = tokenA)
        store.save(serverId = 2L, session = tokenB)

        store.clear(serverId = 1L)

        assertNull(store.current(serverId = 1L))
        assertEquals(tokenB, store.current(serverId = 2L))
    }

    @Test
    fun clearIfTokenOnlyDropsTheExactTokenOfThatServer() = runBlocking {
        val store = newStore()
        store.save(serverId = 1L, session = tokenA)

        store.clearIfToken(serverId = 1L, token = "some-newer-token")
        assertEquals(tokenA, store.current(serverId = 1L))      // a login that happened in between survives

        store.clearIfToken(serverId = 2L, token = "token-a")
        assertEquals(tokenA, store.current(serverId = 1L))      // same string on another server is not the same session

        store.clearIfToken(serverId = 1L, token = "token-a")
        assertNull(store.current(serverId = 1L))
    }
}
