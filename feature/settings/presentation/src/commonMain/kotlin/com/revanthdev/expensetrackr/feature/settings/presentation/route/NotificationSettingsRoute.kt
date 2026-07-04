package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object NotificationSettingsRoute

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
