package com.revanthdev.expensetrackr.feature.categories.presentation

import com.revanthdev.expensetrackr.core.domain.model.Category

data class ManageCategoriesState(
    val categories: List<Category> = emptyList(),
    val showAddDialog: Boolean = false,
    val showEditDialog: Category? = null,
    val newName: String = "",
    val newIcon: String = "📦",
    val deleteError: String? = null
)

sealed interface ManageCategoriesAction {
    data object OnAddClick : ManageCategoriesAction
    data class OnEditClick(val category: Category) : ManageCategoriesAction
    data class OnDeleteClick(val category: Category) : ManageCategoriesAction
    data class OnNameChange(val name: String) : ManageCategoriesAction
    data class OnIconChange(val icon: String) : ManageCategoriesAction
    data object OnSaveNew : ManageCategoriesAction
    data object OnSaveEdit : ManageCategoriesAction
    data object OnDismissDialog : ManageCategoriesAction
    data object OnBack : ManageCategoriesAction
}

sealed interface ManageCategoriesEvent {
    data object NavigateBack : ManageCategoriesEvent
}
