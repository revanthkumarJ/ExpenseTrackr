package com.revanthdev.expensetrackr.core.database.di

import com.revanthdev.expensetrackr.core.database.DefaultCategorySeeder
import com.revanthdev.expensetrackr.core.database.ExpenseTrackerDatabase
import com.revanthdev.expensetrackr.core.database.createDatabase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

var databaseContext: Any? = null

val coreDatabaseModule = module {
    single<ExpenseTrackerDatabase> { createDatabase(databaseContext) }
    single { get<ExpenseTrackerDatabase>().categoryDao() }
    single { get<ExpenseTrackerDatabase>().subCategoryDao() }
    single { get<ExpenseTrackerDatabase>().expenseDao() }
    singleOf(::DefaultCategorySeeder)
}
