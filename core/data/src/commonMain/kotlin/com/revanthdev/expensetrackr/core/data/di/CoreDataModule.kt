package com.revanthdev.expensetrackr.core.data.di

import com.revanthdev.expensetrackr.core.data.datasource.createSettingsDataStore
import com.revanthdev.expensetrackr.core.data.repository.DataStoreSettingsRepository
import com.revanthdev.expensetrackr.core.data.repository.RoomCategoryRepository
import com.revanthdev.expensetrackr.core.data.repository.RoomExpenseRepository
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

var dataStoreContext: Any? = null

val coreDataModule = module {
    single { createSettingsDataStore(dataStoreContext) }
    singleOf(::DataStoreSettingsRepository) { bind<SettingsRepository>() }
    singleOf(::RoomCategoryRepository) { bind<CategoryRepository>() }
    singleOf(::RoomExpenseRepository) { bind<ExpenseRepository>() }
}
