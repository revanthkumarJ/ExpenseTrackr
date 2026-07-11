package com.revanthdev.expensetrackr.core.presentation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Platform bridge for sharing text (e.g. the app's store link) via the system share sheet.
 *
 * A real implementation is provided by the host app (Android) through [LocalShareHandler].
 * Platforms that don't provide one fall back to the no-op default.
 */
fun interface ShareHandler {
    fun share(text: String)
}

val LocalShareHandler = staticCompositionLocalOf<ShareHandler> { ShareHandler { } }
