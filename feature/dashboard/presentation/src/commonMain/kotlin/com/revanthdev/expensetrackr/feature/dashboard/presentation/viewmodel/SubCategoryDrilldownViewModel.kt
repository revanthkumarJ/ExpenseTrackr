package com.revanthdev.expensetrackr.feature.dashboard.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubCategoryDrilldownViewModel(
    savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    private val categoryId: Long = savedStateHandle["categoryId"] ?: 0L
    private val categoryName: String = savedStateHandle["categoryName"] ?: ""

    private val _state = MutableStateFlow(SubCategoryDrilldownState(categoryName = categoryName))
    val state = _state.asStateFlow()
    private val _events = Channel<SubCategoryDrilldownEvent>()
    val events = _events.receiveAsFlow()

    init { loadData(DateFilter.ThisMonth) }

    fun onAction(action: SubCategoryDrilldownAction) {
        when (action) {
            is SubCategoryDrilldownAction.OnFilterChange -> loadData(action.filter)
            is SubCategoryDrilldownAction.OnItemClick -> viewModelScope.launch {
                val item = _state.value.items.find { it.subCategoryId == action.subCategoryId }
                val title = item?.name ?: "Uncategorized"
                _events.send(SubCategoryDrilldownEvent.NavigateToFilteredExpenses(
                    categoryId = categoryId,
                    subCategoryId = action.subCategoryId ?: -1L,
                    title = "${_state.value.categoryName} › $title"
                ))
            }
            SubCategoryDrilldownAction.OnAddExpenseClick -> viewModelScope.launch {
                _events.send(SubCategoryDrilldownEvent.NavigateToAddExpense)
            }
            SubCategoryDrilldownAction.OnBack -> viewModelScope.launch {
                _events.send(SubCategoryDrilldownEvent.NavigateBack)
            }
        }
    }

    private fun loadData(filter: DateFilter) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, filter = filter) }
            combine(
                categoryRepository.getSubCategoriesForCategory(categoryId),
                expenseRepository.getExpensesForCategory(categoryId, filter)
            ) { subCats, expenses ->
                val totalSpend = expenses.sumOf { it.amount }
                val grouped = expenses.groupBy { it.subCategory?.id }
                val items = mutableListOf<SubCategoryItem>()

                subCats.forEach { sub ->
                    val subTotal = grouped[sub.id]?.sumOf { it.amount } ?: 0.0
                    items.add(SubCategoryItem(
                        subCategoryId = sub.id,
                        name = sub.name,
                        total = subTotal,
                        percentage = if (totalSpend > 0) (subTotal / totalSpend) * 100 else 0.0
                    ))
                }

                val uncatTotal = grouped[null]?.sumOf { it.amount } ?: 0.0
                items.add(SubCategoryItem(
                    subCategoryId = null,
                    name = "Uncategorized",
                    total = uncatTotal,
                    percentage = if (totalSpend > 0) (uncatTotal / totalSpend) * 100 else 0.0
                ))

                _state.value.copy(
                    totalSpend = totalSpend,
                    items = items.sortedByDescending { it.total },
                    filter = filter,
                    isLoading = false
                )
            }.collect { _state.value = it }
        }
    }
}
