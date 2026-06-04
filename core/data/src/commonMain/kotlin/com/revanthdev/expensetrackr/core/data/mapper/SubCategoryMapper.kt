package com.revanthdev.expensetrackr.core.data.mapper

import com.revanthdev.expensetrackr.core.database.entity.SubCategoryEntity
import com.revanthdev.expensetrackr.core.domain.model.SubCategory
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun SubCategoryEntity.toSubCategory(): SubCategory = SubCategory(
    id = id,
    name = name,
    categoryId = categoryId,
    createdAt = Instant.fromEpochMilliseconds(createdAt).toLocalDateTime(TimeZone.currentSystemDefault())
)

fun SubCategory.toSubCategoryEntity(): SubCategoryEntity = SubCategoryEntity(
    id = id,
    name = name,
    categoryId = categoryId,
    createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
)
