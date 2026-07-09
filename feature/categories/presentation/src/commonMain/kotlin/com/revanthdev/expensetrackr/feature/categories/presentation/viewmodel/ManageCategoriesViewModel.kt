package com.revanthdev.expensetrackr.feature.categories.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.TransactionType
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ManageCategoriesViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {
    private val _state = MutableStateFlow(ManageCategoriesState())
    val state = _state.asStateFlow()
    private val _events = Channel<ManageCategoriesEvent>()
    val events = _events.receiveAsFlow()

    private var allCategories: List<Category> = emptyList()

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { cats ->
                allCategories = cats
                _state.update { state -> state.copy(categories = cats.filter { it.type == state.selectedType }) }
            }
        }
    }

    fun onAction(action: ManageCategoriesAction) {
        when (action) {
            is ManageCategoriesAction.OnTypeTabChange -> _state.update { state ->
                state.copy(selectedType = action.type, categories = allCategories.filter { it.type == action.type })
            }
            ManageCategoriesAction.OnAddClick -> _state.update { it.copy(showAddDialog = true, newName = "", newIcon = "📦") }
            is ManageCategoriesAction.OnEditClick -> _state.update { it.copy(showEditDialog = action.category, newName = action.category.name, newIcon = action.category.icon) }
            is ManageCategoriesAction.OnDeleteClick -> deleteCategory(action.category)
            is ManageCategoriesAction.OnNameChange -> _state.update { it.copy(newName = action.name) }
            is ManageCategoriesAction.OnIconChange -> _state.update { it.copy(newIcon = action.icon) }
            ManageCategoriesAction.OnSaveNew -> saveNew()
            ManageCategoriesAction.OnSaveEdit -> saveEdit()
            ManageCategoriesAction.OnDismissDialog -> _state.update { it.copy(showAddDialog = false, showEditDialog = null, deleteError = null) }
            ManageCategoriesAction.OnBack -> viewModelScope.launch { _events.send(ManageCategoriesEvent.NavigateBack) }
        }
    }

    private fun saveNew() {
        val s = _state.value
        if (s.newName.isBlank()) return
        viewModelScope.launch {
            val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            categoryRepository.insertCategory(
                Category(
                    name = s.newName.trim(),
                    icon = s.newIcon,
                    colorHex = "#616161",
                    type = s.selectedType,
                    createdAt = now
                )
            )
            _state.update { it.copy(showAddDialog = false) }
        }
    }

    private fun saveEdit() {
        val s = _state.value
        val editing = s.showEditDialog ?: return
        if (s.newName.isBlank()) return
        viewModelScope.launch {
            categoryRepository.updateCategory(editing.copy(name = s.newName.trim(), icon = s.newIcon))
            _state.update { it.copy(showEditDialog = null) }
        }
    }

    private fun deleteCategory(category: Category) {
        viewModelScope.launch {
            if (categoryRepository.hasExpensesForCategory(category.id)) {
                _state.update { it.copy(deleteError = category.name) }
            } else {
                categoryRepository.deleteCategory(category.id)
            }
        }
    }
}
