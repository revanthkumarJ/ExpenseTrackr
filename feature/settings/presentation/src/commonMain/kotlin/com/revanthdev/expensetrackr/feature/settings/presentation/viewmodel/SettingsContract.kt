package com.revanthdev.expensetrackr.feature.settings.presentation

import com.revanthdev.expensetrackr.core.domain.model.AppSettings

data class SettingsState(val settings: AppSettings = AppSettings(), val isLoading: Boolean = true)

sealed interface SettingsAction {
    data class OnThemeChange(val isDark: Boolean?) : SettingsAction
    data class OnLanguageChange(val tag: String?) : SettingsAction
    /** amount == null clears the salary; applyToAll controls retroactive vs from-this-month scope. */
    data class OnSalaryUpdate(val amount: Double?, val applyToAll: Boolean) : SettingsAction
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
