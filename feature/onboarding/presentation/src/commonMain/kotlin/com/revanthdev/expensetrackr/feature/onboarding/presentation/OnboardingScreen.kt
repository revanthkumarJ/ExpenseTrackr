package com.revanthdev.expensetrackr.feature.onboarding.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf

@Serializable
data object OnboardingRoute

data class OnboardingPage(val emoji: String, val title: String, val description: String)

private val pages = listOf(
    OnboardingPage("💰", "Welcome to ExpenseTrackr", "Track every rupee. Private, offline, and beautifully simple."),
    OnboardingPage("📂", "Track Your Spending", "Organize expenses into categories and sub-categories. See exactly where your money goes."),
    OnboardingPage("📊", "Smart Analytics", "Beautiful pie charts and budget tracking help you stay on top of your finances."),
    OnboardingPage("🔒", "Your Data, Your Device", "No internet. No cloud. No accounts. All your data stays on your device — always."),
)

sealed interface OnboardingEvent {
    data object NavigateToMain : OnboardingEvent
}

class OnboardingViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    private val _events = Channel<OnboardingEvent>()
    val events = _events.receiveAsFlow()

    fun onGetStarted() {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsOnce()
            settingsRepository.updateSettings(current.copy(isOnboardingDone = true))
            _events.send(OnboardingEvent.NavigateToMain)
        }
    }
}

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
    OnboardingScreen(onGetStarted = viewModel::onGetStarted)
}

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.CenterEnd) {
            if (pagerState.currentPage < pages.lastIndex) {
                TextButton(onClick = onGetStarted) { Text("Skip") }
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            OnboardingPageContent(pages[page])
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            if (pagerState.currentPage == pages.lastIndex) {
                Button(onClick = onGetStarted, modifier = Modifier.fillMaxWidth()) {
                    Text("Get Started")
                }
            } else {
                Button(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next")
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(page.emoji, style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(32.dp))
        Text(page.title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(
            page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

val onboardingModule = org.koin.dsl.module {
    viewModelOf(::OnboardingViewModel)
}
