package com.revanthdev.expensetrackr.feature.categories.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.domain.model.SubCategory
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ManageSubCategoriesViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {
    private val _state = MutableStateFlow(ManageSubCategoriesState())
    val state = _state.asStateFlow()
    private val _events = Channel<ManageSubCategoriesEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                categoryRepository.getAllSubCategories(),
                categoryRepository.getAllCategories()
            ) { subs, cats -> subs to cats }.collect { (subs, cats) ->
                _state.update { it.copy(subCategories = subs, categories = cats) }
            }
        }
    }

    fun onAction(action: ManageSubCategoriesAction) {
        when (action) {
            ManageSubCategoriesAction.OnAddClick -> {
                val firstCatId = _state.value.categories.firstOrNull()?.id ?: 0L
                _state.update { it.copy(showAddDialog = true, newName = "", newCategoryId = firstCatId) }
            }
            is ManageSubCategoriesAction.OnEditClick -> _state.update {
                it.copy(showEditDialog = action.subCategory, newName = action.subCategory.name, newCategoryId = action.subCategory.categoryId)
            }
            is ManageSubCategoriesAction.OnDeleteClick -> checkAndDelete(action.subCategory)
            is ManageSubCategoriesAction.OnNameChange -> _state.update { it.copy(newName = action.name) }
            is ManageSubCategoriesAction.OnCategoryChange -> _state.update { it.copy(newCategoryId = action.categoryId) }
            ManageSubCategoriesAction.OnSaveNew -> saveNew()
            ManageSubCategoriesAction.OnSaveEdit -> saveEdit()
            ManageSubCategoriesAction.OnDismissDialog -> _state.update { it.copy(showAddDialog = false, showEditDialog = null) }
            ManageSubCategoriesAction.OnDetachConfirm -> detachAndDelete()
            ManageSubCategoriesAction.OnDetachDismiss -> _state.update { it.copy(showDetachDialog = null) }
            ManageSubCategoriesAction.OnBack -> viewModelScope.launch { _events.send(ManageSubCategoriesEvent.NavigateBack) }
        }
    }

    private fun saveNew() {
        val s = _state.value
        if (s.newName.isBlank() || s.newCategoryId == 0L) return
        viewModelScope.launch {
            val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            categoryRepository.insertSubCategory(SubCategory(name = s.newName.trim(), categoryId = s.newCategoryId, createdAt = now))
            _state.update { it.copy(showAddDialog = false) }
        }
    }

    private fun saveEdit() {
        val s = _state.value
        val editing = s.showEditDialog ?: return
        if (s.newName.isBlank() || s.newCategoryId == 0L) return
        viewModelScope.launch {
            categoryRepository.updateSubCategory(editing.copy(name = s.newName.trim(), categoryId = s.newCategoryId))
            _state.update { it.copy(showEditDialog = null) }
        }
    }

    private fun checkAndDelete(subCategory: SubCategory) {
        viewModelScope.launch {
            if (categoryRepository.hasExpensesForSubCategory(subCategory.id)) {
                _state.update { it.copy(showDetachDialog = subCategory) }
            } else {
                categoryRepository.deleteSubCategory(subCategory.id)
            }
        }
    }

    private fun detachAndDelete() {
        val sub = _state.value.showDetachDialog ?: return
        viewModelScope.launch {
            categoryRepository.detachExpensesFromSubCategory(sub.id)
            categoryRepository.deleteSubCategory(sub.id)
            _state.update { it.copy(showDetachDialog = null) }
        }
    }
}
