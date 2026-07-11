package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
            SettingsAction.OnSyncClick -> viewModelScope.launch { _events.send(SettingsEvent.NavigateToSync) }
        }
    }
}
