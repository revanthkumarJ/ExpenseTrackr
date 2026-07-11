package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_back
import expensetrackr.core.presentation.generated.resources.settings_privacy
import org.jetbrains.compose.resources.stringResource

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
            Text("Last updated: July 2026", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            PolicySection("Your Financial Data", "All of your expenses, income, budgets, and categories are stored exclusively on your device in a local database. This financial data is never uploaded, transmitted, or shared with anyone.")
            PolicySection("Crash Reporting", "To help us find and fix bugs, the app uses Google Firebase Crashlytics. When the app crashes, an anonymous crash report is sent to Google. It may include your device model, operating system version, app version, and the technical stack trace of the crash. It does NOT include your expenses, income, or any amounts you have entered.")
            PolicySection("Usage Analytics", "The app uses Google Firebase Analytics to understand how the app is used in aggregate (for example, which screens are opened and how often the app is launched). This data is anonymous and cannot be used to identify you personally. It does NOT include the content of your financial records.")
            PolicySection("Third Parties", "Crash and analytics data is processed by Google as part of Firebase. Google's handling of this data is governed by the Google Privacy Policy (policies.google.com/privacy). We do not use any advertising networks or sell your data.")
            PolicySection("Your Choices", "Your financial data stays on your device and you can delete it at any time by clearing the app's storage in your device settings. Crash and analytics reporting only collect anonymous diagnostic information and never your financial records.")
            PolicySection("Contact", "Questions? Email: support@revanthdev.com")
        }
    }
}
