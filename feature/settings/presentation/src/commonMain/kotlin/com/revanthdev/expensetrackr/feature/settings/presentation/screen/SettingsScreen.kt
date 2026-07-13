package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.presentation.LocalShareHandler
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
import expensetrackr.core.presentation.generated.resources.settings_share
import expensetrackr.core.presentation.generated.resources.settings_sync
import expensetrackr.core.presentation.generated.resources.share_app_message
import expensetrackr.core.presentation.generated.resources.settings_terms
import expensetrackr.core.presentation.generated.resources.settings_theme
import expensetrackr.core.presentation.generated.resources.settings_theme_dark
import expensetrackr.core.presentation.generated.resources.settings_theme_light
import expensetrackr.core.presentation.generated.resources.settings_theme_system
import org.jetbrains.compose.resources.stringResource

private const val PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=com.revanthdev.expensetrackr"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(state: SettingsState, onAction: (SettingsAction) -> Unit) {
    val shareHandler = LocalShareHandler.current
    val shareMessage = stringResource(Res.string.share_app_message, PLAY_STORE_URL)

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
                    onClick = { onAction(SettingsAction.OnThemeClick) }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Rounded.Language,
                    title = stringResource(Res.string.settings_language),
                    subtitle = appLanguages.find { it.tag == state.settings.language }?.nativeName
                        ?: stringResource(Res.string.language_system_default),
                    onClick = { onAction(SettingsAction.OnLanguageClick) }
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
            item { SettingsItem(Icons.Rounded.CloudSync, stringResource(Res.string.settings_sync), onClick = { onAction(SettingsAction.OnSyncClick) }) }
            item { SettingsSectionHeader(stringResource(Res.string.settings_section_app_info)) }
            item { SettingsItem(Icons.Rounded.Share, stringResource(Res.string.settings_share), onClick = { shareHandler.share(shareMessage) }) }
            item { SettingsItem(Icons.Rounded.Info, stringResource(Res.string.settings_about), onClick = { onAction(SettingsAction.OnAboutClick) }) }
            item { SettingsItem(Icons.Rounded.Security, stringResource(Res.string.settings_privacy), onClick = { onAction(SettingsAction.OnPrivacyPolicyClick) }) }
            item { SettingsItem(Icons.Rounded.Gavel, stringResource(Res.string.settings_terms), onClick = { onAction(SettingsAction.OnTermsClick) }) }
        }
    }
}
