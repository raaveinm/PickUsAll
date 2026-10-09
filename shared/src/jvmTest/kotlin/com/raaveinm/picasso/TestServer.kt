package com.raaveinm.picasso

//
// Created by Kirill "Raaveinm" on 10/9/26.
//

import java.util.concurrent.atomic.AtomicInteger

/**
 * The live picassobackend the integration tests talk to, from PICASSO_TEST_SERVER (host:port).
 * Unset means those tests are skipped.
 *
 * The server needs users 1..[LAST_USER] with a session each, token "tok-<n>" and steam id
 * 76561198000000000 + n. Every test takes fresh users from one process-wide counter so no
 * test sees another's contacts, conversations or rate-limit state.
 */
object TestServer {
    const val LAST_USER = 200

    val address: String? = System.getenv("PICASSO_TEST_SERVER")

    private val next = AtomicInteger(0)

    fun nextUser(): Int {
        val number = next.incrementAndGet()
        check(number <= LAST_USER) { "the test server only has users 1..$LAST_USER seeded" }
        return number
    }

    fun steamIdOf(number: Int): Long = 76561198000000000L + number
}
