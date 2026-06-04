package com.revanthdev.expensetrackr.feature.applock.presentation

import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString

@OptIn(ExperimentalForeignApi::class)
actual fun hashPin(pin: String): String {
    val input = pin.encodeToByteArray()
    memScoped {
        val digest = allocArray<UByteVar>(CC_SHA256_DIGEST_LENGTH)
        CC_SHA256(input.toCValues(), input.size.toUInt(), digest)
        return (0 until CC_SHA256_DIGEST_LENGTH).joinToString("") {
            digest[it].toString(16).padStart(2, '0')
        }
    }
}
