package com.revanthdev.expensetrackr

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.revanthdev.expensetrackr.core.presentation.LocalBiometricAuthenticator

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val biometricAuthenticator = AndroidBiometricAuthenticator(this)
        setContent {
            CompositionLocalProvider(LocalBiometricAuthenticator provides biometricAuthenticator) {
                App()
            }
        }
    }
}
