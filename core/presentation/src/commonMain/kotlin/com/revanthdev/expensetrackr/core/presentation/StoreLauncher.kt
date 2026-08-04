package com.revanthdev.expensetrackr.core.presentation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Platform bridge for opening this app's own store listing, so the user can leave a rating.
 *
 * Deliberately *not* Play's In-App Review API: Google's policy forbids triggering that flow from a
 * button, so a "Rate us" entry point has to send the user to the listing instead.
 *
 * Platforms that don't provide one fall back to the default, which reports failure so the caller
 * can tell the user nothing happened — same contract as [ShareHandler].
 */
fun interface StoreLauncher {
    /**
     * Opens the store listing for this app. Returns `false` when nothing on the device can handle
     * it — implementations must not throw.
     */
    fun openStoreListing(): Boolean
}

/**
 * Used on platforms with no store listing to send the user to. Named (rather than an inline
 * lambda) so UI can identity-check it and hide the entry point entirely instead of offering a
 * button that always fails.
 */
object NoStoreLauncher : StoreLauncher {
    override fun openStoreListing(): Boolean = false
}

val LocalStoreLauncher = staticCompositionLocalOf<StoreLauncher> { NoStoreLauncher }
