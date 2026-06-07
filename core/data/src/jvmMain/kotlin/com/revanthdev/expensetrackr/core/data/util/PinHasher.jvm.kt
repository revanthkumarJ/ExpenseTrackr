package com.revanthdev.expensetrackr.core.data.util

import java.security.MessageDigest

actual fun hashPin(pin: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}
