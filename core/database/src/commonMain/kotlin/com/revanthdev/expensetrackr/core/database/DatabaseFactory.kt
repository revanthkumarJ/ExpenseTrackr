package com.revanthdev.expensetrackr.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

expect fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<ExpenseTrackerDatabase>

fun createDatabase(context: Any? = null): ExpenseTrackerDatabase =
    getDatabaseBuilder(context)
        .setDriver(BundledSQLiteDriver())
        .build()
