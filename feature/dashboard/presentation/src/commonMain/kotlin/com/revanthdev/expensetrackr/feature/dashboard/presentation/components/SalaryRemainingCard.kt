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
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.dashboard_salary_of
import expensetrackr.core.presentation.generated.resources.dashboard_salary_overspent
import expensetrackr.core.presentation.generated.resources.dashboard_salary_remaining
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SalaryRemainingCard(salary: Double, spent: Double, modifier: Modifier = Modifier) {
    val remaining = salary - spent
    val progress = if (salary > 0) (spent / salary).toFloat() else 0f
    val overspent = remaining < 0
    val accent = when {
        overspent -> BudgetRed
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
                Text(
                    stringResource(if (overspent) Res.string.dashboard_salary_overspent else Res.string.dashboard_salary_remaining),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(remaining.toCurrencyString(), style = MaterialTheme.typography.titleSmall, color = accent)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(Res.string.dashboard_salary_of, spent.toCurrencyString(), salary.toCurrencyString()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            AnimatedProgressBar(progress = progress.coerceIn(0f, 1f), color = accent, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview
@Composable
private fun SalaryRemainingCardPreview() {
    ExpenseTrackerTheme {
        SalaryRemainingCard(salary = 50000.0, spent = 32000.0)
    }
}
