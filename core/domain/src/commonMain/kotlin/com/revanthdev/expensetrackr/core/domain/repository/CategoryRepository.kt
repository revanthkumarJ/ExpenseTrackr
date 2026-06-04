package com.revanthdev.expensetrackr.core.domain.repository

import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.SubCategory
import com.revanthdev.expensetrackr.core.domain.util.DataError
import com.revanthdev.expensetrackr.core.domain.util.EmptyResult
import com.revanthdev.expensetrackr.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Result<Category, DataError.Local>
    suspend fun insertCategory(category: Category): Result<Long, DataError.Local>
    suspend fun updateCategory(category: Category): EmptyResult<DataError.Local>
    suspend fun deleteCategory(id: Long): EmptyResult<DataError.Local>
    suspend fun hasExpensesForCategory(id: Long): Boolean
    fun getAllSubCategories(): Flow<List<SubCategory>>
    fun getSubCategoriesForCategory(categoryId: Long): Flow<List<SubCategory>>
    suspend fun insertSubCategory(subCategory: SubCategory): Result<Long, DataError.Local>
    suspend fun updateSubCategory(subCategory: SubCategory): EmptyResult<DataError.Local>
    suspend fun deleteSubCategory(id: Long): EmptyResult<DataError.Local>
    suspend fun hasExpensesForSubCategory(id: Long): Boolean
    suspend fun detachExpensesFromSubCategory(id: Long): EmptyResult<DataError.Local>
}
