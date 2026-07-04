package com.revanthdev.expensetrackr.feature.budget.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import com.revanthdev.expensetrackr.core.presentation.util.DecimalFormatter
import com.revanthdev.expensetrackr.core.presentation.util.DecimalInputVisualTransformation
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.budget_allow_over_off
import expensetrackr.core.presentation.generated.resources.budget_allow_over_on
import expensetrackr.core.presentation.generated.resources.budget_allow_over_title
import expensetrackr.core.presentation.generated.resources.budget_amount
import expensetrackr.core.presentation.generated.resources.budget_monthly
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OverallBudgetCard(
    enabled: Boolean,
    budgetText: String,
    allowExceedBudget: Boolean,
    onToggle: (Boolean) -> Unit,
    onBudgetChange: (String) -> Unit,
    onAllowExceedToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.budget_monthly), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(checked = enabled, onCheckedChange = onToggle)
            }
            if (enabled) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = onBudgetChange,
                    label = { Text(stringResource(Res.string.budget_amount)) },
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    visualTransformation = DecimalInputVisualTransformation(DecimalFormatter()),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(Res.string.budget_allow_over_title), style = MaterialTheme.typography.titleSmall)
                        Text(
                            stringResource(if (allowExceedBudget) Res.string.budget_allow_over_on else Res.string.budget_allow_over_off),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(checked = allowExceedBudget, onCheckedChange = onAllowExceedToggle)
                }
            }
        }
    }
}

@Preview
@Composable
private fun OverallBudgetCardPreview() {
    ExpenseTrackerTheme {
        OverallBudgetCard(
            enabled = true,
            budgetText = "25000",
            allowExceedBudget = true,
            onToggle = {},
            onBudgetChange = {},
            onAllowExceedToggle = {}
        )
    }
}
