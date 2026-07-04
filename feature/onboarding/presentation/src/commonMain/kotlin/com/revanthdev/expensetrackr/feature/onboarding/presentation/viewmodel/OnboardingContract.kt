package com.revanthdev.expensetrackr.feature.onboarding.presentation

sealed interface OnboardingEvent {
    data object NavigateToMain : OnboardingEvent
}
