package com.revanthdev.expensetrackr.core.domain.model

import kotlinx.datetime.LocalDateTime

data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val budgetAmount: Double? = null,
    // Whether this category is used for expense or income transactions. Income categories never
    // carry a budget and never appear in expense/budget/analytics screens.
    val type: TransactionType = TransactionType.EXPENSE,
    val createdAt: LocalDateTime
)
