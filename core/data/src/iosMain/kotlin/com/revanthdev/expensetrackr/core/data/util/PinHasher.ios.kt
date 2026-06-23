package com.revanthdev.expensetrackr.core.data.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH

@OptIn(ExperimentalForeignApi::class)
actual fun hashPin(pin: String): String {
    val input = pin.encodeToByteArray()
    val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
    input.usePinned { inputPinned ->
        digest.usePinned { digestPinned ->
            CC_SHA256(inputPinned.addressOf(0), input.size.toUInt(), digestPinned.addressOf(0))
        }
    }
    val sb = StringBuilder(digest.size * 2)
    for (byte in digest) {
        sb.append((byte.toInt() and 0xFF).toString(16).padStart(2, '0'))
    }
    return sb.toString()
}
