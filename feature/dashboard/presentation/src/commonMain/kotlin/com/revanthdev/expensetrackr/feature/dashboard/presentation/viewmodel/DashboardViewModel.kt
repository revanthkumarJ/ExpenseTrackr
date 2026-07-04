package com.revanthdev.expensetrackr.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.model.SalaryCalculator
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import com.revanthdev.expensetrackr.core.presentation.util.toMonthYear
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DashboardViewModel(
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()
    private val _events = Channel<DashboardEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadData(DateFilter.ThisMonth)
    }

    fun onAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.OnFilterChange -> loadData(action.filter)
            is DashboardAction.OnCategoryClick -> viewModelScope.launch {
                _events.send(DashboardEvent.NavigateToSubCategory(action.categoryId, action.categoryName))
            }
            DashboardAction.OnAddExpenseClick -> viewModelScope.launch {
                _events.send(DashboardEvent.NavigateToAddExpense)
            }
        }
    }

    private fun loadData(filter: DateFilter) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, filter = filter) }
            combine(
                categoryRepository.getAllCategories(),
                expenseRepository.getSpendByCategory(filter),
                expenseRepository.getTotalSpend(filter),
                settingsRepository.getSettings()
            ) { categories, spendMap, total, settings ->
                val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                // Budgets are monthly, so they only make sense while viewing the current month.
                // For any other period (this week, last month, this year, custom) we hide them.
                val isThisMonth = filter == DateFilter.ThisMonth
                val periodLabel = when (filter) {
                    DateFilter.ThisMonth -> now.toMonthYear()
                    DateFilter.ThisWeek -> "This Week"
                    DateFilter.LastMonth -> "Last Month"
                    DateFilter.ThisYear -> now.year.toString()
                    is DateFilter.CustomRange -> "Custom Range"
                }
                val overallBudget = if (isThisMonth) settings.overallMonthlyBudget else null
                val monthlySalary = if (isThisMonth) {
                    SalaryCalculator.salaryForMonth(
                        settings.salaryHistory,
                        SalaryCalculator.monthIndexOf(now.year, now.monthNumber)
                    ).takeIf { it > 0.0 }
                } else null
                val categoryUis = categories.map { cat ->
                    val catTotal = spendMap[cat.id] ?: 0.0
                    val percent = if (total > 0) (catTotal / total) * 100 else 0.0
                    val budgetProgress = if (isThisMonth) {
                        cat.budgetAmount?.let { budget ->
                            if (budget > 0) (catTotal / budget).toFloat() else null
                        }
                    } else null
                    CategoryUi(cat, catTotal, percent, budgetProgress)
                }.filter { it.total > 0 }.sortedByDescending { it.total }

                DashboardState(
                    monthLabel = periodLabel,
                    totalSpend = total,
                    overallBudget = overallBudget,
                    overallProgress = overallBudget?.let { if (it > 0) (total / it).toFloat() else null },
                    monthlySalary = monthlySalary,
                    categories = categoryUis,
                    filter = filter,
                    isLoading = false
                )
            }.collect { newState -> _state.value = newState }
        }
    }
}
