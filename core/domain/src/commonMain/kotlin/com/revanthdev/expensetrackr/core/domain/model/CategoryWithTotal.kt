package com.revanthdev.expensetrackr.core.domain.model

data class CategoryWithTotal(
    val category: Category,
    val total: Double,
    val percentage: Double
)
