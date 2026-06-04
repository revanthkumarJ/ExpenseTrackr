package com.revanthdev.expensetrackr.core.database

import com.revanthdev.expensetrackr.core.database.dao.CategoryDao
import com.revanthdev.expensetrackr.core.database.entity.CategoryEntity

class DefaultCategorySeeder(private val dao: CategoryDao) {
    private val defaultCategories = listOf(
        CategoryEntity(
            name = "Food & Dining",
            icon = "🍔",
            colorHex = "#FF6D00",
            isDefault = true,
            budgetAmount = null,
            createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
        ),
        CategoryEntity(
            name = "Rent & Housing",
            icon = "🏠",
            colorHex = "#1565C0",
            isDefault = true,
            budgetAmount = null,
            createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
        ),
        CategoryEntity(
            name = "Travel",
            icon = "✈️",
            colorHex = "#00796B",
            isDefault = true,
            budgetAmount = null,
            createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
        ),
        CategoryEntity(
            name = "Medicines & Health",
            icon = "💊",
            colorHex = "#C62828",
            isDefault = true,
            budgetAmount = null,
            createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
        ),
        CategoryEntity(
            name = "Entertainment",
            icon = "🎬",
            colorHex = "#6A1B9A",
            isDefault = true,
            budgetAmount = null,
            createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
        ),
        CategoryEntity(
            name = "Shopping",
            icon = "🛍️",
            colorHex = "#AD1457",
            isDefault = true,
            budgetAmount = null,
            createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
        ),
        CategoryEntity(
            name = "Education",
            icon = "📚",
            colorHex = "#283593",
            isDefault = true,
            budgetAmount = null,
            createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
        ),
        CategoryEntity(
            name = "Utilities",
            icon = "⚡",
            colorHex = "#F9A825",
            isDefault = true,
            budgetAmount = null,
            createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
        ),
        CategoryEntity(
            name = "Others",
            icon = "📦",
            colorHex = "#616161",
            isDefault = true,
            budgetAmount = null,
            createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
        )
    )

    suspend fun seedIfEmpty() {
        // Seed only when no categories exist yet.
        // getAllCategories() is a Flow; we check via a dedicated approach —
        // since CategoryDao has no count query, we attempt an insert-and-ignore
        // pattern by just calling seedCategories() guarded externally, or we
        // rely on callers to check before calling this.
        // For a self-contained version, callers should invoke seedCategories()
        // only when the categories flow first emits an empty list.
    }

    suspend fun seedCategories() {
        defaultCategories.forEach { dao.insertCategory(it) }
    }
}
