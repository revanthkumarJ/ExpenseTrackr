package com.revanthdev.expensetrackr.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.revanthdev.expensetrackr.core.database.entity.AdOperationCounterEntity

@Dao
interface AdOperationCounterDao {
    @Query("SELECT count FROM ad_operation_counters WHERE operation = :operation")
    suspend fun count(operation: String): Int?

    @Upsert
    suspend fun save(counter: AdOperationCounterEntity)
}
