package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.designsystem.component.bounceClick
import com.revanthdev.expensetrackr.core.domain.model.AppLockType
import com.revanthdev.expensetrackr.core.domain.model.AppSettings
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf

@Serializable
data object SettingsRoute

@Serializable
data object AboutRoute

@Serializable
data object PrivacyPolicyRoute

@Serializable
data object TermsOfServiceRoute

@Serializable
data object AppLockSetupRoute

@Serializable
data object NotificationSettingsRoute

data class SettingsState(val settings: AppSettings = AppSettings(), val isLoading: Boolean = true)

sealed interface SettingsAction {
    data class OnThemeChange(val isDark: Boolean?) : SettingsAction
    data object OnBudgetClick : SettingsAction
    data object OnManageCategoriesClick : SettingsAction
    data object OnManageSubCategoriesClick : SettingsAction
    data object OnAppLockClick : SettingsAction
    data object OnNotificationSettingsClick : SettingsAction
    data object OnAboutClick : SettingsAction
    data object OnPrivacyPolicyClick : SettingsAction
    data object OnTermsClick : SettingsAction
}

sealed interface SettingsEvent {
    data object NavigateToBudget : SettingsEvent
    data object NavigateToManageCategories : SettingsEvent
    data object NavigateToManageSubCategories : SettingsEvent
    data object NavigateToAppLockSetup : SettingsEvent
    data object NavigateToNotificationSettings : SettingsEvent
    data object NavigateToAbout : SettingsEvent
    data object NavigateToPrivacyPolicy : SettingsEvent
    data object NavigateToTerms : SettingsEvent
}

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()
    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _state.update { it.copy(settings = settings, isLoading = false) }
            }
        }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnThemeChange -> viewModelScope.launch {
                val s = _state.value.settings
                settingsRepository.updateSettings(s.copy(isDarkMode = action.isDark))
            }
            SettingsAction.OnBudgetClick -> viewModelScope.launch { _events.send(SettingsEvent.NavigateToBudget) }
            SettingsAction.OnManageCategoriesClick -> viewModelScope.launch { _events.send(SettingsEvent.NavigateToManageCategories) }
            SettingsAction.OnManageSubCategoriesClick -> viewModelScope.launch { _events.send(SettingsEvent.NavigateToManageSubCategories) }
            SettingsAction.OnAppLockClick -> viewModelScope.launch { _events.send(SettingsEvent.NavigateToAppLockSetup) }
            SettingsAction.OnNotificationSettingsClick -> viewModelScope.launch { _events.send(SettingsEvent.NavigateToNotificationSettings) }
            SettingsAction.OnAboutClick -> viewModelScope.launch { _events.send(SettingsEvent.NavigateToAbout) }
            SettingsAction.OnPrivacyPolicyClick -> viewModelScope.launch { _events.send(SettingsEvent.NavigateToPrivacyPolicy) }
            SettingsAction.OnTermsClick -> viewModelScope.launch { _events.send(SettingsEvent.NavigateToTerms) }
        }
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(state: SettingsState, onAction: (SettingsAction) -> Unit) {
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Theme") },
            text = {
                Column {
                    listOf(null to "System Default", false to "Light", true to "Dark").forEach { (value, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable {
                                onAction(SettingsAction.OnThemeChange(value))
                                showThemeDialog = false
                            }.padding(vertical = 8.dp)
                        ) {
                            RadioButton(selected = state.settings.isDarkMode == value, onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text("Close") } }
        )
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
            item { SettingsSectionHeader("Preferences") }
            item {
                SettingsItem(
                    icon = Icons.Rounded.Brightness6,
                    title = "Theme",
                    subtitle = when (state.settings.isDarkMode) { null -> "System Default"; true -> "Dark"; false -> "Light" },
                    onClick = { showThemeDialog = true }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Rounded.Lock,
                    title = "App Lock",
                    subtitle = state.settings.appLockType.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { onAction(SettingsAction.OnAppLockClick) }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Rounded.Notifications,
                    title = "Notifications",
                    subtitle = if (state.settings.dailyReminderEnabled) "Reminders enabled" else "Reminders disabled",
                    onClick = { onAction(SettingsAction.OnNotificationSettingsClick) }
                )
            }
            item { SettingsSectionHeader("Budget") }
            item {
                SettingsItem(
                    icon = Icons.Rounded.AccountBalance,
                    title = "Budget Management",
                    subtitle = state.settings.overallMonthlyBudget?.let { "₹${it}/month" } ?: "Not set",
                    onClick = { onAction(SettingsAction.OnBudgetClick) }
                )
            }
            item { SettingsSectionHeader("Data Management") }
            item { SettingsItem(Icons.Rounded.Category, "Manage Categories", onClick = { onAction(SettingsAction.OnManageCategoriesClick) }) }
            item { SettingsItem(Icons.Rounded.Folder, "Manage Sub-Categories", onClick = { onAction(SettingsAction.OnManageSubCategoriesClick) }) }
            item { SettingsSectionHeader("App Info") }
            item { SettingsItem(Icons.Rounded.Info, "About", onClick = { onAction(SettingsAction.OnAboutClick) }) }
            item { SettingsItem(Icons.Rounded.Security, "Privacy Policy", onClick = { onAction(SettingsAction.OnPrivacyPolicyClick) }) }
            item { SettingsItem(Icons.Rounded.Gavel, "Terms of Service", onClick = { onAction(SettingsAction.OnTermsClick) }) }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .bounceClick(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

val settingsModule = org.koin.dsl.module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::NotificationSettingsViewModel)
    viewModelOf(::AppLockSetupViewModel)
}
