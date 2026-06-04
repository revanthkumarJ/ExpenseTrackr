package com.revanthdev.expensetrackr.core.domain.model

import kotlinx.datetime.LocalDateTime

data class SubCategory(
    val id: Long = 0,
    val name: String,
    val categoryId: Long,
    val createdAt: LocalDateTime
)
