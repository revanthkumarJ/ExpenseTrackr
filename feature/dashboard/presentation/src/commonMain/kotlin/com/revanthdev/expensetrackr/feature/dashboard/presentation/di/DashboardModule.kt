package com.revanthdev.expensetrackr.feature.dashboard.presentation

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dashboardModule = module {
    viewModelOf(::DashboardViewModel)
    viewModelOf(::SubCategoryDrilldownViewModel)
    viewModelOf(::FilteredExpensesViewModel)
}
