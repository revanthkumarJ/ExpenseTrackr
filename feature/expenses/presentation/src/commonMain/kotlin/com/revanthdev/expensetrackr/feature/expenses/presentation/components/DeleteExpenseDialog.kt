package com.revanthdev.expensetrackr.feature.expenses.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_cancel
import expensetrackr.core.presentation.generated.resources.action_delete
import expensetrackr.core.presentation.generated.resources.delete_expense_message
import expensetrackr.core.presentation.generated.resources.delete_expense_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DeleteExpenseDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.delete_expense_title)) },
        text = { Text(stringResource(Res.string.delete_expense_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(Res.string.action_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.action_cancel))
            }
        }
    )
}

@Preview
@Composable
private fun DeleteExpenseDialogPreview() {
    ExpenseTrackerTheme {
        DeleteExpenseDialog(onConfirm = {}, onDismiss = {})
    }
}
