package com.revanthdev.expensetrackr.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.revanthdev.expensetrackr.core.database.entity.SubCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubCategoryDao {
    @Query("SELECT * FROM sub_categories ORDER BY name ASC")
    fun getAllSubCategories(): Flow<List<SubCategoryEntity>>

    @Query("SELECT * FROM sub_categories WHERE categoryId = :categoryId ORDER BY name ASC")
    fun getSubCategoriesForCategory(categoryId: Long): Flow<List<SubCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubCategory(subCategory: SubCategoryEntity): Long

    @Update
    suspend fun updateSubCategory(subCategory: SubCategoryEntity)

    @Query("DELETE FROM sub_categories WHERE id = :id")
    suspend fun deleteSubCategory(id: Long)

    @Query("SELECT COUNT(*) > 0 FROM expenses WHERE subCategoryId = :subCategoryId")
    suspend fun hasExpenses(subCategoryId: Long): Boolean

    @Query("UPDATE expenses SET subCategoryId = NULL WHERE subCategoryId = :subCategoryId")
    suspend fun detachExpenses(subCategoryId: Long)
}
