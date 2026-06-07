package com.revanthdev.expensetrackr.feature.applock.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.revanthdev.expensetrackr.core.designsystem.component.PinEntryScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.data.util.hashPin
import com.revanthdev.expensetrackr.core.domain.model.AppLockType
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import com.revanthdev.expensetrackr.core.presentation.LocalBiometricAuthenticator
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf

@Serializable
data object AppLockRoute

data class AppLockState(
    val pin: String = "",
    val error: String? = null,
    val showBiometric: Boolean = false,
    val isLoading: Boolean = false,
)

sealed interface AppLockAction {
    data class OnPinDigit(val digit: String) : AppLockAction
    data object OnPinBackspace : AppLockAction
    data object OnBiometricRequest : AppLockAction
}

sealed interface AppLockEvent {
    data object Unlocked : AppLockEvent
    data object TriggerBiometric : AppLockEvent
}

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

@Composable
fun AppLockRoot(
    onUnlocked: () -> Unit,
    viewModel: AppLockViewModel = koinViewModel()
) {
    val biometric = LocalBiometricAuthenticator.current
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            AppLockEvent.Unlocked -> onUnlocked()
            AppLockEvent.TriggerBiometric -> biometric?.authenticate(
                title = "Unlock ExpenseTrackr",
                subtitle = "Confirm your identity to continue",
                onSuccess = { viewModel.onBiometricSuccess() },
                onError = {}
            )
        }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    AppLockScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun AppLockScreen(state: AppLockState, onAction: (AppLockAction) -> Unit) {
    PinEntryScreen(
        title = "Enter PIN",
        subtitle = "Enter your 6-digit PIN to unlock",
        pin = state.pin,
        error = state.error,
        onDigit = { onAction(AppLockAction.OnPinDigit(it)) },
        onBackspace = { onAction(AppLockAction.OnPinBackspace) },
        biometricIcon = if (state.showBiometric) Icons.Rounded.Fingerprint else null,
        onBiometric = if (state.showBiometric) {
            { onAction(AppLockAction.OnBiometricRequest) }
        } else null
    )
}

private fun String.collectAsStateWithLifecycle() = this

@Composable
private fun MutableStateFlow<AppLockState>.collectAsStateWithLifecycle(): State<AppLockState> =
    this.asStateFlow().collectAsState()

val appLockModule = org.koin.dsl.module {
    viewModelOf(::AppLockViewModel)
}
