package com.revanthdev.expensetrackr.feature.expenses.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.presentation.util.DecimalFormatter
import com.revanthdev.expensetrackr.core.presentation.util.DecimalInputVisualTransformation
import com.revanthdev.expensetrackr.core.presentation.util.toDisplayDate
import com.revanthdev.expensetrackr.core.presentation.util.toDisplayTime
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_back
import expensetrackr.core.presentation.generated.resources.action_delete
import expensetrackr.core.presentation.generated.resources.add_expense_title
import expensetrackr.core.presentation.generated.resources.common_none
import expensetrackr.core.presentation.generated.resources.edit_expense_title
import expensetrackr.core.presentation.generated.resources.field_amount
import expensetrackr.core.presentation.generated.resources.field_category
import expensetrackr.core.presentation.generated.resources.field_date
import expensetrackr.core.presentation.generated.resources.field_expense_name
import expensetrackr.core.presentation.generated.resources.field_notes
import expensetrackr.core.presentation.generated.resources.field_subcategory
import expensetrackr.core.presentation.generated.resources.field_time
import expensetrackr.core.presentation.generated.resources.save_expense
import expensetrackr.core.presentation.generated.resources.subcategory_none_hint
import expensetrackr.core.presentation.generated.resources.update_expense
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(state: AddEditExpenseState, onAction: (AddEditExpenseAction) -> Unit) {
    val isEdit = state.expenseId != -1L
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showSubCategoryDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        ExpenseDatePickerDialog(
            dateTime = state.dateTime,
            onConfirm = { onAction(AddEditExpenseAction.OnDateTimeChange(it)) },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        ExpenseTimePickerDialog(
            dateTime = state.dateTime,
            onConfirm = { onAction(AddEditExpenseAction.OnDateTimeChange(it)) },
            onDismiss = { showTimePicker = false }
        )
    }

    state.budgetWarning?.let { warning ->
        BudgetWarningDialog(
            warning = warning,
            onDismiss = { onAction(AddEditExpenseAction.OnBudgetWarningDismiss) }
        )
    }

    if (state.showDeleteDialog) {
        DeleteExpenseDialog(
            onConfirm = { onAction(AddEditExpenseAction.OnDeleteConfirm) },
            onDismiss = { onAction(AddEditExpenseAction.OnDeleteDismiss) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (isEdit) Res.string.edit_expense_title else Res.string.add_expense_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(AddEditExpenseAction.OnBack) }) {
                        Icon(Icons.Rounded.ArrowBack, stringResource(Res.string.action_back))
                    }
                },
                actions = {
                    if (isEdit) {
                        IconButton(onClick = { onAction(AddEditExpenseAction.OnDeleteClick) }) {
                            Icon(
                                Icons.Rounded.Delete,
                                stringResource(Res.string.action_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .padding(16.dp),
        ) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { onAction(AddEditExpenseAction.OnNameChange(it.take(100))) },
                    label = { Text(stringResource(Res.string.field_expense_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.nameError != null,
                    supportingText = state.nameError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = state.amountText,
                    onValueChange = { onAction(AddEditExpenseAction.OnAmountChange(it)) },
                    label = { Text(stringResource(Res.string.field_amount)) },
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.amountError != null,
                    supportingText = state.amountError?.let { { Text(it) } },
                    visualTransformation = DecimalInputVisualTransformation(DecimalFormatter())
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1.4f)) {
                        OutlinedTextField(
                            value = state.dateTime.toDisplayDate(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(Res.string.field_date)) },
                            leadingIcon = { Icon(Icons.Rounded.CalendarMonth, null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(Modifier.matchParentSize().clickable { showDatePicker = true })
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = state.dateTime.toDisplayTime(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(Res.string.field_time)) },
                            leadingIcon = { Icon(Icons.Rounded.Schedule, null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(Modifier.matchParentSize().clickable { showTimePicker = true })
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = state.selectedCategory?.let { "${it.icon} ${it.name}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(Res.string.field_category)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(showCategoryDropdown)
                        },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        isError = state.categoryError != null
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }) {
                        state.categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.icon} ${cat.name}") },
                                onClick = {
                                    onAction(AddEditExpenseAction.OnCategorySelect(cat))
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = state.selectedCategory != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        if (state.subCategories.isEmpty()) {
                            OutlinedTextField(
                                value = stringResource(Res.string.common_none),
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text(stringResource(Res.string.field_subcategory)) },
                                supportingText = { Text(stringResource(Res.string.subcategory_none_hint)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            ExposedDropdownMenuBox(
                                expanded = showSubCategoryDropdown,
                                onExpandedChange = { showSubCategoryDropdown = it }
                            ) {
                                OutlinedTextField(
                                    value = state.selectedSubCategory?.name
                                        ?: stringResource(Res.string.common_none),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(stringResource(Res.string.field_subcategory)) },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(showSubCategoryDropdown)
                                    },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = showSubCategoryDropdown,
                                    onDismissRequest = { showSubCategoryDropdown = false }) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.common_none)) },
                                        onClick = {
                                            onAction(AddEditExpenseAction.OnSubCategorySelect(null))
                                            showSubCategoryDropdown = false
                                        })
                                    state.subCategories.forEach { sub ->
                                        DropdownMenuItem(text = { Text(sub.name) }, onClick = {
                                            onAction(AddEditExpenseAction.OnSubCategorySelect(sub))
                                            showSubCategoryDropdown = false
                                        })
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { onAction(AddEditExpenseAction.OnNotesChange(it.take(300))) },
                    label = { Text(stringResource(Res.string.field_notes)) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = { onAction(AddEditExpenseAction.OnSave) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = MaterialTheme.shapes.large,
                enabled = state.isValid && !state.isSaving
            ) {
                if (state.isSaving) CircularProgressIndicator(
                    Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                else Text(
                    stringResource(if (isEdit) Res.string.update_expense else Res.string.save_expense),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
