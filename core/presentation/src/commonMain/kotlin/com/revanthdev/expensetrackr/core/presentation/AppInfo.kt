package com.revanthdev.expensetrackr.core.presentation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Version identity of the running build, supplied by the host app so shared UI never has to
 * hardcode it: Android reads `BuildConfig.VERSION_NAME`/`VERSION_CODE`, iOS reads the bundle's
 * `CFBundleShortVersionString`/`CFBundleVersion`.
 *
 * [versionName] is null when the host provides nothing (e.g. desktop, previews); callers should
 * omit the version rather than display a placeholder.
 */
data class AppInfo(
    val versionName: String? = null,
    val versionCode: Long = 0L,
)

val LocalAppInfo = staticCompositionLocalOf { AppInfo() }
