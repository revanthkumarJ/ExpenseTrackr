package com.revanthdev.expensetrackr.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.revanthdev.expensetrackr.core.database.dao.CategoryDao
import com.revanthdev.expensetrackr.core.database.dao.ExpenseDao
import com.revanthdev.expensetrackr.core.database.dao.SubCategoryDao
import com.revanthdev.expensetrackr.core.database.entity.CategoryEntity
import com.revanthdev.expensetrackr.core.database.entity.ExpenseEntity
import com.revanthdev.expensetrackr.core.database.entity.SubCategoryEntity

@Database(
    entities = [CategoryEntity::class, SubCategoryEntity::class, ExpenseEntity::class],
    version = 1
)
@ConstructedBy(ExpenseTrackerDatabaseConstructor::class)
abstract class ExpenseTrackerDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun subCategoryDao(): SubCategoryDao
    abstract fun expenseDao(): ExpenseDao
}

// Room's KSP generates the actual object for each platform. Required so Kotlin/Native (iOS)
// can instantiate the database without the JVM reflection that Android/desktop fall back to.
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpectDeclaration")
expect object ExpenseTrackerDatabaseConstructor : RoomDatabaseConstructor<ExpenseTrackerDatabase> {
    override fun initialize(): ExpenseTrackerDatabase
}
