package com.revanthdev.expensetrackr.core.database

import com.revanthdev.expensetrackr.core.database.dao.CategoryDao
import com.revanthdev.expensetrackr.core.database.entity.CategoryEntity

class DefaultCategorySeeder(private val dao: CategoryDao) {
    private fun now() = kotlin.time.Clock.System.now().toEpochMilliseconds()

    private fun expenseCategories() = listOf(
        Triple("Food & Dining", "🍔", "#FF6D00"),
        Triple("Rent & Housing", "🏠", "#1565C0"),
        Triple("Travel", "✈️", "#00796B"),
        Triple("Medicines & Health", "💊", "#C62828"),
        Triple("Entertainment", "🎬", "#6A1B9A"),
        Triple("Shopping", "🛍️", "#AD1457"),
        Triple("Education", "📚", "#283593"),
        Triple("Utilities", "⚡", "#F9A825"),
        Triple("Others", "📦", "#616161")
    ).map { (name, icon, color) ->
        CategoryEntity(
            name = name, icon = icon, colorHex = color,
            isDefault = true, budgetAmount = null, type = "EXPENSE", createdAt = now()
        )
    }

    private fun incomeCategories() = listOf(
        Triple("Salary", "💰", "#2E7D32"),
        Triple("Business", "🏢", "#00695C"),
        Triple("Freelance", "💻", "#1565C0"),
        Triple("Investments", "📈", "#6A1B9A"),
        Triple("Interest", "🏦", "#0277BD"),
        Triple("Gifts", "🎁", "#AD1457"),
        Triple("Other Income", "➕", "#616161")
    ).map { (name, icon, color) ->
        CategoryEntity(
            name = name, icon = icon, colorHex = color,
            isDefault = true, budgetAmount = null, type = "INCOME", createdAt = now()
        )
    }

    /**
     * Seed the default categories for each type independently, so both a fresh install and an
     * upgraded database (which already has expense categories but no income ones) end up populated.
     */
    suspend fun seedIfEmpty() {
        if (dao.getCategoryCountByType("EXPENSE") == 0) {
            expenseCategories().forEach { dao.insertCategory(it) }
        }
        if (dao.getCategoryCountByType("INCOME") == 0) {
            incomeCategories().forEach { dao.insertCategory(it) }
        }
    }
}
