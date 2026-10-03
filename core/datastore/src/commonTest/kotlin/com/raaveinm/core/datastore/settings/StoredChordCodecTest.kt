package com.raaveinm.core.datastore.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

//
// Created by Kirill "Raaveinm" on 10/3/26.
//

class StoredChordCodecTest {

    private val chord: StoredChord = StoredChord(
        keyCode = 55834574848L,
        isPrimary = true,
        isShift = false,
        isAlt = true,
        isControl = false
    )

    @Test
    fun roundTrips() {
        assertEquals(chord, chord.encode().decodeStoredChord())
    }

    @Test
    fun encodingIsStable() {
        // this string is what already sits in users' files - changing it would silently reset their bindings
        assertEquals("55834574848,1,0,1,0", chord.encode())
    }

    @Test
    fun malformedEntriesDecodeToNull() {
        listOf(
            "",
            "garbage",
            "1,1,0,0",              // too few fields
            "1,1,0,0,0,0",          // too many fields
            "notanumber,1,0,0,0",   // bad key code
            "1,2,0,0,0",            // flag is not 0/1
            "1,true,false,false,false"
        ).forEach { assertNull(it.decodeStoredChord(), "should reject '$it'") }
    }
}
