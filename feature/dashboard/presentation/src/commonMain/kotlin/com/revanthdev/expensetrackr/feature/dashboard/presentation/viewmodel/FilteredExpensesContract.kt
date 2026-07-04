package com.revanthdev.expensetrackr.feature.dashboard.presentation

import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.model.ExpenseWithDetails

data class FilteredExpensesState(
    val title: String = "",
    val filter: DateFilter = DateFilter.ThisMonth,
    val grouped: Map<String, List<ExpenseWithDetails>> = emptyMap(),
    val isLoading: Boolean = true
)

sealed interface FilteredExpensesAction {
    data class OnFilterChange(val filter: DateFilter) : FilteredExpensesAction
    data class OnExpenseClick(val id: Long) : FilteredExpensesAction
    data object OnBack : FilteredExpensesAction
}

sealed interface FilteredExpensesEvent {
    data class NavigateToEdit(val id: Long) : FilteredExpensesEvent
    data object NavigateBack : FilteredExpensesEvent
}
