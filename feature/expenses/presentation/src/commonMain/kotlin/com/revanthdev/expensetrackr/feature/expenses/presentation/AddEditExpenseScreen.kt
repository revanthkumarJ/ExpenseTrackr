package com.revanthdev.expensetrackr.feature.expenses.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.Expense
import com.revanthdev.expensetrackr.core.domain.model.SubCategory
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.domain.util.onFailure
import com.revanthdev.expensetrackr.core.domain.util.onSuccess
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.parameter.parametersOf

@Serializable
data class AddEditExpenseRoute(val expenseId: Long = -1L)

data class AddEditExpenseState(
    val expenseId: Long = -1L,
    val name: String = "",
    val amountText: String = "",
    val selectedCategory: Category? = null,
    val selectedSubCategory: SubCategory? = null,
    val notes: String = "",
    val categories: List<Category> = emptyList(),
    val subCategories: List<SubCategory> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val nameError: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null
)

val AddEditExpenseState.isValid: Boolean
    get() = name.isNotBlank() && amountText.toDoubleOrNull()?.let { it > 0 } == true && selectedCategory != null

sealed interface AddEditExpenseAction {
    data class OnNameChange(val name: String) : AddEditExpenseAction
    data class OnAmountChange(val amount: String) : AddEditExpenseAction
    data class OnCategorySelect(val category: Category) : AddEditExpenseAction
    data class OnSubCategorySelect(val subCategory: SubCategory?) : AddEditExpenseAction
    data class OnNotesChange(val notes: String) : AddEditExpenseAction
    data object OnSave : AddEditExpenseAction
    data object OnDeleteClick : AddEditExpenseAction
    data object OnDeleteConfirm : AddEditExpenseAction
    data object OnDeleteDismiss : AddEditExpenseAction
    data object OnBack : AddEditExpenseAction
}

sealed interface AddEditExpenseEvent {
    data object NavigateBack : AddEditExpenseEvent
}

class AddEditExpenseViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val expenseId: Long = savedStateHandle["expenseId"] ?: -1L
    private val _state = MutableStateFlow(AddEditExpenseState(expenseId = expenseId))
    val state = _state.asStateFlow()
    private val _events = Channel<AddEditExpenseEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { cats ->
                _state.update { it.copy(categories = cats) }
            }
        }
        if (expenseId != -1L) loadExpense(expenseId)
    }

    private fun loadExpense(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
//            expenseRepository.getExpenseById(id).onSuccess { expense ->
//                val cats = _state.value.categories
//                val cat = cats.find { it.id == expense.categoryId }
//                val subCats = if (cat != null) {
//                    categoryRepository.getSubCategoriesForCategory(cat.id).first()
//                } else emptyList()
//                val subCat = subCats.find { it.id == expense.subCategoryId }
//                _state.update { state ->
//                    state.copy(
//                        name = expense.name,
//                        amountText = expense.amount.toString(),
//                        selectedCategory = cat,
//                        selectedSubCategory = subCat,
//                        subCategories = subCats,
//                        notes = expense.notes ?: "",
//                        isLoading = false
//                    )
//                }
//            }.onFailure { _state.update { it.copy(isLoading = false) } }
        }
    }

    fun onAction(action: AddEditExpenseAction) {
        when (action) {
            is AddEditExpenseAction.OnNameChange -> {
                savedStateHandle["name"] = action.name
                _state.update { it.copy(name = action.name, nameError = null) }
            }
            is AddEditExpenseAction.OnAmountChange -> {
                savedStateHandle["amount"] = action.amount
                _state.update { it.copy(amountText = action.amount, amountError = null) }
            }
            is AddEditExpenseAction.OnCategorySelect -> {
                _state.update { it.copy(selectedCategory = action.category, selectedSubCategory = null) }
                viewModelScope.launch {
                    categoryRepository.getSubCategoriesForCategory(action.category.id).collect { subs ->
                        _state.update { it.copy(subCategories = subs) }
                    }
                }
            }
            is AddEditExpenseAction.OnSubCategorySelect -> {
                _state.update { it.copy(selectedSubCategory = action.subCategory) }
            }
            is AddEditExpenseAction.OnNotesChange -> _state.update { it.copy(notes = action.notes) }
            AddEditExpenseAction.OnSave -> saveExpense()
            AddEditExpenseAction.OnDeleteClick -> _state.update { it.copy(showDeleteDialog = true) }
            AddEditExpenseAction.OnDeleteConfirm -> deleteExpense()
            AddEditExpenseAction.OnDeleteDismiss -> _state.update { it.copy(showDeleteDialog = false) }
            AddEditExpenseAction.OnBack -> viewModelScope.launch { _events.send(AddEditExpenseEvent.NavigateBack) }
        }
    }

    private fun saveExpense() {
        val s = _state.value
        val amount = s.amountText.toDoubleOrNull()
        if (s.name.isBlank() || amount == null || amount <= 0 || s.selectedCategory == null) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val expense = Expense(
                id = if (expenseId == -1L) 0L else expenseId,
                name = s.name.trim(),
                amount = amount,
                categoryId = s.selectedCategory.id,
                subCategoryId = s.selectedSubCategory?.id,
                notes = s.notes.trim().ifBlank { null },
                expenseDate = now,
                createdAt = now
            )
            if (expenseId == -1L) expenseRepository.insertExpense(expense)
            else expenseRepository.updateExpense(expense)
            _events.send(AddEditExpenseEvent.NavigateBack)
        }
    }

    private fun deleteExpense() {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expenseId)
            _events.send(AddEditExpenseEvent.NavigateBack)
        }
    }
}

@Composable
fun AddEditExpenseRoot(
    onNavigateBack: () -> Unit,
    viewModel: AddEditExpenseViewModel = koinViewModel()
) {
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            AddEditExpenseEvent.NavigateBack -> onNavigateBack()
        }
    }
    val state by viewModel.state.collectAsState()
    AddEditExpenseScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(state: AddEditExpenseState, onAction: (AddEditExpenseAction) -> Unit) {
    val isEdit = state.expenseId != -1L
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showSubCategoryDropdown by remember { mutableStateOf(false) }

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { onAction(AddEditExpenseAction.OnDeleteDismiss) },
            title = { Text("Delete Expense?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { onAction(AddEditExpenseAction.OnDeleteConfirm) }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { onAction(AddEditExpenseAction.OnDeleteDismiss) }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Expense" else "Add Expense") },
                navigationIcon = {
                    IconButton(onClick = { onAction(AddEditExpenseAction.OnBack) }) {
                        Icon(Icons.Rounded.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (isEdit) {
                        IconButton(onClick = { onAction(AddEditExpenseAction.OnDeleteClick) }) {
                            Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = { onAction(AddEditExpenseAction.OnNameChange(it.take(100))) },
                label = { Text("Expense Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = state.amountText,
                onValueChange = { onAction(AddEditExpenseAction.OnAmountChange(it)) },
                label = { Text("Amount (₹) *") },
                prefix = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = state.amountError != null,
                supportingText = state.amountError?.let { { Text(it) } }
            )

            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = it }
            ) {
                OutlinedTextField(
                    value = state.selectedCategory?.let { "${it.icon} ${it.name}" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showCategoryDropdown) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    isError = state.categoryError != null
                )
                ExposedDropdownMenu(expanded = showCategoryDropdown, onDismissRequest = { showCategoryDropdown = false }) {
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

            if (state.subCategories.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = showSubCategoryDropdown,
                    onExpandedChange = { showSubCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = state.selectedSubCategory?.name ?: "None",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sub-Category (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showSubCategoryDropdown) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = showSubCategoryDropdown, onDismissRequest = { showSubCategoryDropdown = false }) {
                        DropdownMenuItem(text = { Text("None") }, onClick = {
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

            OutlinedTextField(
                value = state.notes,
                onValueChange = { onAction(AddEditExpenseAction.OnNotesChange(it.take(300))) },
                label = { Text("Notes (optional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onAction(AddEditExpenseAction.OnSave) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isValid && !state.isSaving
            ) {
                if (state.isSaving) CircularProgressIndicator(Modifier.size(20.dp))
                else Text(if (isEdit) "Update Expense" else "Save Expense")
            }
        }
    }
}

val addEditExpenseModule = org.koin.dsl.module {
    viewModelOf(::AddEditExpenseViewModel)
}
