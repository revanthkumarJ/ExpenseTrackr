package com.revanthdev.expensetrackr.feature.budget.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import com.revanthdev.expensetrackr.core.presentation.util.toAmountString
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val settingsRepository: SettingsRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _state = MutableStateFlow(BudgetState())
    val state = _state.asStateFlow()
    private val _events = Channel<BudgetEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.getSettings(),
                categoryRepository.getAllCategories()
            ) { settings, cats ->
                BudgetState(
                    overallBudgetEnabled = settings.overallMonthlyBudget != null,
                    overallBudgetText = settings.overallMonthlyBudget?.let { it.toAmountString() } ?: "",
                    allowExceedBudget = settings.allowExceedBudget,
                    categories = cats,
                    categoryBudgets = cats.associate { cat ->
                        cat.id to (cat.budgetAmount?.let { it.toAmountString() } ?: "")
                    },
                    isLoading = false
                )
            }.collect { _state.value = it }
        }
    }

    fun onAction(action: BudgetAction) {
        when (action) {
            is BudgetAction.OnOverallToggle -> _state.update { it.copy(overallBudgetEnabled = action.enabled) }
            is BudgetAction.OnOverallBudgetChange -> _state.update { it.copy(overallBudgetText = action.text) }
            is BudgetAction.OnAllowExceedToggle -> _state.update { it.copy(allowExceedBudget = action.allow) }
            is BudgetAction.OnCategoryBudgetChange -> _state.update {
                it.copy(categoryBudgets = it.categoryBudgets + (action.categoryId to action.text))
            }
            BudgetAction.OnSave -> save()
            BudgetAction.OnResetAll -> resetAll()
            BudgetAction.OnBack -> viewModelScope.launch { _events.send(BudgetEvent.NavigateBack) }
        }
    }

    private fun save() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val s = _state.value
            val settings = settingsRepository.getSettingsOnce()
            val newBudget = if (s.overallBudgetEnabled) s.overallBudgetText.toDoubleOrNull() else null
            settingsRepository.updateSettings(
                settings.copy(overallMonthlyBudget = newBudget, allowExceedBudget = s.allowExceedBudget)
            )
            s.categories.forEach { cat ->
                val budgetText = s.categoryBudgets[cat.id] ?: ""
                val budgetAmount = if (budgetText.isBlank()) null else budgetText.toDoubleOrNull()
                categoryRepository.updateCategory(cat.copy(budgetAmount = budgetAmount))
            }
            _state.update { it.copy(isSaving = false) }
            _events.send(BudgetEvent.NavigateBack)
        }
    }

    private fun resetAll() {
        viewModelScope.launch {
            val settings = settingsRepository.getSettingsOnce()
            settingsRepository.updateSettings(settings.copy(overallMonthlyBudget = null))
            _state.value.categories.forEach { cat ->
                categoryRepository.updateCategory(cat.copy(budgetAmount = null))
            }
            _state.update { it.copy(overallBudgetEnabled = false, overallBudgetText = "", categoryBudgets = emptyMap()) }
        }
    }
}
