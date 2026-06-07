package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.data.util.hashPin
import com.revanthdev.expensetrackr.core.designsystem.component.PinEntryScreen
import com.revanthdev.expensetrackr.core.domain.model.AppLockType
import com.revanthdev.expensetrackr.core.domain.model.AppSettings
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import com.revanthdev.expensetrackr.core.presentation.LocalBiometricAuthenticator
import expensetrackr.core.presentation.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf

// ---- About Screen ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(Res.string.settings_about)) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, stringResource(Res.string.action_back)) } })
    }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("💰 ExpenseTrackr", style = MaterialTheme.typography.headlineMedium)
            Text("Version 1.0.0", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Track every rupee. Stay in control.", style = MaterialTheme.typography.bodyLarge)
            HorizontalDivider()
            Text("Developer", style = MaterialTheme.typography.titleSmall)
            Text("RevanthDev", style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider()
            Text("This app is completely offline — no internet connection required. Your financial data never leaves your device.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ---- Privacy Policy Screen ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(Res.string.settings_privacy)) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, stringResource(Res.string.action_back)) } })
    }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(Res.string.settings_privacy), style = MaterialTheme.typography.headlineMedium)
            Text("Last updated: June 2026", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            PolicySection("Data Collection", "ExpenseTrackr collects no data. We have no servers, no analytics, no crash reporting, and no advertising SDKs.")
            PolicySection("Data Storage", "All your expense data is stored exclusively on your device in a local Room database. It is never transmitted anywhere.")
            PolicySection("Third Parties", "This app contains no third-party SDKs that collect data. There are no analytics tools, advertising networks, or social media integrations.")
            PolicySection("Your Rights", "You own your data completely. You can delete all your data at any time by clearing the app's storage in your device settings.")
            PolicySection("Contact", "Questions? Email: support@revanthdev.com")
        }
    }
}

// ---- Terms of Service Screen ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(Res.string.settings_terms)) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, stringResource(Res.string.action_back)) } })
    }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(Res.string.settings_terms), style = MaterialTheme.typography.headlineMedium)
            Text("Last updated: June 2026", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            PolicySection("Acceptance", "By using ExpenseTrackr, you agree to these terms.")
            PolicySection("Free App", "ExpenseTrackr is provided free of charge, as-is, with no warranties.")
            PolicySection("No Financial Advice", "This app is a personal tracking tool only. It does not provide financial advice. We are not liable for any financial decisions you make based on the information shown in this app.")
            PolicySection("Data Loss", "We are not responsible for loss of data due to device failure, app deletion, or any other reason. Back up your device regularly.")
            PolicySection("Changes", "We may update these terms. Continued use of the app constitutes acceptance of any updated terms.")
        }
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ---- Notification Settings ViewModel ----

class NotificationSettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    private val _state = MutableStateFlow(AppSettings())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { _state.value = it }
        }
    }

    fun toggleReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings(_state.value.copy(dailyReminderEnabled = enabled))
        }
    }

    fun toggleBudgetAlerts(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings(_state.value.copy(budgetAlertEnabled = enabled))
        }
    }
}

@Composable
fun NotificationSettingsRoot(onBack: () -> Unit, viewModel: NotificationSettingsViewModel = koinViewModel()) {
    val settings by viewModel.state.collectAsState()
    NotificationSettingsScreen(
        enabled = settings.dailyReminderEnabled,
        hour = settings.dailyReminderHour,
        minute = settings.dailyReminderMinute,
        budgetAlertsEnabled = settings.budgetAlertEnabled,
        onToggleReminder = { viewModel.toggleReminder(it) },
        onToggleBudgetAlerts = { viewModel.toggleBudgetAlerts(it) },
        onBack = onBack
    )
}

// ---- App Lock Setup ViewModel ----

class AppLockSetupViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    private val _state = MutableStateFlow(AppSettings())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { _state.value = it }
        }
    }

    /** Sets (or changes) the unlock PIN. Keeps biometric enabled if it already was. */
    fun setPin(pin: String) {
        viewModelScope.launch {
            val current = _state.value
            val keepBiometric = current.appLockType == AppLockType.BOTH || current.appLockType == AppLockType.BIOMETRIC
            settingsRepository.updateSettings(
                current.copy(
                    appLockType = if (keepBiometric) AppLockType.BOTH else AppLockType.PIN,
                    pinHash = hashPin(pin)
                )
            )
        }
    }

    /** Biometric is always paired with a PIN fallback, so this is a no-op without a PIN. */
    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = _state.value
            if (current.pinHash == null) return@launch
            settingsRepository.updateSettings(
                current.copy(appLockType = if (enabled) AppLockType.BOTH else AppLockType.PIN)
            )
        }
    }

    fun disableLock() {
        viewModelScope.launch {
            settingsRepository.updateSettings(
                _state.value.copy(appLockType = AppLockType.NONE, pinHash = null)
            )
        }
    }
}

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

val subScreensModule = org.koin.dsl.module {
    viewModelOf(::NotificationSettingsViewModel)
    viewModelOf(::AppLockSetupViewModel)
}

// ---- Notification Settings Screen ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    budgetAlertsEnabled: Boolean,
    onToggleReminder: (Boolean) -> Unit,
    onToggleBudgetAlerts: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(Res.string.notif_title)) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, stringResource(Res.string.action_back)) } })
    }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(Res.string.notif_daily_title), style = MaterialTheme.typography.titleSmall)
                            val reminderTime = "${hour.toString().padStart(2,'0')}:${minute.toString().padStart(2,'0')}"
                            Text(stringResource(Res.string.notif_daily_sub, reminderTime), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = enabled, onCheckedChange = onToggleReminder)
                    }
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(Res.string.notif_budget_title), style = MaterialTheme.typography.titleSmall)
                            Text(stringResource(Res.string.notif_budget_sub), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = budgetAlertsEnabled, onCheckedChange = onToggleBudgetAlerts)
                    }
                }
            }
        }
    }
}

// ---- App Lock Setup Screen ----

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

/**
 * Two-step PIN creation using the same [PinEntryScreen] keypad as the unlock screen.
 * Step 1 enters a 6-digit PIN; step 2 re-enters it to confirm. Biometric is never shown here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PinSetupFlow(onComplete: (String) -> Unit, onCancel: () -> Unit) {
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
