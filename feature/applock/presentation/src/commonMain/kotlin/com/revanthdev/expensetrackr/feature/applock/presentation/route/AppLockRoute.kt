package com.revanthdev.expensetrackr.feature.applock.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.revanthdev.expensetrackr.core.presentation.LocalBiometricAuthenticator
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.applock_unlock_sub
import expensetrackr.core.presentation.generated.resources.applock_unlock_title
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object AppLockRoute

@Composable
fun AppLockRoot(
    onUnlocked: () -> Unit,
    viewModel: AppLockViewModel = koinViewModel()
) {
    val biometric = LocalBiometricAuthenticator.current
    val bioTitle = stringResource(Res.string.applock_unlock_title)
    val bioSubtitle = stringResource(Res.string.applock_unlock_sub)
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            AppLockEvent.Unlocked -> onUnlocked()
            AppLockEvent.TriggerBiometric -> biometric?.authenticate(
                title = bioTitle,
                subtitle = bioSubtitle,
                onSuccess = { viewModel.onBiometricSuccess() },
                onError = {}
            )
        }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    AppLockScreen(state = state, onAction = viewModel::onAction)
}
