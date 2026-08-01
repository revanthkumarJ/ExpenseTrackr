package com.revanthdev.expensetrackr

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.revanthdev.expensetrackr.core.presentation.LocalBiometricAuthenticator
import com.revanthdev.expensetrackr.core.presentation.LocalShareHandler
import com.revanthdev.expensetrackr.core.presentation.ShareHandler

class MainActivity : FragmentActivity() {

    // On Android 9 and below, writing the backup CSVs to public Downloads needs this permission
    // (Android 10+ uses MediaStore and needs nothing). Registered here; requested in onCreate.
    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

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
        setContent {
            CompositionLocalProvider(
                LocalBiometricAuthenticator provides biometricAuthenticator,
                LocalShareHandler provides shareHandler,
            ) {
                App()
            }
        }
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
