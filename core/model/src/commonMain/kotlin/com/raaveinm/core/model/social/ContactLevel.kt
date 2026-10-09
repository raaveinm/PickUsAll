package com.raaveinm.core.model.social

//
// Created by Kirill "Raaveinm" on 10/9/26.
// Copyright (c) 2026 Retrograde Mercury. All rights reserved.
//

/**
 * What the LOCAL user regards another Artist as - Picasso's own relationship, not Steam
 * friendship (see brainstorm/chat-sync-contract.md, section 3A).
 *
 * "Stranger" is deliberately not a value: it is the absence of a row. [wire] is the
 * string the server sends and expects.
 *
 * [FRIEND] behaves like [ALLY] today; the planned use is "may add me to a palette
 * without my confirmation".
 */
enum class ContactLevel(val wire: String) {
    IMPOSTER("imposter"),
    ALLY("ally"),
    FRIEND("friend");

    val allowsCommunication: Boolean get() = this != IMPOSTER

    companion object {
        fun fromWire(wire: String?): ContactLevel? = entries.firstOrNull { it.wire == wire }
    }
}
