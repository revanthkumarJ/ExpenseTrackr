package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.domain.model.AppLockType
import com.revanthdev.expensetrackr.core.domain.model.AppSettings
import com.revanthdev.expensetrackr.core.presentation.LocalBiometricAuthenticator
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_back
import expensetrackr.core.presentation.generated.resources.applock_biometric_need_pin
import expensetrackr.core.presentation.generated.resources.applock_biometric_none
import expensetrackr.core.presentation.generated.resources.applock_biometric_title
import expensetrackr.core.presentation.generated.resources.applock_biometric_use
import expensetrackr.core.presentation.generated.resources.applock_change_pin
import expensetrackr.core.presentation.generated.resources.applock_forgot
import expensetrackr.core.presentation.generated.resources.applock_off
import expensetrackr.core.presentation.generated.resources.applock_on
import expensetrackr.core.presentation.generated.resources.applock_remove
import expensetrackr.core.presentation.generated.resources.applock_set_pin
import expensetrackr.core.presentation.generated.resources.applock_setup_title
import expensetrackr.core.presentation.generated.resources.applock_status_both
import expensetrackr.core.presentation.generated.resources.applock_status_none
import expensetrackr.core.presentation.generated.resources.applock_status_pin
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockSetupScreen(
    settings: AppSettings,
    onSetPin: (String) -> Unit,
    onBiometricToggle: (Boolean) -> Unit,
    onDisableLock: () -> Unit,
    onBack: () -> Unit
) {
    val biometric = LocalBiometricAuthenticator.current
    val biometricAvailable = biometric?.isAvailable == true
    val pinSet = settings.pinHash != null
    val biometricEnabled = settings.appLockType == AppLockType.BOTH || settings.appLockType == AppLockType.BIOMETRIC

    var setupMode by remember { mutableStateOf(false) }

    if (setupMode) {
        PinSetupFlow(
            onComplete = { pin ->
                onSetPin(pin)
                setupMode = false
            },
            onCancel = { setupMode = false }
        )
        return
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(Res.string.applock_setup_title)) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, stringResource(Res.string.action_back)) } })
    }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = if (pinSet) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 1.dp
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (pinSet) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                        contentDescription = null,
                        tint = if (pinSet) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            stringResource(if (pinSet) Res.string.applock_on else Res.string.applock_off),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (pinSet) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            when {
                                !pinSet -> stringResource(Res.string.applock_status_none)
                                biometricEnabled -> stringResource(Res.string.applock_status_both)
                                else -> stringResource(Res.string.applock_status_pin)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (pinSet) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Set / change PIN
            Button(
                onClick = { setupMode = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Rounded.Pin, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(if (pinSet) Res.string.applock_change_pin else Res.string.applock_set_pin))
            }

            // Biometric toggle
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 1.dp
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(Res.string.applock_biometric_title), style = MaterialTheme.typography.titleSmall)
                        Text(
                            when {
                                !biometricAvailable -> stringResource(Res.string.applock_biometric_none)
                                !pinSet -> stringResource(Res.string.applock_biometric_need_pin)
                                else -> stringResource(Res.string.applock_biometric_use)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { onBiometricToggle(it) },
                        enabled = pinSet && biometricAvailable
                    )
                }
            }

            if (pinSet) {
                OutlinedButton(
                    onClick = onDisableLock,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Rounded.LockOpen, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.applock_remove))
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(Res.string.applock_forgot),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
