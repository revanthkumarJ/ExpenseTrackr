package com.revanthdev.expensetrackr.feature.analytics.presentation

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val analyticsModule = module {
    viewModelOf(::AnalyticsViewModel)
}
