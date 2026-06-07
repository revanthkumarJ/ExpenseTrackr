package com.revanthdev.expensetrackr.feature.onboarding.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import com.revanthdev.expensetrackr.core.presentation.appLanguages
import expensetrackr.core.presentation.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf

@Serializable
data object OnboardingRoute

data class OnboardingPage(val emoji: String, val title: String, val description: String)

sealed interface OnboardingEvent {
    data object NavigateToMain : OnboardingEvent
}

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

@Composable
fun OnboardingScreen(
    currentLanguage: String?,
    onSelectLanguage: (String?) -> Unit,
    onGetStarted: () -> Unit
) {
    val infoPages = listOf(
        OnboardingPage("💰", stringResource(Res.string.onboarding_welcome_title), stringResource(Res.string.onboarding_welcome_desc)),
        OnboardingPage("📂", stringResource(Res.string.onboarding_track_title), stringResource(Res.string.onboarding_track_desc)),
        OnboardingPage("📊", stringResource(Res.string.onboarding_analytics_title), stringResource(Res.string.onboarding_analytics_desc)),
        OnboardingPage("🔒", stringResource(Res.string.onboarding_privacy_title), stringResource(Res.string.onboarding_privacy_desc)),
    )
    val pageCount = infoPages.size + 1 // page 0 = language picker
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.CenterEnd) {
            if (pagerState.currentPage < pageCount - 1) {
                TextButton(onClick = onGetStarted) { Text(stringResource(Res.string.onboarding_skip)) }
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            if (page == 0) {
                LanguageSelectionPage(currentLanguage, onSelectLanguage)
            } else {
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                OnboardingPageContent(infoPages[page - 1], pageOffset)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pageCount) { index ->
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
            val isLast = pagerState.currentPage == pageCount - 1
            Button(
                onClick = {
                    if (isLast) onGetStarted()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(stringResource(if (isLast) Res.string.onboarding_get_started else Res.string.onboarding_next), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun LanguageSelectionPage(currentLanguage: String?, onSelectLanguage: (String?) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Text("🌐", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(Res.string.onboarding_language_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(Res.string.onboarding_language_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item {
                LanguageOption(stringResource(Res.string.language_system_default), currentLanguage == null) {
                    onSelectLanguage(null)
                }
            }
            items(appLanguages) { lang ->
                LanguageOption(lang.nativeName, currentLanguage == lang.tag) { onSelectLanguage(lang.tag) }
            }
        }
    }
}

@Composable
private fun LanguageOption(name: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(12.dp))
            Text(
                name,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage, pageOffset: Float) {
    val clamped = pageOffset.absoluteValue.coerceIn(0f, 1f)
    val scale = lerp(0.82f, 1f, 1f - clamped)
    val contentAlpha = lerp(0.3f, 1f, 1f - clamped)
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = contentAlpha
                    translationX = pageOffset * 120f
                }
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(page.emoji, style = MaterialTheme.typography.displayLarge)
        }
        Spacer(Modifier.height(40.dp))
        Box(Modifier.graphicsLayer { alpha = contentAlpha; translationX = pageOffset * 60f }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(page.title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Text(
                    page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

val onboardingModule = org.koin.dsl.module {
    viewModelOf(::OnboardingViewModel)
}
