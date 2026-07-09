package com.revanthdev.expensetrackr.feature.expenses.presentation

import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.model.ExpenseWithDetails
import com.revanthdev.expensetrackr.core.domain.model.TransactionType
import com.revanthdev.expensetrackr.core.presentation.UiText

data class ExpensesState(
    val grouped: Map<String, List<ExpenseWithDetails>> = emptyMap(),
    val filter: DateFilter = DateFilter.ThisMonth,
    // null = show all transactions; otherwise restrict the list to EXPENSE or INCOME.
    val typeFilter: TransactionType? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val snackbarMessage: UiText? = null
)

sealed interface ExpensesAction {
    data class OnFilterChange(val filter: DateFilter) : ExpensesAction
    data class OnTypeFilterChange(val type: TransactionType?) : ExpensesAction
    data class OnSearchChange(val query: String) : ExpensesAction
    data class OnExpenseClick(val id: Long) : ExpensesAction
    data class OnDeleteExpense(val id: Long) : ExpensesAction
    data object OnAddExpenseClick : ExpensesAction
    data object OnSnackbarDismiss : ExpensesAction
}

sealed interface ExpensesEvent {
    data class NavigateToEdit(val id: Long) : ExpensesEvent
    data object NavigateToAddExpense : ExpensesEvent
    data class ShowSnackbar(val message: UiText) : ExpensesEvent
}
