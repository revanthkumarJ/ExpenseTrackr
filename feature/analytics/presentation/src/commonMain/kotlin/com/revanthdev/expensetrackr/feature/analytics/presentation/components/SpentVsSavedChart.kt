package com.revanthdev.expensetrackr.feature.analytics.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.theme.BudgetGreen
import com.revanthdev.expensetrackr.core.designsystem.theme.BudgetRed
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.analytics_saved
import expensetrackr.core.presentation.generated.resources.analytics_spent
import org.jetbrains.compose.resources.stringResource

/**
 * Two horizontal bars comparing spend vs savings, each scaled against income so their
 * lengths are directly comparable. When overspent, the saved bar is empty and shown in red.
 */
@Composable
internal fun SpentVsSavedChart(spent: Double, saved: Double, income: Double, modifier: Modifier = Modifier) {
    val denominator = maxOf(income, spent, 1.0)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            BarRow(
                label = stringResource(Res.string.analytics_spent),
                value = spent.toCurrencyString(),
                fraction = (spent / denominator).toFloat().coerceIn(0f, 1f),
                color = BudgetRed
            )
            BarRow(
                label = stringResource(Res.string.analytics_saved),
                value = saved.toCurrencyString(),
                fraction = (saved / denominator).toFloat().coerceIn(0f, 1f),
                color = if (saved < 0) BudgetRed else BudgetGreen
            )
        }
    }
}

@Composable
private fun BarRow(label: String, value: String, fraction: Float, color: Color) {
    val animated by animateFloatAsState(targetValue = fraction, animationSpec = tween(700), label = "barFill")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = color)
        }
        Box(
            Modifier.fillMaxWidth().height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                Modifier.fillMaxWidth(animated).fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }
    }
}

@Preview
@Composable
private fun SpentVsSavedChartPreview() {
    ExpenseTrackerTheme {
        SpentVsSavedChart(spent = 30000.0, saved = 20000.0, income = 50000.0, modifier = Modifier.fillMaxWidth())
    }
}
