package com.revanthdev.expensetrackr.core.domain.repository

import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.model.Expense
import com.revanthdev.expensetrackr.core.domain.model.ExpenseWithDetails
import com.revanthdev.expensetrackr.core.domain.util.DataError
import com.revanthdev.expensetrackr.core.domain.util.EmptyResult
import com.revanthdev.expensetrackr.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getExpensesWithDetails(filter: DateFilter): Flow<List<ExpenseWithDetails>>
    fun getExpensesForCategory(categoryId: Long, filter: DateFilter): Flow<List<ExpenseWithDetails>>
    fun getExpensesForCategoryAndSubCategory(
        categoryId: Long,
        subCategoryId: Long?,
        filter: DateFilter
    ): Flow<List<ExpenseWithDetails>>
    suspend fun getExpenseById(id: Long): Result<Expense, DataError.Local>
    suspend fun insertExpense(expense: Expense): Result<Long, DataError.Local>
    suspend fun updateExpense(expense: Expense): EmptyResult<DataError.Local>
    suspend fun deleteExpense(id: Long): EmptyResult<DataError.Local>
    fun getTotalSpend(filter: DateFilter): Flow<Double>
    fun getSpendByCategory(filter: DateFilter): Flow<Map<Long, Double>>
}
