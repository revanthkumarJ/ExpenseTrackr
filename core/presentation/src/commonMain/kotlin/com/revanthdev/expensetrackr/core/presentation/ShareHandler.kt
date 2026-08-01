package com.revanthdev.expensetrackr.core.presentation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Platform bridge for sharing text (e.g. the app's store link) via the system share sheet.
 *
 * A real implementation is provided by the host app (Android) through [LocalShareHandler].
 * Platforms that don't provide one fall back to the default, which reports failure so the caller
 * can tell the user nothing happened instead of appearing to be broken.
 */
fun interface ShareHandler {
    /**
     * Opens the system share sheet for [text]. Returns `false` when the device has nothing that
     * can handle the share — implementations must not throw.
     */
    fun share(text: String): Boolean
}

val LocalShareHandler = staticCompositionLocalOf<ShareHandler> { ShareHandler { false } }
