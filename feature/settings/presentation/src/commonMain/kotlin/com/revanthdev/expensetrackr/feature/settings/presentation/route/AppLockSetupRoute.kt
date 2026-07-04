package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object AppLockSetupRoute

@Composable
fun AppLockSetupRoot(onBack: () -> Unit, viewModel: AppLockSetupViewModel = koinViewModel()) {
    val settings by viewModel.state.collectAsState()
    AppLockSetupScreen(
        settings = settings,
        onSetPin = viewModel::setPin,
        onBiometricToggle = viewModel::setBiometricEnabled,
        onDisableLock = viewModel::disableLock,
        onBack = onBack
    )
}
