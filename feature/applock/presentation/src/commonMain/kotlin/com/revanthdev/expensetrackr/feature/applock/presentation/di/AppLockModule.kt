package com.revanthdev.expensetrackr.feature.applock.presentation

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appLockModule = module {
    viewModelOf(::AppLockViewModel)
}
