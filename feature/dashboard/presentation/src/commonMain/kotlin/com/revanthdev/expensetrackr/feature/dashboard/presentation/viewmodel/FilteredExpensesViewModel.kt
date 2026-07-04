package com.revanthdev.expensetrackr.feature.dashboard.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.presentation.util.toDisplayDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FilteredExpensesViewModel(
    savedStateHandle: SavedStateHandle,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    private val categoryId: Long = savedStateHandle["categoryId"] ?: 0L
    private val subCategoryId: Long = savedStateHandle["subCategoryId"] ?: -1L
    private val title: String = savedStateHandle["title"] ?: ""

    private val _state = MutableStateFlow(FilteredExpensesState(title = title))
    val state = _state.asStateFlow()
    private val _events = Channel<FilteredExpensesEvent>()
    val events = _events.receiveAsFlow()

    init { loadData(DateFilter.ThisMonth) }

    fun onAction(action: FilteredExpensesAction) {
        when (action) {
            is FilteredExpensesAction.OnFilterChange -> loadData(action.filter)
            is FilteredExpensesAction.OnExpenseClick -> viewModelScope.launch {
                _events.send(FilteredExpensesEvent.NavigateToEdit(action.id))
            }
            FilteredExpensesAction.OnBack -> viewModelScope.launch {
                _events.send(FilteredExpensesEvent.NavigateBack)
            }
        }
    }

    private fun loadData(filter: DateFilter) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, filter = filter) }
            val actualSubCatId = if (subCategoryId == -1L) null else subCategoryId
            expenseRepository.getExpensesForCategoryAndSubCategory(categoryId, actualSubCatId, filter).collect { expenses ->
                _state.update { state ->
                    state.copy(
                        grouped = expenses.groupBy { it.expenseDate.toDisplayDate() },
                        isLoading = false
                    )
                }
            }
        }
    }
}
