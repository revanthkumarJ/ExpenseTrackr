package com.revanthdev.expensetrackr.feature.analytics.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import com.revanthdev.expensetrackr.core.presentation.util.toPercentString
import kotlinx.datetime.LocalDateTime
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.PI

@Composable
internal fun DonutChart(
    stats: List<CategoryStat>,
    centerLabel: String,
    centerValue: String,
    modifier: Modifier = Modifier
) {
    val total = stats.sumOf { it.total }.toFloat()
    val sweepProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(900),
        label = "donutSweep"
    )
    // Which slice the user tapped, or null when nothing is selected. Tapping a slice shows
    // that category in the center; tapping the same slice again (or empty space) clears it.
    var selected by remember(stats) { mutableStateOf<CategoryStat?>(null) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(stats, total) {
                    detectTapGestures { tap ->
                        val hit = sliceAt(tap, size.width.toFloat(), size.height.toFloat(), stats, total)
                        selected = if (hit == selected) null else hit
                    }
                }
        ) {
            val stroke = size.minDimension * 0.16f
            val diameter = size.minDimension * 0.82f - stroke
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            var startAngle = -90f
            stats.forEach { stat ->
                val full = if (total > 0) ((stat.total / total) * 360f).toFloat() else 0f
                val sweep = full * sweepProgress
                val isSelected = stat == selected
                // Dim the other slices when one is selected so the choice stands out.
                val color = if (selected == null || isSelected) stat.color else stat.color.copy(alpha = 0.3f)
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep - 3f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = if (isSelected) stroke * 1.22f else stroke, cap = StrokeCap.Round)
                )
                startAngle += full
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            val sel = selected
            if (sel == null) {
                Text(centerLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(centerValue, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            } else {
                Text(
                    "${sel.category.icon} ${sel.category.name}",
                    style = MaterialTheme.typography.labelLarge,
                    color = sel.color,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Text(sel.total.toCurrencyString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    sel.percentage.toPercentString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Returns the [CategoryStat] whose donut slice contains [tap], or null if the tap falls outside
 * the ring. Mirrors the geometry used when drawing so hit-testing stays in sync with the arcs.
 */
private fun sliceAt(
    tap: Offset,
    width: Float,
    height: Float,
    stats: List<CategoryStat>,
    total: Float
): CategoryStat? {
    if (total <= 0f) return null
    val minDim = min(width, height)
    val stroke = minDim * 0.16f
    val radius = (minDim * 0.82f - stroke) / 2f
    val cx = width / 2f
    val cy = height / 2f
    val dx = tap.x - cx
    val dy = tap.y - cy
    val dist = hypot(dx, dy)
    // Only count taps that land on the ring band (with a little touch tolerance).
    if (dist < radius - stroke || dist > radius + stroke) return null

    // Canvas angles: 0° = 3 o'clock, growing clockwise (y grows downward), same as atan2 here.
    var angle = (atan2(dy, dx) * 180f / PI.toFloat())
    angle = (angle + 360f) % 360f

    var start = -90f
    stats.forEach { stat ->
        val full = ((stat.total / total) * 360f).toFloat()
        val rel = ((angle - start) % 360f + 360f) % 360f
        if (rel < full) return stat
        start += full
    }
    return null
}

@Preview
@Composable
private fun DonutChartPreview() {
    val createdAt = LocalDateTime(2024, 1, 1, 0, 0)
    ExpenseTrackerTheme {
        DonutChart(
            stats = listOf(
                CategoryStat(Category(1, "Food", "🍔", "#FF7043", createdAt = createdAt), 6000.0, 60.0, Color(0xFFFF7043)),
                CategoryStat(Category(2, "Travel", "✈️", "#42A5F5", createdAt = createdAt), 4000.0, 40.0, Color(0xFF42A5F5)),
            ),
            centerLabel = "Total",
            centerValue = "₹10,000",
            modifier = Modifier.fillMaxSize().height(260.dp)
        )
    }
}
