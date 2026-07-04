package com.revanthdev.expensetrackr.feature.categories.presentation

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val categoriesModule = module {
    viewModelOf(::ManageCategoriesViewModel)
    viewModelOf(::ManageSubCategoriesViewModel)
}
