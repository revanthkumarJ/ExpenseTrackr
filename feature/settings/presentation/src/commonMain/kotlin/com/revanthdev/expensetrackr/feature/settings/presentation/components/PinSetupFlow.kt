package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.revanthdev.expensetrackr.core.designsystem.component.PinEntryScreen
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_back
import expensetrackr.core.presentation.generated.resources.pin_choose_sub
import expensetrackr.core.presentation.generated.resources.pin_confirm_sub
import expensetrackr.core.presentation.generated.resources.pin_confirm_title
import expensetrackr.core.presentation.generated.resources.pin_create
import expensetrackr.core.presentation.generated.resources.pin_mismatch
import expensetrackr.core.presentation.generated.resources.pin_reenter
import expensetrackr.core.presentation.generated.resources.pin_set_title
import org.jetbrains.compose.resources.stringResource

/**
 * Two-step PIN creation using the same [PinEntryScreen] keypad as the unlock screen.
 * Step 1 enters a 6-digit PIN; step 2 re-enters it to confirm. Biometric is never shown here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PinSetupFlow(onComplete: (String) -> Unit, onCancel: () -> Unit) {
    var firstPin by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirming by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val mismatchMessage = stringResource(Res.string.pin_mismatch)

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(if (confirming) Res.string.pin_confirm_title else Res.string.pin_set_title)) },
            navigationIcon = { IconButton(onClick = onCancel) { Icon(Icons.Rounded.ArrowBack, stringResource(Res.string.action_back)) } }
        )
    }) { padding ->
        PinEntryScreen(
            modifier = Modifier.padding(padding),
            title = stringResource(if (confirming) Res.string.pin_reenter else Res.string.pin_create),
            subtitle = stringResource(if (confirming) Res.string.pin_confirm_sub else Res.string.pin_choose_sub),
            pin = pin,
            error = error,
            onDigit = { digit ->
                if (pin.length < 6) {
                    pin += digit
                    error = null
                    if (pin.length == 6) {
                        if (!confirming) {
                            firstPin = pin
                            pin = ""
                            confirming = true
                        } else if (pin == firstPin) {
                            onComplete(firstPin)
                        } else {
                            error = mismatchMessage
                            pin = ""
                            firstPin = ""
                            confirming = false
                        }
                    }
                }
            },
            onBackspace = {
                pin = pin.dropLast(1)
                error = null
            }
        )
    }
}

@Preview
@Composable
private fun PinSetupFlowPreview() {
    ExpenseTrackerTheme {
        PinSetupFlow(onComplete = {}, onCancel = {})
    }
}
