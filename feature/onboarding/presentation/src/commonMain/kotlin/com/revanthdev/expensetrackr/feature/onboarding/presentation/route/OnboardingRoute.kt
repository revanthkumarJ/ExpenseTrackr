package com.revanthdev.expensetrackr.feature.onboarding.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object OnboardingRoute

@Composable
fun OnboardingRoot(
    onNavigateToMain: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            OnboardingEvent.NavigateToMain -> onNavigateToMain()
        }
    }
    val language by viewModel.language.collectAsState()
    OnboardingScreen(
        currentLanguage = language,
        onSelectLanguage = viewModel::setLanguage,
        onGetStarted = viewModel::onGetStarted
    )
}
