package com.revanthdev.expensetrackr.feature.onboarding.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import kotlin.math.absoluteValue
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** [logo], when set, renders the app logo image in the hero circle instead of [emoji]. */
data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String,
    val logo: DrawableResource? = null
)

@Composable
internal fun OnboardingPageContent(page: OnboardingPage, pageOffset: Float) {
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
            if (page.logo != null) {
                Image(
                    painter = painterResource(page.logo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(page.emoji, style = MaterialTheme.typography.displayLarge)
            }
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

@Preview
@Composable
private fun OnboardingPageContentPreview() {
    ExpenseTrackerTheme {
        OnboardingPageContent(
            page = OnboardingPage("💰", "Welcome to ExpenseTrackr", "Track every rupee with ease."),
            pageOffset = 0f
        )
    }
}
