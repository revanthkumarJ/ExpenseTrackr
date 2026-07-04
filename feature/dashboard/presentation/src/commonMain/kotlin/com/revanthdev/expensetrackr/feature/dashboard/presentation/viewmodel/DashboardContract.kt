package com.revanthdev.expensetrackr.feature.dashboard.presentation

import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.DateFilter

data class CategoryUi(
    val category: Category,
    val total: Double,
    val percentage: Double,
    val budgetProgress: Float?
)

data class DashboardState(
    val monthLabel: String = "",
    val totalSpend: Double = 0.0,
    val overallBudget: Double? = null,
    val overallProgress: Float? = null,
    // Salary for the current month (only populated when viewing ThisMonth). null = no salary set.
    val monthlySalary: Double? = null,
    val categories: List<CategoryUi> = emptyList(),
    val filter: DateFilter = DateFilter.ThisMonth,
    val isLoading: Boolean = true
)

sealed interface DashboardAction {
    data class OnFilterChange(val filter: DateFilter) : DashboardAction
    data class OnCategoryClick(val categoryId: Long, val categoryName: String) : DashboardAction
    data object OnAddExpenseClick : DashboardAction
}

sealed interface DashboardEvent {
    data class NavigateToSubCategory(val categoryId: Long, val categoryName: String) : DashboardEvent
    data object NavigateToAddExpense : DashboardEvent
}
