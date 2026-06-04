package com.revanthdev.expensetrackr.feature.applock.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.domain.model.AppLockType
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
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
                    if (newPin.length >= 4) checkPin(newPin)
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

expect fun hashPin(pin: String): String

@Composable
fun AppLockRoot(
    onUnlocked: () -> Unit,
    onTriggerBiometric: () -> Unit,
    viewModel: AppLockViewModel = koinViewModel()
) {
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            AppLockEvent.Unlocked -> onUnlocked()
            AppLockEvent.TriggerBiometric -> onTriggerBiometric()
        }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    AppLockScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun AppLockScreen(state: AppLockState, onAction: (AppLockAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(24.dp))
        Text("Enter PIN", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(6) { i ->
                Box(
                    modifier = Modifier.size(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (i < state.pin.length) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier.size(12.dp)
                    ) {}
                }
            }
        }

        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("1 2 3", "4 5 6", "7 8 9", "  0 ⌫").forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    row.trim().split(" ").filter { it.isNotEmpty() }.forEach { key ->
                        FilledTonalButton(
                            onClick = {
                                if (key == "⌫") onAction(AppLockAction.OnPinBackspace)
                                else onAction(AppLockAction.OnPinDigit(key))
                            },
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Text(key, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }

        if (state.showBiometric) {
            Spacer(Modifier.height(24.dp))
            OutlinedButton(onClick = { onAction(AppLockAction.OnBiometricRequest) }) {
                Icon(Icons.Rounded.Fingerprint, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Use Biometric")
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Forgot PIN? Clear app data to reset.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun String.collectAsStateWithLifecycle() = this

@Composable
private fun MutableStateFlow<AppLockState>.collectAsStateWithLifecycle(): State<AppLockState> =
    this.asStateFlow().collectAsState()

val appLockModule = org.koin.dsl.module {
    viewModelOf(::AppLockViewModel)
}
