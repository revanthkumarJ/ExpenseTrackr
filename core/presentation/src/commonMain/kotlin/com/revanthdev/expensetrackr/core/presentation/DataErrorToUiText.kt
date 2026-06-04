package com.revanthdev.expensetrackr.core.presentation

import com.revanthdev.expensetrackr.core.domain.util.DataError

fun DataError.toUiText(): UiText = when (this) {
    DataError.Local.DISK_FULL -> UiText.DynamicString("Storage is full. Please free up space.")
    DataError.Local.NOT_FOUND -> UiText.DynamicString("Item not found.")
    DataError.Local.UNKNOWN -> UiText.DynamicString("Something went wrong. Please try again.")
}
