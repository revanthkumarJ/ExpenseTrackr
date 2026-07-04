package com.revanthdev.expensetrackr.feature.budget.presentation

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val budgetModule = module {
    viewModelOf(::BudgetViewModel)
}
