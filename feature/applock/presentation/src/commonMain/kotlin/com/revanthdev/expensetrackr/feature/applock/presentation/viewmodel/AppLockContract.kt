package com.revanthdev.expensetrackr.feature.applock.presentation

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
