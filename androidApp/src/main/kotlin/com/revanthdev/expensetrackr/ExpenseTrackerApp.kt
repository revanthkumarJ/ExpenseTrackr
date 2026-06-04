package com.revanthdev.expensetrackr

import android.app.Application
import com.revanthdev.expensetrackr.core.data.di.coreDataModule
import com.revanthdev.expensetrackr.core.data.di.dataStoreContext
import com.revanthdev.expensetrackr.core.database.DefaultCategorySeeder
import com.revanthdev.expensetrackr.core.database.di.coreDatabaseModule
import com.revanthdev.expensetrackr.core.database.di.databaseContext
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
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import kotlin.coroutines.EmptyCoroutineContext.get

class ExpenseTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        databaseContext = applicationContext
        dataStoreContext = applicationContext

        startKoin {
            androidLogger()
            androidContext(this@ExpenseTrackerApp)
            modules(
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
                settingsModule
            )
        }

        CoroutineScope(Dispatchers.IO).launch {
            get<DefaultCategorySeeder>().seedCategories()
        }
    }
}
