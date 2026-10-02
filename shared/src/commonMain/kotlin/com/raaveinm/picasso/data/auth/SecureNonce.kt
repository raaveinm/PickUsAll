package com.raaveinm.picasso.data.auth

//
// Created by Kirill "Raaveinm" on 10/1/26.
//

expect fun secureNonce(): String

internal const val NONCE_BYTES = 32

internal fun ByteArray.toHexString(): String =
    joinToString("") { byte -> (byte.toInt() and 0xFF).toString(16).padStart(2, '0') }
