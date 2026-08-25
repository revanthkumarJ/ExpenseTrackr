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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.presentation.LocalAppInfo
import com.revanthdev.expensetrackr.core.presentation.LocalNativeAd
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_back
import expensetrackr.core.presentation.generated.resources.settings_about
import expensetrackr.core.presentation.generated.resources.settings_version
import org.jetbrains.compose.resources.stringResource

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
            // Comes from the host app's build metadata (BuildConfig.VERSION_NAME on Android), so
            // it can never drift from the shipped build. Hosts that supply none omit the line.
            LocalAppInfo.current.versionName?.let { versionName ->
                Text(
                    stringResource(Res.string.settings_version, versionName),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text("Track every rupee. Stay in control.", style = MaterialTheme.typography.bodyLarge)
            HorizontalDivider()
            Text("Developer", style = MaterialTheme.typography.titleSmall)
            Text("RevanthDev", style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider()
            Text("Your financial records stay on your device and are never sent to advertisers. The Android app is supported by Google AdMob banner, native, and occasional rewarded ads, and uses Firebase for crash reporting and usage analytics. See the Privacy Policy for details and available choices.", style = MaterialTheme.typography.bodyMedium)
            LocalNativeAd.current()
        }
    }
}
