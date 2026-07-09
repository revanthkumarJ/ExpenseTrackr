package com.revanthdev.expensetrackr.core.domain.model

import kotlinx.datetime.LocalDateTime

data class ExpenseWithDetails(
    val id: Long,
    val name: String,
    val amount: Double,
    val category: Category,
    val subCategory: SubCategory?,
    val notes: String?,
    val type: TransactionType = TransactionType.EXPENSE,
    val expenseDate: LocalDateTime,
    val createdAt: LocalDateTime
)
