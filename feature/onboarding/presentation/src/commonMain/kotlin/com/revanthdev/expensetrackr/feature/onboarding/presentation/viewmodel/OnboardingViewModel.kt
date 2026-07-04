package com.revanthdev.expensetrackr.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    private val _events = Channel<OnboardingEvent>()
    val events = _events.receiveAsFlow()

    val language: StateFlow<String?> = settingsRepository.getSettings()
        .map { it.language }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setLanguage(tag: String?) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsOnce()
            settingsRepository.updateSettings(current.copy(language = tag))
        }
    }

    fun onGetStarted() {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsOnce()
            settingsRepository.updateSettings(current.copy(isOnboardingDone = true))
            _events.send(OnboardingEvent.NavigateToMain)
        }
    }
}
