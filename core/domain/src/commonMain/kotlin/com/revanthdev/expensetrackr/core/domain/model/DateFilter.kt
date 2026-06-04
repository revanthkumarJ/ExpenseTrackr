package com.revanthdev.expensetrackr.core.domain.model

import kotlinx.datetime.LocalDate

sealed interface DateFilter {
    data object ThisWeek : DateFilter
    data object ThisMonth : DateFilter
    data object LastMonth : DateFilter
    data object ThisYear : DateFilter
    data class CustomRange(val start: LocalDate, val end: LocalDate) : DateFilter
}
