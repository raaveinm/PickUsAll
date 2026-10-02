package com.raaveinm.picasso.data.auth

import java.security.SecureRandom

actual fun secureNonce(): String =
    ByteArray(NONCE_BYTES).also { SecureRandom().nextBytes(it) }.toHexString()
