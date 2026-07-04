package com.revanthdev.expensetrackr.feature.expenses.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_ok
import expensetrackr.core.presentation.generated.resources.budget_warn_category
import expensetrackr.core.presentation.generated.resources.budget_warn_monthly
import expensetrackr.core.presentation.generated.resources.over_budget_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BudgetWarningDialog(warning: BudgetWarning, onDismiss: () -> Unit) {
    val message = when (warning) {
        is BudgetWarning.Monthly -> stringResource(
            Res.string.budget_warn_monthly,
            warning.projected,
            warning.budget
        )

        is BudgetWarning.Category -> stringResource(
            Res.string.budget_warn_category,
            warning.categoryName,
            warning.projected,
            warning.budget
        )
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.over_budget_title)) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.action_ok))
            }
        }
    )
}

@Preview
@Composable
private fun BudgetWarningDialogPreview() {
    ExpenseTrackerTheme {
        BudgetWarningDialog(
            warning = BudgetWarning.Monthly(projected = "₹27,500", budget = "₹25,000"),
            onDismiss = {}
        )
    }
}
