package com.revanthdev.expensetrackr.feature.categories.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import com.revanthdev.expensetrackr.core.domain.model.Category
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_cancel
import expensetrackr.core.presentation.generated.resources.action_save
import expensetrackr.core.presentation.generated.resources.field_name
import expensetrackr.core.presentation.generated.resources.field_parent_category
import expensetrackr.core.presentation.generated.resources.select_category
import expensetrackr.core.presentation.generated.resources.subcategory_edit
import expensetrackr.core.presentation.generated.resources.subcategory_new
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SubCategoryEditDialog(
    isNew: Boolean,
    name: String,
    categories: List<Category>,
    selectedCategoryId: Long,
    onNameChange: (String) -> Unit,
    onCategoryChange: (Long) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var showCategoryDropdown by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (isNew) Res.string.subcategory_new else Res.string.subcategory_edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(Res.string.field_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    val selectedCat = categories.find { it.id == selectedCategoryId }
                    OutlinedTextField(
                        value = selectedCat?.let { "${it.icon} ${it.name}" } ?: stringResource(Res.string.select_category),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(Res.string.field_parent_category)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showCategoryDropdown) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = showCategoryDropdown, onDismissRequest = { showCategoryDropdown = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.icon} ${cat.name}") },
                                onClick = {
                                    onCategoryChange(cat.id)
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
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
private fun SubCategoryEditDialogPreview() {
    ExpenseTrackerTheme {
        SubCategoryEditDialog(
            isNew = true,
            name = "Dining out",
            categories = listOf(Category(1, "Food", "🍔", "#FF7043", createdAt = LocalDateTime(2024, 1, 1, 0, 0))),
            selectedCategoryId = 1,
            onNameChange = {},
            onCategoryChange = {},
            onSave = {},
            onDismiss = {}
        )
    }
}
