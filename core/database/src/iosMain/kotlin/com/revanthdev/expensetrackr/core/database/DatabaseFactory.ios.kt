package com.revanthdev.expensetrackr.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<ExpenseTrackerDatabase> {
    val docsDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )!!
    val dbPath = docsDirectory.path + "/expense_tracker.db"
    return Room.databaseBuilder<ExpenseTrackerDatabase>(name = dbPath)
}
