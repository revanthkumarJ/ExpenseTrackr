package com.revanthdev.expensetrackr.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<ExpenseTrackerDatabase> {
    val appContext = context as Context
    val dbFile = appContext.getDatabasePath("expense_tracker.db")
    return Room.databaseBuilder<ExpenseTrackerDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
