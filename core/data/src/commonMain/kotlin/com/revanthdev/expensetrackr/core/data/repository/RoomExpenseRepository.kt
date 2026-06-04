package com.revanthdev.expensetrackr.core.data.repository

import com.revanthdev.expensetrackr.core.data.datasource.toDateRange
import com.revanthdev.expensetrackr.core.data.mapper.toCategory
import com.revanthdev.expensetrackr.core.data.mapper.toExpense
import com.revanthdev.expensetrackr.core.data.mapper.toExpenseEntity
import com.revanthdev.expensetrackr.core.data.mapper.toSubCategory
import com.revanthdev.expensetrackr.core.database.dao.CategoryDao
import com.revanthdev.expensetrackr.core.database.dao.ExpenseDao
import com.revanthdev.expensetrackr.core.database.dao.SubCategoryDao
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.model.Expense
import com.revanthdev.expensetrackr.core.domain.model.ExpenseWithDetails
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.domain.util.DataError
import com.revanthdev.expensetrackr.core.domain.util.EmptyResult
import com.revanthdev.expensetrackr.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.toLocalDateTime

class RoomExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val subCategoryDao: SubCategoryDao
) : ExpenseRepository {

    override fun getExpensesWithDetails(filter: DateFilter): Flow<List<ExpenseWithDetails>> {
        val range = filter.toDateRange()
        val expensesFlow = expenseDao.getExpensesByDateRange(range.startMs, range.endMs)
        val categoriesFlow = categoryDao.getAllCategories()
        val subCategoriesFlow = subCategoryDao.getAllSubCategories()

        return combine(expensesFlow, categoriesFlow, subCategoriesFlow) { expenses, categories, subCategories ->
            val categoryMap = categories.associateBy { it.id }
            val subCategoryMap = subCategories.associateBy { it.id }
            expenses.mapNotNull { entity ->
                val category = categoryMap[entity.categoryId]?.toCategory() ?: return@mapNotNull null
                val subCategory = entity.subCategoryId?.let { subCategoryMap[it]?.toSubCategory() }
                ExpenseWithDetails(
                    id = entity.id,
                    name = entity.name,
                    amount = entity.amount,
                    category = category,
                    subCategory = subCategory,
                    notes = entity.notes,
                    expenseDate = kotlinx.datetime.Instant.fromEpochMilliseconds(entity.expenseDate)
                        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
                    createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(entity.createdAt)
                        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                )
            }
        }
    }

    override fun getExpensesForCategory(
        categoryId: Long,
        filter: DateFilter
    ): Flow<List<ExpenseWithDetails>> {
        val range = filter.toDateRange()
        val expensesFlow = expenseDao.getExpensesByCategory(categoryId, range.startMs, range.endMs)
        val categoriesFlow = categoryDao.getAllCategories()
        val subCategoriesFlow = subCategoryDao.getAllSubCategories()

        return combine(expensesFlow, categoriesFlow, subCategoriesFlow) { expenses, categories, subCategories ->
            val categoryMap = categories.associateBy { it.id }
            val subCategoryMap = subCategories.associateBy { it.id }
            expenses.mapNotNull { entity ->
                val category = categoryMap[entity.categoryId]?.toCategory() ?: return@mapNotNull null
                val subCategory = entity.subCategoryId?.let { subCategoryMap[it]?.toSubCategory() }
                ExpenseWithDetails(
                    id = entity.id,
                    name = entity.name,
                    amount = entity.amount,
                    category = category,
                    subCategory = subCategory,
                    notes = entity.notes,
                    expenseDate = kotlinx.datetime.Instant.fromEpochMilliseconds(entity.expenseDate)
                        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
                    createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(entity.createdAt)
                        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                )
            }
        }
    }

    override fun getExpensesForCategoryAndSubCategory(
        categoryId: Long,
        subCategoryId: Long?,
        filter: DateFilter
    ): Flow<List<ExpenseWithDetails>> {
        val range = filter.toDateRange()
        val expensesFlow = if (subCategoryId != null) {
            expenseDao.getExpensesByCategoryAndSubCategory(categoryId, subCategoryId, range.startMs, range.endMs)
        } else {
            expenseDao.getExpensesByCategoryNoSubCategory(categoryId, range.startMs, range.endMs)
        }
        val categoriesFlow = categoryDao.getAllCategories()
        val subCategoriesFlow = subCategoryDao.getAllSubCategories()

        return combine(expensesFlow, categoriesFlow, subCategoriesFlow) { expenses, categories, subCategories ->
            val categoryMap = categories.associateBy { it.id }
            val subCategoryMap = subCategories.associateBy { it.id }
            expenses.mapNotNull { entity ->
                val category = categoryMap[entity.categoryId]?.toCategory() ?: return@mapNotNull null
                val subCat = entity.subCategoryId?.let { subCategoryMap[it]?.toSubCategory() }
                ExpenseWithDetails(
                    id = entity.id,
                    name = entity.name,
                    amount = entity.amount,
                    category = category,
                    subCategory = subCat,
                    notes = entity.notes,
                    expenseDate = kotlinx.datetime.Instant.fromEpochMilliseconds(entity.expenseDate)
                        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
                    createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(entity.createdAt)
                        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                )
            }
        }
    }

    override suspend fun getExpenseById(id: Long): Result<Expense, DataError.Local> = try {
        val entity = expenseDao.getExpenseById(id)
        if (entity != null) {
            Result.Success(entity.toExpense())
        } else {
            Result.Error(DataError.Local.NOT_FOUND)
        }
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun insertExpense(expense: Expense): Result<Long, DataError.Local> = try {
        Result.Success(expenseDao.insertExpense(expense.toExpenseEntity()))
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun updateExpense(expense: Expense): EmptyResult<DataError.Local> = try {
        expenseDao.updateExpense(expense.toExpenseEntity())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun deleteExpense(id: Long): EmptyResult<DataError.Local> = try {
        expenseDao.deleteExpense(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override fun getTotalSpend(filter: DateFilter): Flow<Double> {
        val range = filter.toDateRange()
        return expenseDao.getTotalSpend(range.startMs, range.endMs)
    }

    override fun getSpendByCategory(filter: DateFilter): Flow<Map<Long, Double>> {
        val range = filter.toDateRange()
        return expenseDao.getSpendByCategory(range.startMs, range.endMs)
            .map { list -> list.associate { it.categoryId to it.total } }
    }
}
