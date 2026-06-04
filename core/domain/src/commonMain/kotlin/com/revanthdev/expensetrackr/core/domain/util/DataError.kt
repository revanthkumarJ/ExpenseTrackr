package com.revanthdev.expensetrackr.core.domain.util

sealed interface DataError : Error {
    enum class Local : DataError { DISK_FULL, NOT_FOUND, UNKNOWN }
}
