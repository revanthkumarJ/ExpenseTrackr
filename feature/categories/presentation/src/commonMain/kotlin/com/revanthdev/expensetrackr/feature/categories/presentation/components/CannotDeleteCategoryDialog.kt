package com.revanthdev.expensetrackr.feature.categories.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_ok
import expensetrackr.core.presentation.generated.resources.cannot_delete_message
import expensetrackr.core.presentation.generated.resources.cannot_delete_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CannotDeleteCategoryDialog(categoryName: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.cannot_delete_title)) },
        text = { Text(stringResource(Res.string.cannot_delete_message, categoryName)) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_ok)) } }
    )
}

@Preview
@Composable
private fun CannotDeleteCategoryDialogPreview() {
    ExpenseTrackerTheme {
        CannotDeleteCategoryDialog(categoryName = "Food", onDismiss = {})
    }
}
