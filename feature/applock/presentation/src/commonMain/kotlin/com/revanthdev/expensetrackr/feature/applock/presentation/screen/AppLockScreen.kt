package com.revanthdev.expensetrackr.feature.applock.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.revanthdev.expensetrackr.core.designsystem.component.PinEntryScreen
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.applock_enter_pin
import expensetrackr.core.presentation.generated.resources.applock_enter_pin_sub
import expensetrackr.core.presentation.generated.resources.applock_incorrect
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppLockScreen(state: AppLockState, onAction: (AppLockAction) -> Unit) {
    PinEntryScreen(
        title = stringResource(Res.string.applock_enter_pin),
        subtitle = stringResource(Res.string.applock_enter_pin_sub),
        pin = state.pin,
        error = state.error?.let { stringResource(Res.string.applock_incorrect) },
        onDigit = { onAction(AppLockAction.OnPinDigit(it)) },
        onBackspace = { onAction(AppLockAction.OnPinBackspace) },
        biometricIcon = if (state.showBiometric) Icons.Rounded.Fingerprint else null,
        onBiometric = if (state.showBiometric) {
            { onAction(AppLockAction.OnBiometricRequest) }
        } else null
    )
}

@Preview
@Composable
private fun AppLockScreenPreview() {
    ExpenseTrackerTheme {
        AppLockScreen(state = AppLockState(pin = "123", showBiometric = true), onAction = {})
    }
}
