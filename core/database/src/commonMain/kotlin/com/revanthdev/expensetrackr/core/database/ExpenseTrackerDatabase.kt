package com.revanthdev.expensetrackr.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
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
abstract class ExpenseTrackerDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun subCategoryDao(): SubCategoryDao
    abstract fun expenseDao(): ExpenseDao
}
