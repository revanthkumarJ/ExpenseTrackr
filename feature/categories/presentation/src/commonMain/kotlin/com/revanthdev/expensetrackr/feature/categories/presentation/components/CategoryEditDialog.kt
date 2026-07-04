package com.revanthdev.expensetrackr.feature.categories.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_cancel
import expensetrackr.core.presentation.generated.resources.action_save
import expensetrackr.core.presentation.generated.resources.categories_edit
import expensetrackr.core.presentation.generated.resources.categories_new
import expensetrackr.core.presentation.generated.resources.field_icon_emoji
import expensetrackr.core.presentation.generated.resources.field_name
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CategoryEditDialog(
    isNew: Boolean,
    name: String,
    icon: String,
    onNameChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (isNew) Res.string.categories_new else Res.string.categories_edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(Res.string.field_name)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = icon,
                    onValueChange = { if (it.length <= 2) onIconChange(it) },
                    label = { Text(stringResource(Res.string.field_icon_emoji)) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) { Text(stringResource(Res.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) } }
    )
}

@Preview
@Composable
private fun CategoryEditDialogPreview() {
    ExpenseTrackerTheme {
        CategoryEditDialog(
            isNew = true,
            name = "Groceries",
            icon = "🛒",
            onNameChange = {},
            onIconChange = {},
            onSave = {},
            onDismiss = {}
        )
    }
}
