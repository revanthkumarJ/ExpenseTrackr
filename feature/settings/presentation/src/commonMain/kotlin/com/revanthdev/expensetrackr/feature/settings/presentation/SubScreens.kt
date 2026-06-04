package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ---- About Screen ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("About") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } })
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
        TopAppBar(title = { Text("Privacy Policy") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } })
    }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Privacy Policy", style = MaterialTheme.typography.headlineMedium)
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
        TopAppBar(title = { Text("Terms of Service") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } })
    }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Terms of Service", style = MaterialTheme.typography.headlineMedium)
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
        TopAppBar(title = { Text("Notification Settings") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } })
    }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Daily Expense Reminder", style = MaterialTheme.typography.titleSmall)
                            Text("Remind you to log today's expenses at ${hour.toString().padStart(2,'0')}:${minute.toString().padStart(2,'0')}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = enabled, onCheckedChange = onToggleReminder)
                    }
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Budget Alerts", style = MaterialTheme.typography.titleSmall)
                            Text("Alert when spending exceeds 80% of budget", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    currentLockType: String,
    onSelectNone: () -> Unit,
    onSetupPin: () -> Unit,
    onSetupBiometric: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("App Lock") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } })
    }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Choose how to protect your app:", style = MaterialTheme.typography.bodyLarge)
            LockOption("None", "No lock — open directly", currentLockType == "NONE", onSelectNone)
            LockOption("PIN", "4–6 digit numeric PIN", currentLockType == "PIN", onSetupPin)
            LockOption("Biometric", "Fingerprint / Face unlock", currentLockType == "BIOMETRIC", onSetupBiometric)
            Text(
                "⚠ If you forget your PIN, you must clear app data to reset it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun LockOption(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (selected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors()
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RadioButton(selected = selected, onClick = null)
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
