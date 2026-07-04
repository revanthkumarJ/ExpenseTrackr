package com.revanthdev.expensetrackr.feature.categories.presentation

import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.SubCategory

data class ManageSubCategoriesState(
    val subCategories: List<SubCategory> = emptyList(),
    val categories: List<Category> = emptyList(),
    val showAddDialog: Boolean = false,
    val showEditDialog: SubCategory? = null,
    val newName: String = "",
    val newCategoryId: Long = 0L,
    val showDetachDialog: SubCategory? = null,
    val deleteError: String? = null
)

sealed interface ManageSubCategoriesAction {
    data object OnAddClick : ManageSubCategoriesAction
    data class OnEditClick(val subCategory: SubCategory) : ManageSubCategoriesAction
    data class OnDeleteClick(val subCategory: SubCategory) : ManageSubCategoriesAction
    data class OnNameChange(val name: String) : ManageSubCategoriesAction
    data class OnCategoryChange(val categoryId: Long) : ManageSubCategoriesAction
    data object OnSaveNew : ManageSubCategoriesAction
    data object OnSaveEdit : ManageSubCategoriesAction
    data object OnDismissDialog : ManageSubCategoriesAction
    data object OnDetachConfirm : ManageSubCategoriesAction
    data object OnDetachDismiss : ManageSubCategoriesAction
    data object OnBack : ManageSubCategoriesAction
}

sealed interface ManageSubCategoriesEvent {
    data object NavigateBack : ManageSubCategoriesEvent
}
