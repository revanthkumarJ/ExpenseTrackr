package com.revanthdev.expensetrackr.feature.applock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.data.util.hashPin
import com.revanthdev.expensetrackr.core.domain.model.AppLockType
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppLockViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    private val _state = MutableStateFlow(AppLockState())
    val state = _state.asStateFlow()
    private val _events = Channel<AppLockEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.getSettingsOnce()
            _state.update {
                it.copy(
                    showBiometric = settings.appLockType == AppLockType.BIOMETRIC || settings.appLockType == AppLockType.BOTH
                )
            }
            if (_state.value.showBiometric) {
                _events.send(AppLockEvent.TriggerBiometric)
            }
        }
    }

    fun onAction(action: AppLockAction) {
        when (action) {
            is AppLockAction.OnPinDigit -> {
                val current = _state.value.pin
                if (current.length < 6) {
                    val newPin = current + action.digit
                    _state.update { it.copy(pin = newPin, error = null) }
                    if (newPin.length == 6) checkPin(newPin)
                }
            }
            AppLockAction.OnPinBackspace -> {
                _state.update { it.copy(pin = it.pin.dropLast(1), error = null) }
            }
            AppLockAction.OnBiometricRequest -> {
                viewModelScope.launch { _events.send(AppLockEvent.TriggerBiometric) }
            }
        }
    }

    private fun checkPin(pin: String) {
        viewModelScope.launch {
            val settings = settingsRepository.getSettingsOnce()
            val hash = hashPin(pin)
            if (hash == settings.pinHash) {
                _events.send(AppLockEvent.Unlocked)
            } else if (pin.length == 6) {
                _state.update { it.copy(error = "Incorrect PIN. Try again.", pin = "") }
            }
        }
    }

    fun onBiometricSuccess() {
        viewModelScope.launch { _events.send(AppLockEvent.Unlocked) }
    }
}
