package com.revanthdev.expensetrackr.core.domain.model

import kotlinx.datetime.LocalDateTime

data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val budgetAmount: Double? = null,
    val createdAt: LocalDateTime
)
