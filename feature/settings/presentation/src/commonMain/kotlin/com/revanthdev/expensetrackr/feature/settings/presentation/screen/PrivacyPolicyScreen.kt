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
            Text("Last updated: August 2026", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            PolicySection("Your Financial Data", "All of your expenses, income, budgets, and categories are stored exclusively on your device in a local database. This financial data is never uploaded, transmitted, or shared with anyone.")
            PolicySection("Crash Reporting", "To help us find and fix bugs, the app uses Google Firebase Crashlytics. When the app crashes, an anonymous crash report is sent to Google. It may include your device model, operating system version, app version, and the technical stack trace of the crash. It does NOT include your expenses, income, or any amounts you have entered.")
            PolicySection("Usage Analytics", "The app uses Google Firebase Analytics to understand how the app is used in aggregate (for example, which screens are opened and how often the app is launched). This data is anonymous and cannot be used to identify you personally. It does NOT include the content of your financial records.")
            PolicySection("Advertising", "The Android app uses Google AdMob to display banner, native, and rewarded advertisements. AdMob and its advertising partners may collect device identifiers, advertising ID, IP address, approximate location, ad interactions, diagnostics, and other data needed to select, deliver, measure, and prevent fraud in advertising. Ads never receive the expenses, income, budgets, notes, or other financial records you enter.")
            PolicySection("Rewarded Ads", "Some backup, restore, and report-download actions may offer a rewarded advertisement. The first two actions proceed normally; every third action requests a rewarded ad. If an ad cannot be loaded, the action remains available. A small local counter is stored on your device to determine when the next rewarded ad is due.")
            PolicySection("Consent and Choices", "Where required, you may be asked for consent before personalized advertising is used. Depending on your choice and region, Google may show personalized or non-personalized ads. You can reset or limit the advertising ID through Android settings and can review available privacy choices in the app or Google's advertising settings.")
            PolicySection("Third Parties", "Crash reporting and analytics are processed through Firebase, and advertising is processed through Google AdMob and participating advertising partners. Their handling of data is governed by their own terms and the Google Privacy Policy at policies.google.com/privacy. We do not sell your financial records.")
            PolicySection("Your Financial Data Choices", "Your financial records stay on your device. You can delete them by using the app's controls or clearing app storage. Clearing app storage also removes locally stored rewarded-ad counters.")
            PolicySection("Contact", "Questions? Email: jrevanth101@gmail.com")
        }
    }
}
