package com.revanthdev.expensetrackr.core.data.mapper

import com.revanthdev.expensetrackr.core.database.entity.ExpenseEntity
import com.revanthdev.expensetrackr.core.domain.model.Expense
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun ExpenseEntity.toExpense(): Expense = Expense(
    id = id,
    name = name,
    amount = amount,
    categoryId = categoryId,
    subCategoryId = subCategoryId,
    notes = notes,
    expenseDate = Instant.fromEpochMilliseconds(expenseDate).toLocalDateTime(TimeZone.currentSystemDefault()),
    createdAt = Instant.fromEpochMilliseconds(createdAt).toLocalDateTime(TimeZone.currentSystemDefault())
)

fun Expense.toExpenseEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    name = name,
    amount = amount,
    categoryId = categoryId,
    subCategoryId = subCategoryId,
    notes = notes,
    expenseDate = expenseDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
    createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
)
