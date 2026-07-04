package com.revanthdev.expensetrackr.feature.analytics.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.analytics_spending_trend
import expensetrackr.core.presentation.generated.resources.analytics_spending_trend_sub
import org.jetbrains.compose.resources.stringResource

private val BarMaxHeight = 120.dp

@Composable
internal fun SpendingBarChart(buckets: List<TimeBucket>, modifier: Modifier = Modifier) {
    val maxTotal = buckets.maxOfOrNull { it.total } ?: 0.0
    if (buckets.isEmpty() || maxTotal <= 0.0) return
    val peakIndex = buckets.indexOfFirst { it.total == maxTotal }

    // Selection drives the highlighted bar + the header value; defaults to the peak day.
    // Keyed on buckets so switching periods resets to the new peak.
    var selectedIndex by remember(buckets) { mutableIntStateOf(peakIndex) }
    val selected = buckets[selectedIndex]

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(Res.string.analytics_spending_trend),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(Res.string.analytics_spending_trend_sub),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${selected.label} · ${selected.total.toCurrencyString()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(BarMaxHeight + 22.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                buckets.forEachIndexed { index, bucket ->
                    BarColumn(
                        label = bucket.label,
                        fraction = (bucket.total / maxTotal).toFloat(),
                        selected = index == selectedIndex,
                        onClick = { selectedIndex = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BarColumn(
    label: String,
    fraction: Float,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "barFill"
    )
    val barColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
    }
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .fillMaxHeight()
            // No ripple — a full-height ripple over a thin bar looks wrong; selection is the feedback.
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .fillMaxWidth(0.7f)
                // A hairline minimum so empty days still read as a bar on the axis.
                .height((BarMaxHeight * animated).coerceAtLeast(2.dp))
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(barColor)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun SpendingBarChartPreview() {
    ExpenseTrackerTheme {
        SpendingBarChart(
            buckets = listOf(
                TimeBucket("Sun", 150.0),
                TimeBucket("Mon", 400.0),
                TimeBucket("Tue", 1200.0),
                TimeBucket("Wed", 300.0),
                TimeBucket("Thu", 900.0),
                TimeBucket("Fri", 1800.0),
                TimeBucket("Sat", 600.0),
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
