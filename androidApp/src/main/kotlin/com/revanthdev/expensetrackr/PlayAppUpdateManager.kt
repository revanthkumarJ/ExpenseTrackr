package com.revanthdev.expensetrackr

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.revanthdev.expensetrackr.core.presentation.AppUpdateManager
import com.revanthdev.expensetrackr.core.presentation.AppUpdateStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [AppUpdateManager] backed by Play's In-App Update API, using the FLEXIBLE flow: the download
 * happens in the background while the user keeps using the app, and installing it is a separate,
 * user-accepted step (see [completeUpdate], prompted from `App.kt`).
 *
 * Play only reports an update for builds installed from Play, so on a debug/sideloaded APK the
 * status stays [AppUpdateStatus.Unknown]. Test this against an internal-test-track install.
 *
 * @param launcher registered by the host Activity for [IntentSenderRequest]; Play hands us an
 *   IntentSender rather than an Intent, so the plain `StartActivityForResult` contract won't do.
 */
class PlayAppUpdateManager(
    context: Context,
    private val launcher: ActivityResultLauncher<IntentSenderRequest>,
) : AppUpdateManager {

    private val delegate = AppUpdateManagerFactory.create(context.applicationContext)

    private val _status = MutableStateFlow(AppUpdateStatus.Unknown)
    override val status: StateFlow<AppUpdateStatus> = _status.asStateFlow()

    /** The info object Play gave us; required to start the flow, and only valid for one use. */
    private var pendingInfo: AppUpdateInfo? = null

    private val installListener = InstallStateUpdatedListener { state ->
        _status.value = when (state.installStatus()) {
            InstallStatus.DOWNLOADING, InstallStatus.PENDING -> AppUpdateStatus.Downloading
            InstallStatus.DOWNLOADED -> AppUpdateStatus.ReadyToInstall
            // CANCELED/FAILED put us back to "an update is still out there" so the user can retry
            // from Settings; INSTALLED never really surfaces because installing restarts the app.
            InstallStatus.CANCELED, InstallStatus.FAILED -> AppUpdateStatus.Available
            else -> _status.value
        }
    }

    init {
        delegate.registerListener(installListener)
    }

    override fun refresh() {
        // Don't clobber an in-flight download with a stale availability check.
        if (_status.value == AppUpdateStatus.Downloading ||
            _status.value == AppUpdateStatus.ReadyToInstall
        ) return

        delegate.appUpdateInfo
            .addOnSuccessListener { info ->
                pendingInfo = info
                _status.value = when {
                    // A download that finished before this process started (e.g. the user
                    // backgrounded the app mid-update) is reported here, not via the listener.
                    info.installStatus() == InstallStatus.DOWNLOADED -> AppUpdateStatus.ReadyToInstall
                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> AppUpdateStatus.Available
                    info.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE -> AppUpdateStatus.UpToDate
                    else -> AppUpdateStatus.Unknown
                }
            }
            // Offline, no Play Store, or a sideloaded build: stay silent rather than nag the user.
            .addOnFailureListener { _status.value = AppUpdateStatus.Unknown }
    }

    override fun startUpdate() {
        val info = pendingInfo ?: return
        if (_status.value != AppUpdateStatus.Available) return
        runCatching {
            delegate.startUpdateFlowForResult(
                info,
                launcher,
                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
            )
        }
    }

    override fun completeUpdate() {
        if (_status.value != AppUpdateStatus.ReadyToInstall) return
        delegate.completeUpdate()
    }

    /** Must be called when the host Activity is destroyed, or Play leaks the listener. */
    fun dispose() {
        delegate.unregisterListener(installListener)
    }
}
