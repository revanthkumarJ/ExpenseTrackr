package com.revanthdev.expensetrackr.feature.dashboard.presentation

import com.revanthdev.expensetrackr.core.domain.model.DateFilter

data class SubCategoryItem(
    val subCategoryId: Long?,
    val name: String,
    val total: Double,
    val percentage: Double
)

data class SubCategoryDrilldownState(
    val categoryName: String = "",
    val filter: DateFilter = DateFilter.ThisMonth,
    val totalSpend: Double = 0.0,
    val items: List<SubCategoryItem> = emptyList(),
    val isLoading: Boolean = true
)

sealed interface SubCategoryDrilldownAction {
    data class OnFilterChange(val filter: DateFilter) : SubCategoryDrilldownAction
    data class OnItemClick(val subCategoryId: Long?) : SubCategoryDrilldownAction
    data object OnAddExpenseClick : SubCategoryDrilldownAction
    data object OnBack : SubCategoryDrilldownAction
}

sealed interface SubCategoryDrilldownEvent {
    data class NavigateToFilteredExpenses(val categoryId: Long, val subCategoryId: Long, val title: String) : SubCategoryDrilldownEvent
    data object NavigateToAddExpense : SubCategoryDrilldownEvent
    data object NavigateBack : SubCategoryDrilldownEvent
}
