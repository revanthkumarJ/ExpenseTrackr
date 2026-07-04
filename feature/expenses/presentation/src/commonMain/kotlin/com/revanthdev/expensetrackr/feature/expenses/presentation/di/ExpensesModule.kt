package com.revanthdev.expensetrackr.feature.expenses.presentation

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val expensesModule = module {
    viewModelOf(::ExpensesViewModel)
}

val addEditExpenseModule = module {
    viewModelOf(::AddEditExpenseViewModel)
}
