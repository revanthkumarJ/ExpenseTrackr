package com.revanthdev.expensetrackr.feature.expenses.presentation

import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.SubCategory
import com.revanthdev.expensetrackr.core.domain.model.TransactionType
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

internal fun nowDateTime(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

data class AddEditExpenseState(
    val expenseId: Long = -1L,
    val type: TransactionType = TransactionType.EXPENSE,
    val name: String = "",
    val amountText: String = "",
    val selectedCategory: Category? = null,
    val selectedSubCategory: SubCategory? = null,
    val notes: String = "",
    val dateTime: LocalDateTime = nowDateTime(),
    val categories: List<Category> = emptyList(),
    val subCategories: List<SubCategory> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val nameError: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null,
    val budgetWarning: BudgetWarning? = null,
    val originalAmount: Double = 0.0,
    val originalCategoryId: Long? = null,
    val originalCreatedAt: LocalDateTime? = null
)

/** A budget-overflow warning, carrying pre-formatted amounts so the UI can localize the message. */
sealed interface BudgetWarning {
    data class Monthly(val projected: String, val budget: String) : BudgetWarning
    data class Category(val categoryName: String, val projected: String, val budget: String) :
        BudgetWarning
}

val AddEditExpenseState.isValid: Boolean
    get() = name.isNotBlank() && amountText.toDoubleOrNull()
        ?.let { it > 0 } == true && selectedCategory != null

sealed interface AddEditExpenseAction {
    data class OnTypeChange(val type: TransactionType) : AddEditExpenseAction
    data class OnNameChange(val name: String) : AddEditExpenseAction
    data class OnAmountChange(val amount: String) : AddEditExpenseAction
    data class OnCategorySelect(val category: Category) : AddEditExpenseAction
    data class OnSubCategorySelect(val subCategory: SubCategory?) : AddEditExpenseAction
    data class OnNotesChange(val notes: String) : AddEditExpenseAction
    data class OnDateTimeChange(val dateTime: LocalDateTime) : AddEditExpenseAction
    data object OnSave : AddEditExpenseAction
    data object OnDeleteClick : AddEditExpenseAction
    data object OnDeleteConfirm : AddEditExpenseAction
    data object OnDeleteDismiss : AddEditExpenseAction
    data object OnBudgetWarningDismiss : AddEditExpenseAction
    data object OnBack : AddEditExpenseAction
}

sealed interface AddEditExpenseEvent {
    data object NavigateBack : AddEditExpenseEvent
}
