package com.revanthdev.expensetrackr.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"]
        ),
        ForeignKey(
            entity = SubCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["subCategoryId"]
        )
    ],
    indices = [
        Index("categoryId"),
        Index("subCategoryId"),
        Index("expenseDate"),
        Index("type")
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val categoryId: Long,
    val subCategoryId: Long?,
    val notes: String?,
    // "EXPENSE" or "INCOME" (name of domain TransactionType). Defaults to EXPENSE for legacy rows.
    val type: String = "EXPENSE",
    val expenseDate: Long,
    val createdAt: Long
)
