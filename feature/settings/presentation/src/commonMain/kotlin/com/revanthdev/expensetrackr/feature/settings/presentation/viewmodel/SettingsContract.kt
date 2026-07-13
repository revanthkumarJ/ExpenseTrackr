package com.revanthdev.expensetrackr.feature.settings.presentation

import com.revanthdev.expensetrackr.core.domain.model.AppSettings

data class SettingsState(val settings: AppSettings = AppSettings(), val isLoading: Boolean = true)

sealed interface SettingsAction {
    data class OnThemeChange(val isDark: Boolean?) : SettingsAction
    data class OnLanguageChange(val tag: String?) : SettingsAction
    data object OnThemeClick : SettingsAction
    data object OnLanguageClick : SettingsAction
    data object OnBudgetClick : SettingsAction
    data object OnManageCategoriesClick : SettingsAction
    data object OnManageSubCategoriesClick : SettingsAction
    data object OnAppLockClick : SettingsAction
    data object OnNotificationSettingsClick : SettingsAction
    data object OnAboutClick : SettingsAction
    data object OnPrivacyPolicyClick : SettingsAction
    data object OnTermsClick : SettingsAction
    data object OnSyncClick : SettingsAction
}

sealed interface SettingsEvent {
    data object NavigateToTheme : SettingsEvent
    data object NavigateToLanguage : SettingsEvent
    data object NavigateToBudget : SettingsEvent
    data object NavigateToManageCategories : SettingsEvent
    data object NavigateToManageSubCategories : SettingsEvent
    data object NavigateToAppLockSetup : SettingsEvent
    data object NavigateToNotificationSettings : SettingsEvent
    data object NavigateToAbout : SettingsEvent
    data object NavigateToPrivacyPolicy : SettingsEvent
    data object NavigateToTerms : SettingsEvent
    data object NavigateToSync : SettingsEvent
}
