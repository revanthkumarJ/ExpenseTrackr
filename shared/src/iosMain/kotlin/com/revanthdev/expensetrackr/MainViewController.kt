package com.revanthdev.expensetrackr

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.revanthdev.expensetrackr.core.presentation.AppInfo
import com.revanthdev.expensetrackr.core.presentation.LocalAppInfo
import com.revanthdev.expensetrackr.core.presentation.LocalShareHandler
import platform.Foundation.NSBundle

/**
 * Hosts the shared Compose [App] as a UIViewController for SwiftUI (via `ComposeView`).
 * Provides the iOS [IosShareHandler] so the in-app "Share App" action opens the iOS share sheet,
 * and the bundle's version so the About screen doesn't hardcode it.
 *
 * Biometric auth has no iOS implementation, so the app falls back to PIN unlock (the default).
 * There is no iOS store listing yet, so `LocalStoreLauncher` and `LocalAppUpdateManager` keep
 * their defaults — "Rate us" reports that nothing can handle it, and the update row stays idle.
 */
fun MainViewController() = ComposeUIViewController {
    CompositionLocalProvider(
        LocalShareHandler provides IosShareHandler(),
        LocalAppInfo provides iosAppInfo(),
    ) {
        App()
    }
}

/**
 * Reads the version out of Info.plist: `CFBundleShortVersionString` is the user-facing "1.0.7",
 * `CFBundleVersion` the build number — the iOS counterparts of versionName/versionCode.
 */
private fun iosAppInfo(): AppInfo {
    val info = NSBundle.mainBundle.infoDictionary
    return AppInfo(
        versionName = info?.get("CFBundleShortVersionString") as? String,
        versionCode = (info?.get("CFBundleVersion") as? String)?.toLongOrNull() ?: 0L,
    )
}
