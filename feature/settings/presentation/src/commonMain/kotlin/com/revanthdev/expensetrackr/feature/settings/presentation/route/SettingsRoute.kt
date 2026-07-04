package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object SettingsRoute

@Serializable
data object AboutRoute

@Serializable
data object PrivacyPolicyRoute

@Serializable
data object TermsOfServiceRoute

@Composable
fun SettingsRoot(
    onNavigateToBudget: () -> Unit,
    onNavigateToManageCategories: () -> Unit,
    onNavigateToManageSubCategories: () -> Unit,
    onNavigateToAppLockSetup: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTerms: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            SettingsEvent.NavigateToBudget -> onNavigateToBudget()
            SettingsEvent.NavigateToManageCategories -> onNavigateToManageCategories()
            SettingsEvent.NavigateToManageSubCategories -> onNavigateToManageSubCategories()
            SettingsEvent.NavigateToAppLockSetup -> onNavigateToAppLockSetup()
            SettingsEvent.NavigateToNotificationSettings -> onNavigateToNotificationSettings()
            SettingsEvent.NavigateToAbout -> onNavigateToAbout()
            SettingsEvent.NavigateToPrivacyPolicy -> onNavigateToPrivacyPolicy()
            SettingsEvent.NavigateToTerms -> onNavigateToTerms()
        }
    }
    val state by viewModel.state.collectAsState()
    SettingsScreen(state = state, onAction = viewModel::onAction)
}
