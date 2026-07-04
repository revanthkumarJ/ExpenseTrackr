package com.revanthdev.expensetrackr.feature.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.component.AnimatedProgressBar
import com.revanthdev.expensetrackr.core.designsystem.theme.BudgetGreen
import com.revanthdev.expensetrackr.core.designsystem.theme.BudgetRed
import com.revanthdev.expensetrackr.core.designsystem.theme.BudgetYellow
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.dashboard_budget_monthly
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BudgetProgressBar(spent: String, budget: String, progress: Float, modifier: Modifier = Modifier) {
    val color = when {
        progress < 0.7f -> BudgetGreen
        progress < 0.9f -> BudgetYellow
        else -> BudgetRed
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(Res.string.dashboard_budget_monthly), style = MaterialTheme.typography.titleSmall)
                Text("$spent / $budget", style = MaterialTheme.typography.titleSmall, color = color)
            }
            Spacer(Modifier.height(10.dp))
            AnimatedProgressBar(progress = progress, color = color, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview
@Composable
private fun BudgetProgressBarPreview() {
    ExpenseTrackerTheme {
        BudgetProgressBar(spent = "₹18,000", budget = "₹25,000", progress = 0.72f)
    }
}
