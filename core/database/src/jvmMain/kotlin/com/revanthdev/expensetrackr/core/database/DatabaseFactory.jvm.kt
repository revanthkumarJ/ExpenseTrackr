package com.revanthdev.expensetrackr.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<ExpenseTrackerDatabase> {
    val dbFile = File(System.getProperty("user.home"), ".expensetrackr/expense_tracker.db")
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<ExpenseTrackerDatabase>(name = dbFile.absolutePath)
}
