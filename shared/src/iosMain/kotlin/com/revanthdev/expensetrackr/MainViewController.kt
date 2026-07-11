package com.revanthdev.expensetrackr

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.revanthdev.expensetrackr.core.presentation.LocalShareHandler

/**
 * Hosts the shared Compose [App] as a UIViewController for SwiftUI (via `ComposeView`).
 * Provides the iOS [IosShareHandler] so the in-app "Share App" action opens the iOS share sheet.
 * Biometric auth has no iOS implementation, so the app falls back to PIN unlock (the default).
 */
fun MainViewController() = ComposeUIViewController {
    CompositionLocalProvider(LocalShareHandler provides IosShareHandler()) {
        App()
    }
}
