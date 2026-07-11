package com.revanthdev.expensetrackr.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.revanthdev.expensetrackr.core.database.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    // ---- Expense-only reads (type = 'EXPENSE'): power spend totals, budgets and analytics ----
    @Query("SELECT * FROM expenses WHERE type = 'EXPENSE' AND expenseDate BETWEEN :startMs AND :endMs ORDER BY expenseDate DESC")
    fun getExpensesByDateRange(startMs: Long, endMs: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE type = 'EXPENSE' AND categoryId = :categoryId AND expenseDate BETWEEN :startMs AND :endMs ORDER BY expenseDate DESC")
    fun getExpensesByCategory(categoryId: Long, startMs: Long, endMs: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE type = 'EXPENSE' AND categoryId = :categoryId AND subCategoryId = :subCategoryId AND expenseDate BETWEEN :startMs AND :endMs ORDER BY expenseDate DESC")
    fun getExpensesByCategoryAndSubCategory(
        categoryId: Long,
        subCategoryId: Long,
        startMs: Long,
        endMs: Long
    ): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE type = 'EXPENSE' AND categoryId = :categoryId AND subCategoryId IS NULL AND expenseDate BETWEEN :startMs AND :endMs ORDER BY expenseDate DESC")
    fun getExpensesByCategoryNoSubCategory(
        categoryId: Long,
        startMs: Long,
        endMs: Long
    ): Flow<List<ExpenseEntity>>

    // ---- All transactions (both types): powers the combined transactions list ----
    @Query("SELECT * FROM expenses WHERE expenseDate BETWEEN :startMs AND :endMs ORDER BY expenseDate DESC")
    fun getTransactionsByDateRange(startMs: Long, endMs: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    /** Every transaction (expense + income), all dates — used to export a full backup. */
    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC")
    suspend fun getAllTransactions(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Long)

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE type = 'EXPENSE' AND expenseDate BETWEEN :startMs AND :endMs")
    fun getTotalSpend(startMs: Long, endMs: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE type = 'INCOME' AND expenseDate BETWEEN :startMs AND :endMs")
    fun getTotalIncome(startMs: Long, endMs: Long): Flow<Double>

    @Query("SELECT categoryId, SUM(amount) as total FROM expenses WHERE type = 'EXPENSE' AND expenseDate BETWEEN :startMs AND :endMs GROUP BY categoryId")
    fun getSpendByCategory(startMs: Long, endMs: Long): Flow<List<CategorySpend>>
}

data class CategorySpend(val categoryId: Long, val total: Double)
