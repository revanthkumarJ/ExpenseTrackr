package com.revanthdev.expensetrackr

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.revanthdev.expensetrackr.widget.refreshExpenseWidgets
import kotlinx.coroutines.launch
import com.revanthdev.expensetrackr.core.presentation.AppInfo
import com.revanthdev.expensetrackr.core.presentation.LocalAppInfo
import com.revanthdev.expensetrackr.core.presentation.LocalAppUpdateManager
import com.revanthdev.expensetrackr.core.presentation.LocalBiometricAuthenticator
import com.revanthdev.expensetrackr.core.presentation.LocalShareHandler
import com.revanthdev.expensetrackr.core.presentation.LocalStoreLauncher
import com.revanthdev.expensetrackr.core.presentation.ShareHandler
import com.revanthdev.expensetrackr.core.presentation.StoreLauncher

class MainActivity : FragmentActivity() {

    // On Android 9 and below, writing the backup CSVs to public Downloads needs this permission
    // (Android 10+ uses MediaStore and needs nothing). Registered here; requested in onCreate.
    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    // Play hands back an IntentSender for the in-app update flow, so this needs the
    // StartIntentSenderForResult contract. The result itself is ignored: a declined or failed
    // flow is already reflected in PlayAppUpdateManager's status via its install listener.
    private val updateFlowLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { }

    private val appUpdateManager by lazy { PlayAppUpdateManager(this, updateFlowLauncher) }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestLegacyStoragePermissionIfNeeded()
        val biometricAuthenticator = AndroidBiometricAuthenticator(this)
        // Returns false (rather than crashing) on the rare device with no app able to receive a
        // share — the caller turns that into a "no app found" message.
        val shareHandler = ShareHandler { text ->
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            try {
                startActivity(Intent.createChooser(sendIntent, null))
                true
            } catch (_: ActivityNotFoundException) {
                false
            } catch (_: Exception) {
                false
            }
        }
        val appInfo = AppInfo(
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE.toLong(),
        )
        quickAddRequests = if (intent.isQuickAdd()) 1 else 0
        setContent {
            CompositionLocalProvider(
                LocalBiometricAuthenticator provides biometricAuthenticator,
                LocalShareHandler provides shareHandler,
                LocalStoreLauncher provides storeLauncher,
                LocalAppInfo provides appInfo,
                LocalAppUpdateManager provides appUpdateManager,
            ) {
                App(quickAddRequest = quickAddRequests)
            }
        }
    }

    /**
     * Counter rather than a flag: tapping the widget's + while the app is already open must
     * re-open the Add Transaction form, and a `Boolean` that is already `true` wouldn't change,
     * so the composition would never notice the second request.
     */
    private var quickAddRequests by mutableIntStateOf(0)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Keep it as the Activity's current intent, matching the platform contract.
        setIntent(intent)
        if (intent.isQuickAdd()) quickAddRequests++
    }

    private fun Intent.isQuickAdd(): Boolean = action == ACTION_QUICK_ADD

    override fun onResume() {
        super.onResume()
        // Re-checked on every resume so an update published while the app sat in the background
        // is picked up, and so a download that completed off-screen surfaces its install prompt.
        appUpdateManager.refresh()
    }

    override fun onStop() {
        super.onStop()
        // The user is heading back to the launcher, where the widget is about to be visible —
        // redraw it so any expense added in this session is reflected immediately.
        lifecycleScope.launch { refreshExpenseWidgets(this@MainActivity) }
    }

    override fun onDestroy() {
        appUpdateManager.dispose()
        super.onDestroy()
    }

    /**
     * Sends the user to this app's Play listing to leave a rating. Prefers the `market://` scheme
     * so the Play app opens directly, and falls back to the web listing on devices without it
     * (and returns false if even a browser is missing, so the caller can say so).
     */
    private val storeLauncher = StoreLauncher {
        val marketUri = "market://details?id=$packageName".toUri()
        val webUri = "https://play.google.com/store/apps/details?id=$packageName".toUri()
        openUri(marketUri) || openUri(webUri)
    }

    private fun openUri(uri: Uri): Boolean = try {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: Exception) {
        false
    }

    companion object {
        /** Set by the home-screen widget's + button; opens the app on the Add Transaction form. */
        const val ACTION_QUICK_ADD = "com.revanthdev.expensetrackr.action.QUICK_ADD"
    }

    private fun requestLegacyStoragePermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}
