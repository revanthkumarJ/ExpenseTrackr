package com.revanthdev.expensetrackr.feature.expenses.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.SubCategory
import com.revanthdev.expensetrackr.core.domain.model.TransactionType
import com.revanthdev.expensetrackr.core.presentation.util.DecimalFormatter
import com.revanthdev.expensetrackr.core.presentation.util.DecimalInputVisualTransformation
import com.revanthdev.expensetrackr.core.presentation.util.toDisplayDate
import com.revanthdev.expensetrackr.core.presentation.util.toDisplayTime
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_back
import expensetrackr.core.presentation.generated.resources.action_delete
import expensetrackr.core.presentation.generated.resources.add_new_category
import expensetrackr.core.presentation.generated.resources.add_new_subcategory
import expensetrackr.core.presentation.generated.resources.add_transaction_title
import expensetrackr.core.presentation.generated.resources.common_none
import expensetrackr.core.presentation.generated.resources.edit_transaction_title
import expensetrackr.core.presentation.generated.resources.field_amount
import expensetrackr.core.presentation.generated.resources.field_category
import expensetrackr.core.presentation.generated.resources.field_date
import expensetrackr.core.presentation.generated.resources.field_expense_name
import expensetrackr.core.presentation.generated.resources.field_income_name
import expensetrackr.core.presentation.generated.resources.field_notes
import expensetrackr.core.presentation.generated.resources.field_subcategory
import expensetrackr.core.presentation.generated.resources.field_time
import expensetrackr.core.presentation.generated.resources.save_transaction
import expensetrackr.core.presentation.generated.resources.select_category
import expensetrackr.core.presentation.generated.resources.select_subcategory
import expensetrackr.core.presentation.generated.resources.type_expense
import expensetrackr.core.presentation.generated.resources.type_income
import expensetrackr.core.presentation.generated.resources.update_transaction
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(state: AddEditExpenseState, onAction: (AddEditExpenseAction) -> Unit) {
    val isEdit = state.expenseId != -1L
    var showCategorySheet by remember { mutableStateOf(false) }
    var showSubCategorySheet by remember { mutableStateOf(false) }
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

    if (showCategorySheet) {
        CategoryPickerSheet(
            categories = state.categories,
            selected = state.selectedCategory,
            onAddCategory = {
                showCategorySheet = false
                onAction(AddEditExpenseAction.OnAddCategoryClick)
            },
            onSelect = {
                onAction(AddEditExpenseAction.OnCategorySelect(it))
                showCategorySheet = false
            },
            onDismiss = { showCategorySheet = false }
        )
    }

    if (showSubCategorySheet) {
        SubCategoryPickerSheet(
            subCategories = state.subCategories,
            selected = state.selectedSubCategory,
            onAddSubCategory = {
                showSubCategorySheet = false
                onAction(AddEditExpenseAction.OnAddSubCategoryClick)
            },
            onSelect = {
                onAction(AddEditExpenseAction.OnSubCategorySelect(it))
                showSubCategorySheet = false
            },
            onDismiss = { showSubCategorySheet = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (isEdit) Res.string.edit_transaction_title else Res.string.add_transaction_title)) },
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
                val typeOptions = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    typeOptions.forEachIndexed { index, type ->
                        SegmentedButton(
                            selected = state.type == type,
                            onClick = { onAction(AddEditExpenseAction.OnTypeChange(type)) },
                            shape = SegmentedButtonDefaults.itemShape(index, typeOptions.size)
                        ) {
                            Text(stringResource(if (type == TransactionType.INCOME) Res.string.type_income else Res.string.type_expense))
                        }
                    }
                }

                OutlinedTextField(
                    value = state.name,
                    onValueChange = { onAction(AddEditExpenseAction.OnNameChange(it.take(100))) },
                    label = {
                        Text(
                            stringResource(
                                if (state.type == TransactionType.INCOME) Res.string.field_income_name
                                else Res.string.field_expense_name
                            )
                        )
                    },
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

                // Category — opens a bottom sheet (first item lets the user create a category).
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.selectedCategory?.let { "${it.icon} ${it.name}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(Res.string.field_category)) },
                        trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, null) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.categoryError != null
                    )
                    Box(Modifier.matchParentSize().clickable { showCategorySheet = true })
                }

                // Sub-category — only relevant once a category is chosen; also opens a bottom sheet.
                AnimatedVisibility(
                    visible = state.selectedCategory != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = state.selectedSubCategory?.name
                                ?: stringResource(Res.string.common_none),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(Res.string.field_subcategory)) },
                            trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(Modifier.matchParentSize().clickable { showSubCategorySheet = true })
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
                    stringResource(if (isEdit) Res.string.update_transaction else Res.string.save_transaction),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPickerSheet(
    categories: List<Category>,
    selected: Category?,
    onAddCategory: () -> Unit,
    onSelect: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        SheetTitle(stringResource(Res.string.select_category))
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { SheetAddCard(stringResource(Res.string.add_new_category), onAddCategory) }
            items(categories) { cat ->
                SheetOptionCard(
                    text = "${cat.icon} ${cat.name}",
                    selected = cat.id == selected?.id,
                    onClick = { onSelect(cat) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubCategoryPickerSheet(
    subCategories: List<SubCategory>,
    selected: SubCategory?,
    onAddSubCategory: () -> Unit,
    onSelect: (SubCategory?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        SheetTitle(stringResource(Res.string.select_subcategory))
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { SheetAddCard(stringResource(Res.string.add_new_subcategory), onAddSubCategory) }
            item {
                SheetOptionCard(
                    text = stringResource(Res.string.common_none),
                    selected = selected == null,
                    onClick = { onSelect(null) }
                )
            }
            items(subCategories) { sub ->
                SheetOptionCard(
                    text = sub.name,
                    selected = sub.id == selected?.id,
                    onClick = { onSelect(sub) }
                )
            }
        }
    }
}

@Composable
private fun SheetTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp)
    )
}

/** "Add …" entry, styled as a card that matches the option cards, with a leading + tile. */
@Composable
private fun SheetAddCard(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** A selectable option card; the selected one gets a primary outline + check badge. */
@Composable
private fun SheetOptionCard(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow,
        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
