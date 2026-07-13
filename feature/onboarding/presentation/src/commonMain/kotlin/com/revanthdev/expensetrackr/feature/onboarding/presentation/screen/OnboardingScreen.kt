package com.revanthdev.expensetrackr.feature.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.app_logo
import expensetrackr.core.presentation.generated.resources.onboarding_analytics_desc
import expensetrackr.core.presentation.generated.resources.onboarding_analytics_title
import expensetrackr.core.presentation.generated.resources.onboarding_get_started
import expensetrackr.core.presentation.generated.resources.onboarding_next
import expensetrackr.core.presentation.generated.resources.onboarding_privacy_desc
import expensetrackr.core.presentation.generated.resources.onboarding_privacy_title
import expensetrackr.core.presentation.generated.resources.onboarding_skip
import expensetrackr.core.presentation.generated.resources.onboarding_track_desc
import expensetrackr.core.presentation.generated.resources.onboarding_track_title
import expensetrackr.core.presentation.generated.resources.onboarding_welcome_desc
import expensetrackr.core.presentation.generated.resources.onboarding_welcome_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingScreen(
    currentLanguage: String?,
    onSelectLanguage: (String?) -> Unit,
    onGetStarted: () -> Unit
) {
    val infoPages = listOf(
        OnboardingPage("💰", stringResource(Res.string.onboarding_welcome_title), stringResource(Res.string.onboarding_welcome_desc), logo = Res.drawable.app_logo),
        OnboardingPage("📂", stringResource(Res.string.onboarding_track_title), stringResource(Res.string.onboarding_track_desc)),
        OnboardingPage("📊", stringResource(Res.string.onboarding_analytics_title), stringResource(Res.string.onboarding_analytics_desc)),
        OnboardingPage("🔒", stringResource(Res.string.onboarding_privacy_title), stringResource(Res.string.onboarding_privacy_desc)),
    )
    val pageCount = infoPages.size + 1 // page 0 = language picker
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    Scaffold {

        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (pagerState.currentPage < pageCount - 1) {
                    TextButton(onClick = onGetStarted) { Text(stringResource(Res.string.onboarding_skip)) }
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                if (page == 0) {
                    LanguageSelectionPage(currentLanguage, onSelectLanguage)
                } else {
                    val pageOffset =
                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
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
                    Text(
                        stringResource(if (isLast) Res.string.onboarding_get_started else Res.string.onboarding_next),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
