package com.revanthdev.expensetrackr.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ad_operation_counters")
data class AdOperationCounterEntity(
    @PrimaryKey val operation: String,
    val count: Int,
)
