package com.revanthdev.expensetrackr.core.data.mapper

import com.revanthdev.expensetrackr.core.database.entity.CategoryEntity
import com.revanthdev.expensetrackr.core.domain.model.Category
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun CategoryEntity.toCategory(): Category = Category(
    id = id,
    name = name,
    icon = icon,
    colorHex = colorHex,
    isDefault = isDefault,
    budgetAmount = budgetAmount,
    createdAt = Instant.fromEpochMilliseconds(createdAt).toLocalDateTime(TimeZone.currentSystemDefault())
)

fun Category.toCategoryEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    icon = icon,
    colorHex = colorHex,
    isDefault = isDefault,
    budgetAmount = budgetAmount,
    createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
)
