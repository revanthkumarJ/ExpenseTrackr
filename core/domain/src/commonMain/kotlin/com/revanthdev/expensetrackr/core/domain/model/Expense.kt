package com.revanthdev.expensetrackr.core.domain.model

import kotlinx.datetime.LocalDateTime

data class Expense(
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val categoryId: Long,
    val subCategoryId: Long? = null,
    val notes: String? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    val expenseDate: LocalDateTime,
    val createdAt: LocalDateTime
)
