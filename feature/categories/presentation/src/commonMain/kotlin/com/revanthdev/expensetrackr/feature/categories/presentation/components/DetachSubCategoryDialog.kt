package com.revanthdev.expensetrackr.feature.categories.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_cancel
import expensetrackr.core.presentation.generated.resources.action_delete_detach
import expensetrackr.core.presentation.generated.resources.subcategory_has_expenses_message
import expensetrackr.core.presentation.generated.resources.subcategory_has_expenses_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DetachSubCategoryDialog(
    subCategoryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.subcategory_has_expenses_title)) },
        text = { Text(stringResource(Res.string.subcategory_has_expenses_message, subCategoryName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(Res.string.action_delete_detach), color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) } }
    )
}

@Preview
@Composable
private fun DetachSubCategoryDialogPreview() {
    ExpenseTrackerTheme {
        DetachSubCategoryDialog(subCategoryName = "Dining out", onConfirm = {}, onDismiss = {})
    }
}
