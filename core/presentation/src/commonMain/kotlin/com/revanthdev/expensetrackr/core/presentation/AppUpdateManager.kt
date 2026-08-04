package com.revanthdev.expensetrackr.core.presentation

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Where the running build sits relative to the newest version published on the store. */
enum class AppUpdateStatus {
    /** Not checked yet, or the platform can't tell us (no store, offline, sideloaded build). */
    Unknown,

    /** This build is the newest one published. */
    UpToDate,

    /** A newer build exists and [AppUpdateManager.startUpdate] can fetch it. */
    Available,

    /** The user accepted; the new build is downloading in the background. */
    Downloading,

    /** Download finished — [AppUpdateManager.completeUpdate] will install it and restart the app. */
    ReadyToInstall,
}

/**
 * Platform bridge for in-app updates. On Android this is backed by Play's In-App Update API, which
 * only reports an update for builds actually installed from Play — a locally-installed debug APK
 * stays [AppUpdateStatus.Unknown] forever, which is expected, not a bug.
 *
 * Shared UI decides *when* to prompt (see `App.kt`); the implementation only provides the mechanism.
 */
interface AppUpdateManager {
    val status: StateFlow<AppUpdateStatus>

    /** Re-queries the store. Safe to call repeatedly (e.g. on resume, or from a Settings row). */
    fun refresh()

    /** Starts the platform's update flow. No-op unless [status] is [AppUpdateStatus.Available]. */
    fun startUpdate()

    /**
     * Installs an already-downloaded update. This restarts the app, so only call it from a prompt
     * the user has actively accepted. No-op unless [status] is [AppUpdateStatus.ReadyToInstall].
     */
    fun completeUpdate()
}

/** Used on platforms with no store integration; always reports [AppUpdateStatus.Unknown]. */
object NoOpAppUpdateManager : AppUpdateManager {
    override val status: StateFlow<AppUpdateStatus> =
        MutableStateFlow(AppUpdateStatus.Unknown).asStateFlow()

    override fun refresh() = Unit
    override fun startUpdate() = Unit
    override fun completeUpdate() = Unit
}

val LocalAppUpdateManager = staticCompositionLocalOf<AppUpdateManager> { NoOpAppUpdateManager }
