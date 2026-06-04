package com.revanthdev.expensetrackr.core.data.repository

import com.revanthdev.expensetrackr.core.data.mapper.toCategory
import com.revanthdev.expensetrackr.core.data.mapper.toCategoryEntity
import com.revanthdev.expensetrackr.core.data.mapper.toSubCategory
import com.revanthdev.expensetrackr.core.data.mapper.toSubCategoryEntity
import com.revanthdev.expensetrackr.core.database.dao.CategoryDao
import com.revanthdev.expensetrackr.core.database.dao.SubCategoryDao
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.SubCategory
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import com.revanthdev.expensetrackr.core.domain.util.DataError
import com.revanthdev.expensetrackr.core.domain.util.EmptyResult
import com.revanthdev.expensetrackr.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCategoryRepository(
    private val categoryDao: CategoryDao,
    private val subCategoryDao: SubCategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { list -> list.map { it.toCategory() } }

    override suspend fun getCategoryById(id: Long): Result<Category, DataError.Local> = try {
        val entity = categoryDao.getCategoryById(id)
        if (entity != null) {
            Result.Success(entity.toCategory())
        } else {
            Result.Error(DataError.Local.NOT_FOUND)
        }
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun insertCategory(category: Category): Result<Long, DataError.Local> = try {
        Result.Success(categoryDao.insertCategory(category.toCategoryEntity()))
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun updateCategory(category: Category): EmptyResult<DataError.Local> = try {
        categoryDao.updateCategory(category.toCategoryEntity())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun deleteCategory(id: Long): EmptyResult<DataError.Local> = try {
        categoryDao.deleteCategory(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun hasExpensesForCategory(id: Long): Boolean =
        categoryDao.hasExpenses(id)

    override fun getAllSubCategories(): Flow<List<SubCategory>> =
        subCategoryDao.getAllSubCategories().map { list -> list.map { it.toSubCategory() } }

    override fun getSubCategoriesForCategory(categoryId: Long): Flow<List<SubCategory>> =
        subCategoryDao.getSubCategoriesForCategory(categoryId).map { list -> list.map { it.toSubCategory() } }

    override suspend fun insertSubCategory(subCategory: SubCategory): Result<Long, DataError.Local> = try {
        Result.Success(subCategoryDao.insertSubCategory(subCategory.toSubCategoryEntity()))
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun updateSubCategory(subCategory: SubCategory): EmptyResult<DataError.Local> = try {
        subCategoryDao.updateSubCategory(subCategory.toSubCategoryEntity())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun deleteSubCategory(id: Long): EmptyResult<DataError.Local> = try {
        subCategoryDao.deleteSubCategory(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun hasExpensesForSubCategory(id: Long): Boolean =
        subCategoryDao.hasExpenses(id)

    override suspend fun detachExpensesFromSubCategory(id: Long): EmptyResult<DataError.Local> = try {
        subCategoryDao.detachExpenses(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }
}
