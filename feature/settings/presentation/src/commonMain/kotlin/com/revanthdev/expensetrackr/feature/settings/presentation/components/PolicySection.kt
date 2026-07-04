package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme

@Composable
internal fun PolicySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview
@Composable
private fun PolicySectionPreview() {
    ExpenseTrackerTheme {
        PolicySection("Data Collection", "ExpenseTrackr collects no data. We have no servers, no analytics, and no advertising SDKs.")
    }
}
