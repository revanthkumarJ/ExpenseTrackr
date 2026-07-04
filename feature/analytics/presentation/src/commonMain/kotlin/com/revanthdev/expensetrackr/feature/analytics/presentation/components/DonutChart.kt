package com.revanthdev.expensetrackr.feature.analytics.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import com.revanthdev.expensetrackr.core.domain.model.Category
import kotlinx.datetime.LocalDateTime

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
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.16f
            val diameter = size.minDimension * 0.82f - stroke
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            var startAngle = -90f
            stats.forEach { stat ->
                val full = if (total > 0) ((stat.total / total) * 360f).toFloat() else 0f
                val sweep = full * sweepProgress
                drawArc(
                    color = stat.color,
                    startAngle = startAngle,
                    sweepAngle = sweep - 3f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                startAngle += full
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(centerValue, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
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
