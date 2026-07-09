package com.revanthdev.expensetrackr.core.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * v1 → v2: introduce transaction/category `type` ("EXPENSE" | "INCOME"). Existing rows are all
 * expenses, so both new columns default to 'EXPENSE'. A partial index on the expense type keeps
 * the type-filtered spend/income queries fast.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE expenses ADD COLUMN type TEXT NOT NULL DEFAULT 'EXPENSE'")
        connection.execSQL("ALTER TABLE categories ADD COLUMN type TEXT NOT NULL DEFAULT 'EXPENSE'")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_type ON expenses(type)")
    }
}

val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)
