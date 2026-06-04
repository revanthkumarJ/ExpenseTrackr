package com.revanthdev.expensetrackr.core.presentation

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
}
