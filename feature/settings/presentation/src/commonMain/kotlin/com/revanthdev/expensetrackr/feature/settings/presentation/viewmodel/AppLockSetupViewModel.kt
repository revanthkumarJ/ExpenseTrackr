package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.data.util.hashPin
import com.revanthdev.expensetrackr.core.domain.model.AppLockType
import com.revanthdev.expensetrackr.core.domain.model.AppSettings
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppLockSetupViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    private val _state = MutableStateFlow(AppSettings())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { _state.value = it }
        }
    }

    /** Sets (or changes) the unlock PIN. Keeps biometric enabled if it already was. */
    fun setPin(pin: String) {
        viewModelScope.launch {
            val current = _state.value
            val keepBiometric = current.appLockType == AppLockType.BOTH || current.appLockType == AppLockType.BIOMETRIC
            settingsRepository.updateSettings(
                current.copy(
                    appLockType = if (keepBiometric) AppLockType.BOTH else AppLockType.PIN,
                    pinHash = hashPin(pin)
                )
            )
        }
    }

    /** Biometric is always paired with a PIN fallback, so this is a no-op without a PIN. */
    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = _state.value
            if (current.pinHash == null) return@launch
            settingsRepository.updateSettings(
                current.copy(appLockType = if (enabled) AppLockType.BOTH else AppLockType.PIN)
            )
        }
    }

    fun disableLock() {
        viewModelScope.launch {
            settingsRepository.updateSettings(
                _state.value.copy(appLockType = AppLockType.NONE, pinHash = null)
            )
        }
    }
}
