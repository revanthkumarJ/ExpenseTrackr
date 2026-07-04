package com.revanthdev.expensetrackr.feature.expenses.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.domain.model.AppSettings
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.model.Expense
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import com.revanthdev.expensetrackr.core.domain.util.onFailure
import com.revanthdev.expensetrackr.core.domain.util.onSuccess
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddEditExpenseViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
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
            expenseRepository.getExpenseById(id).onSuccess { expense ->
                val cats = categoryRepository.getAllCategories().first()
                val cat = cats.find { it.id == expense.categoryId }
                val subCats = if (cat != null) {
                    categoryRepository.getSubCategoriesForCategory(cat.id).first()
                } else emptyList()
                val subCat = subCats.find { it.id == expense.subCategoryId }
                _state.update { state ->
                    state.copy(
                        name = expense.name,
                        amountText = expense.amount.toString(),
                        selectedCategory = cat,
                        selectedSubCategory = subCat,
                        subCategories = subCats,
                        notes = expense.notes ?: "",
                        dateTime = expense.expenseDate,
                        originalAmount = expense.amount,
                        originalCategoryId = expense.categoryId,
                        originalCreatedAt = expense.createdAt,
                        isLoading = false
                    )
                }
            }.onFailure { _state.update { it.copy(isLoading = false) } }
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
                _state.update {
                    it.copy(
                        selectedCategory = action.category,
                        selectedSubCategory = null
                    )
                }
                viewModelScope.launch {
                    categoryRepository.getSubCategoriesForCategory(action.category.id)
                        .collect { subs ->
                            _state.update { it.copy(subCategories = subs) }
                        }
                }
            }

            is AddEditExpenseAction.OnSubCategorySelect -> {
                _state.update { it.copy(selectedSubCategory = action.subCategory) }
            }

            is AddEditExpenseAction.OnNotesChange -> _state.update { it.copy(notes = action.notes) }
            is AddEditExpenseAction.OnDateTimeChange -> _state.update { it.copy(dateTime = action.dateTime) }
            AddEditExpenseAction.OnSave -> saveExpense()
            AddEditExpenseAction.OnDeleteClick -> _state.update { it.copy(showDeleteDialog = true) }
            AddEditExpenseAction.OnDeleteConfirm -> deleteExpense()
            AddEditExpenseAction.OnDeleteDismiss -> _state.update { it.copy(showDeleteDialog = false) }
            AddEditExpenseAction.OnBudgetWarningDismiss -> _state.update { it.copy(budgetWarning = null) }
            AddEditExpenseAction.OnBack -> viewModelScope.launch { _events.send(AddEditExpenseEvent.NavigateBack) }
        }
    }

    private fun saveExpense() {
        val s = _state.value
        val amount = s.amountText.toDoubleOrNull()
        val category = s.selectedCategory
        if (s.name.isBlank() || amount == null || amount <= 0 || category == null) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val settings = settingsRepository.getSettingsOnce()
            if (!settings.allowExceedBudget) {
                val warning = checkBudget(amount, category, settings)
                if (warning != null) {
                    _state.update { it.copy(isSaving = false, budgetWarning = warning) }
                    return@launch
                }
            }

            val now = nowDateTime()
            val expense = Expense(
                id = if (expenseId == -1L) 0L else expenseId,
                name = s.name.trim(),
                amount = amount,
                categoryId = category.id,
                subCategoryId = s.selectedSubCategory?.id,
                notes = s.notes.trim().ifBlank { null },
                expenseDate = s.dateTime,
                createdAt = if (expenseId == -1L) now else (s.originalCreatedAt ?: now)
            )
            if (expenseId == -1L) expenseRepository.insertExpense(expense)
            else expenseRepository.updateExpense(expense)
            _events.send(AddEditExpenseEvent.NavigateBack)
        }
    }

    /**
     * Returns a warning message if recording [amount] in [category] this month would push the
     * monthly total or the category total over its budget; null if it's fine. For edits, the
     * expense's existing amount is excluded so it isn't double-counted.
     */
    private suspend fun checkBudget(
        amount: Double,
        category: Category,
        settings: AppSettings
    ): BudgetWarning? {
        val s = _state.value
        val isEditing = expenseId != -1L
        val monthlyTotal = expenseRepository.getTotalSpend(DateFilter.ThisMonth).first()
        val catSpend = expenseRepository.getSpendByCategory(DateFilter.ThisMonth).first()

        val overall = settings.overallMonthlyBudget
        if (overall != null) {
            val oldAmount = if (isEditing) s.originalAmount else 0.0
            val projected = monthlyTotal - oldAmount + amount
            if (projected > overall) {
                return BudgetWarning.Monthly(
                    projected.toCurrencyString(),
                    overall.toCurrencyString()
                )
            }
        }

        val catBudget = category.budgetAmount
        if (catBudget != null) {
            val catOld =
                if (isEditing && s.originalCategoryId == category.id) s.originalAmount else 0.0
            val catProjected = (catSpend[category.id] ?: 0.0) - catOld + amount
            if (catProjected > catBudget) {
                return BudgetWarning.Category(
                    category.name,
                    catProjected.toCurrencyString(),
                    catBudget.toCurrencyString()
                )
            }
        }
        return null
    }

    private fun deleteExpense() {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expenseId)
            _events.send(AddEditExpenseEvent.NavigateBack)
        }
    }
}
