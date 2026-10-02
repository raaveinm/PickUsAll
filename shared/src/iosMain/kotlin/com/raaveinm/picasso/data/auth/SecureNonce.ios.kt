package com.raaveinm.picasso.data.auth

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

@OptIn(ExperimentalForeignApi::class)
actual fun secureNonce(): String {
    val bytes = ByteArray(NONCE_BYTES)
    bytes.usePinned { pinned ->
        val status = SecRandomCopyBytes(kSecRandomDefault, NONCE_BYTES.toULong(), pinned.addressOf(0))
        check(status == errSecSuccess) { "SecRandomCopyBytes failed: $status" }
    }
    return bytes.toHexString()
}
