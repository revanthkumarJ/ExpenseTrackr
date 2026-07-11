package com.revanthdev.expensetrackr

import com.revanthdev.expensetrackr.core.data.di.coreDataModule
import com.revanthdev.expensetrackr.core.database.DefaultCategorySeeder
import com.revanthdev.expensetrackr.core.database.di.coreDatabaseModule
import com.revanthdev.expensetrackr.core.domain.repository.BackupFileStore
import com.revanthdev.expensetrackr.feature.analytics.presentation.analyticsModule
import com.revanthdev.expensetrackr.feature.applock.presentation.appLockModule
import com.revanthdev.expensetrackr.feature.budget.presentation.budgetModule
import com.revanthdev.expensetrackr.feature.categories.presentation.categoriesModule
import com.revanthdev.expensetrackr.feature.dashboard.presentation.dashboardModule
import com.revanthdev.expensetrackr.feature.expenses.presentation.addEditExpenseModule
import com.revanthdev.expensetrackr.feature.expenses.presentation.expensesModule
import com.revanthdev.expensetrackr.feature.onboarding.presentation.onboardingModule
import com.revanthdev.expensetrackr.feature.settings.presentation.settingsModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Starts Koin for the iOS app (parity with Android's `ExpenseTrackerApp.onCreate`). Must be called
 * once, before any Compose UI is created, so `koinViewModel()` can resolve. Called from Swift as
 * `KoinIosKt.startKoinIos()`.
 *
 * The iOS database & DataStore actuals ignore the context argument (they use NSDocumentDirectory),
 * so no context needs to be set here.
 */
fun startKoinIos() {
    val koinApp = startKoin {
        modules(
            module { single<BackupFileStore> { IosBackupFileStore() } },
            coreDatabaseModule,
            coreDataModule,
            onboardingModule,
            appLockModule,
            dashboardModule,
            expensesModule,
            addEditExpenseModule,
            analyticsModule,
            budgetModule,
            categoriesModule,
            settingsModule,
        )
    }

    // Seed the default categories on first launch (parity with Android).
    CoroutineScope(Dispatchers.Default).launch {
        koinApp.koin.get<DefaultCategorySeeder>().seedIfEmpty()
    }
}
