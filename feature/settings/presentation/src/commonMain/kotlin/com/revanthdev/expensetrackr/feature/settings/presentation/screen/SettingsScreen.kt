package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.presentation.appLanguages
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.common_not_set
import expensetrackr.core.presentation.generated.resources.language_system_default
import expensetrackr.core.presentation.generated.resources.nav_settings
import expensetrackr.core.presentation.generated.resources.settings_about
import expensetrackr.core.presentation.generated.resources.settings_app_lock
import expensetrackr.core.presentation.generated.resources.settings_budget_mgmt
import expensetrackr.core.presentation.generated.resources.settings_budget_value
import expensetrackr.core.presentation.generated.resources.settings_language
import expensetrackr.core.presentation.generated.resources.settings_manage_categories
import expensetrackr.core.presentation.generated.resources.settings_manage_subcategories
import expensetrackr.core.presentation.generated.resources.settings_privacy
import expensetrackr.core.presentation.generated.resources.settings_section_app_info
import expensetrackr.core.presentation.generated.resources.settings_section_budget
import expensetrackr.core.presentation.generated.resources.settings_section_data
import expensetrackr.core.presentation.generated.resources.settings_section_preferences
import expensetrackr.core.presentation.generated.resources.settings_terms
import expensetrackr.core.presentation.generated.resources.settings_theme
import expensetrackr.core.presentation.generated.resources.settings_theme_dark
import expensetrackr.core.presentation.generated.resources.settings_theme_light
import expensetrackr.core.presentation.generated.resources.settings_theme_system
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(state: SettingsState, onAction: (SettingsAction) -> Unit) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        LanguageDialog(
            currentLanguage = state.settings.language,
            onSelect = { onAction(SettingsAction.OnLanguageChange(it)) },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showThemeDialog) {
        ThemeDialog(
            current = state.settings.isDarkMode,
            onSelect = { onAction(SettingsAction.OnThemeChange(it)) },
            onDismiss = { showThemeDialog = false }
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
            item { SettingsSectionHeader(stringResource(Res.string.settings_section_budget)) }
            item {
                SettingsItem(
                    icon = Icons.Rounded.AccountBalance,
                    title = stringResource(Res.string.settings_budget_mgmt),
                    subtitle = state.settings.overallMonthlyBudget?.let { stringResource(Res.string.settings_budget_value, it.toCurrencyString()) } ?: stringResource(Res.string.common_not_set),
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
