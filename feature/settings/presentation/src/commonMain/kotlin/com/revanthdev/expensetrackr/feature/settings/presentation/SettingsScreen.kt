package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.revanthdev.expensetrackr.core.presentation.appLanguages
import expensetrackr.core.presentation.generated.resources.*
import org.jetbrains.compose.resources.stringResource
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
    data class OnLanguageChange(val tag: String?) : SettingsAction
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
            is SettingsAction.OnLanguageChange -> viewModelScope.launch {
                val s = _state.value.settings
                settingsRepository.updateSettings(s.copy(language = action.tag))
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
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        val systemDefault = stringResource(Res.string.language_system_default)
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(Res.string.settings_language)) },
            text = {
                LazyColumn {
                    item {
                        LanguageRow(systemDefault, selected = state.settings.language == null) {
                            onAction(SettingsAction.OnLanguageChange(null)); showLanguageDialog = false
                        }
                    }
                    items(appLanguages) { lang ->
                        LanguageRow(lang.nativeName, selected = state.settings.language == lang.tag) {
                            onAction(SettingsAction.OnLanguageChange(lang.tag)); showLanguageDialog = false
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLanguageDialog = false }) { Text(stringResource(Res.string.action_close)) } }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(Res.string.settings_theme)) },
            text = {
                Column {
                    listOf(
                        null to stringResource(Res.string.settings_theme_system),
                        false to stringResource(Res.string.settings_theme_light),
                        true to stringResource(Res.string.settings_theme_dark)
                    ).forEach { (value, label) ->
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
            confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text(stringResource(Res.string.action_close)) } }
        )
    }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(Res.string.nav_settings)) }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
            item { SettingsSectionHeader(stringResource(Res.string.settings_section_preferences)) }
            item {
                SettingsItem(
                    icon = Icons.Rounded.Brightness6,
                    title = stringResource(Res.string.settings_theme),
                    subtitle = when (state.settings.isDarkMode) {
                        null -> stringResource(Res.string.settings_theme_system)
                        true -> stringResource(Res.string.settings_theme_dark)
                        false -> stringResource(Res.string.settings_theme_light)
                    },
                    onClick = { showThemeDialog = true }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Rounded.Language,
                    title = stringResource(Res.string.settings_language),
                    subtitle = appLanguages.find { it.tag == state.settings.language }?.nativeName
                        ?: stringResource(Res.string.language_system_default),
                    onClick = { showLanguageDialog = true }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Rounded.Lock,
                    title = stringResource(Res.string.settings_app_lock),
                    subtitle = state.settings.appLockType.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { onAction(SettingsAction.OnAppLockClick) }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Rounded.Notifications,
                    title = stringResource(Res.string.settings_notifications),
                    subtitle = if (state.settings.dailyReminderEnabled) stringResource(Res.string.settings_notifications_on) else stringResource(Res.string.settings_notifications_off),
                    onClick = { onAction(SettingsAction.OnNotificationSettingsClick) }
                )
            }
            item { SettingsSectionHeader(stringResource(Res.string.settings_section_budget)) }
            item {
                SettingsItem(
                    icon = Icons.Rounded.AccountBalance,
                    title = stringResource(Res.string.settings_budget_mgmt),
                    subtitle = state.settings.overallMonthlyBudget?.let { stringResource(Res.string.settings_budget_value, "₹$it") } ?: stringResource(Res.string.common_not_set),
                    onClick = { onAction(SettingsAction.OnBudgetClick) }
                )
            }
            item { SettingsSectionHeader(stringResource(Res.string.settings_section_data)) }
            item { SettingsItem(Icons.Rounded.Category, stringResource(Res.string.settings_manage_categories), onClick = { onAction(SettingsAction.OnManageCategoriesClick) }) }
            item { SettingsItem(Icons.Rounded.Folder, stringResource(Res.string.settings_manage_subcategories), onClick = { onAction(SettingsAction.OnManageSubCategoriesClick) }) }
            item { SettingsSectionHeader(stringResource(Res.string.settings_section_app_info)) }
            item { SettingsItem(Icons.Rounded.Info, stringResource(Res.string.settings_about), onClick = { onAction(SettingsAction.OnAboutClick) }) }
            item { SettingsItem(Icons.Rounded.Security, stringResource(Res.string.settings_privacy), onClick = { onAction(SettingsAction.OnPrivacyPolicyClick) }) }
            item { SettingsItem(Icons.Rounded.Gavel, stringResource(Res.string.settings_terms), onClick = { onAction(SettingsAction.OnTermsClick) }) }
        }
    }
}

@Composable
private fun LanguageRow(name: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(8.dp))
        Text(name)
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
