package com.revanthdev.expensetrackr.feature.expenses.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.model.ExpenseWithDetails
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.presentation.UiText
import com.revanthdev.expensetrackr.core.presentation.util.toDisplayDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExpensesViewModel(private val expenseRepository: ExpenseRepository) : ViewModel() {
    private val _state = MutableStateFlow(ExpensesState())
    val state = _state.asStateFlow()
    private val _events = Channel<ExpensesEvent>()
    val events = _events.receiveAsFlow()

    private var allExpenses: List<ExpenseWithDetails> = emptyList()

    init { loadExpenses(DateFilter.ThisMonth) }

    fun onAction(action: ExpensesAction) {
        when (action) {
            is ExpensesAction.OnFilterChange -> loadExpenses(action.filter)
            is ExpensesAction.OnSearchChange -> {
                _state.update { it.copy(searchQuery = action.query) }
                applySearch(action.query)
            }
            is ExpensesAction.OnExpenseClick -> viewModelScope.launch {
                _events.send(ExpensesEvent.NavigateToEdit(action.id))
            }
            is ExpensesAction.OnDeleteExpense -> deleteExpense(action.id)
            ExpensesAction.OnAddExpenseClick -> viewModelScope.launch {
                _events.send(ExpensesEvent.NavigateToAddExpense)
            }
            ExpensesAction.OnSnackbarDismiss -> _state.update { it.copy(snackbarMessage = null) }
        }
    }

    private fun loadExpenses(filter: DateFilter) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, filter = filter) }
            expenseRepository.getExpensesWithDetails(filter).collect { expenses ->
                allExpenses = expenses
                val query = _state.value.searchQuery
                val filtered = if (query.isBlank()) expenses else filterExpenses(expenses, query)
                _state.update { it.copy(grouped = groupByDate(filtered), isLoading = false) }
            }
        }
    }

    private fun applySearch(query: String) {
        val filtered = if (query.isBlank()) allExpenses else filterExpenses(allExpenses, query)
        _state.update { it.copy(grouped = groupByDate(filtered)) }
    }

    private fun filterExpenses(expenses: List<ExpenseWithDetails>, query: String): List<ExpenseWithDetails> {
        val q = query.lowercase()
        return expenses.filter {
            it.name.lowercase().contains(q) ||
            it.category.name.lowercase().contains(q) ||
            it.amount.toString().contains(q)
        }
    }

    private fun groupByDate(expenses: List<ExpenseWithDetails>): Map<String, List<ExpenseWithDetails>> =
        expenses.groupBy { it.expenseDate.toDisplayDate() }

    private fun deleteExpense(id: Long) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(id)
            _events.send(ExpensesEvent.ShowSnackbar(UiText.DynamicString("Expense deleted")))
        }
    }
}
