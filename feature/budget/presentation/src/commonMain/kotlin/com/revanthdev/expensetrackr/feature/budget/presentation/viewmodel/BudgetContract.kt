package com.revanthdev.expensetrackr.feature.budget.presentation

import com.revanthdev.expensetrackr.core.domain.model.Category

data class BudgetState(
    val overallBudgetEnabled: Boolean = false,
    val overallBudgetText: String = "",
    val allowExceedBudget: Boolean = true,
    val categories: List<Category> = emptyList(),
    val categoryBudgets: Map<Long, String> = emptyMap(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

sealed interface BudgetAction {
    data class OnOverallToggle(val enabled: Boolean) : BudgetAction
    data class OnOverallBudgetChange(val text: String) : BudgetAction
    data class OnAllowExceedToggle(val allow: Boolean) : BudgetAction
    data class OnCategoryBudgetChange(val categoryId: Long, val text: String) : BudgetAction
    data object OnSave : BudgetAction
    data object OnResetAll : BudgetAction
    data object OnBack : BudgetAction
}

sealed interface BudgetEvent {
    data object NavigateBack : BudgetEvent
}
