package com.revanthdev.expensetrackr.core.presentation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Platform bridge for biometric (fingerprint / face) authentication.
 *
 * A real implementation is provided by the host app (Android) via
 * [LocalBiometricAuthenticator]. Platforms without support simply leave the
 * local `null`, in which case the app falls back to PIN unlock.
 */
interface BiometricAuthenticator {
    /** True if the device has biometric hardware and at least one enrolled credential. */
    val isAvailable: Boolean

    fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )
}

val LocalBiometricAuthenticator = staticCompositionLocalOf<BiometricAuthenticator?> { null }
