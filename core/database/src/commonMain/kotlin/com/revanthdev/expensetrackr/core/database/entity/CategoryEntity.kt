package com.revanthdev.expensetrackr.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,
    val colorHex: String,
    val isDefault: Boolean,
    val budgetAmount: Double?,
    // "EXPENSE" or "INCOME" (name of domain TransactionType). Defaults to EXPENSE for legacy rows.
    val type: String = "EXPENSE",
    val createdAt: Long
)
