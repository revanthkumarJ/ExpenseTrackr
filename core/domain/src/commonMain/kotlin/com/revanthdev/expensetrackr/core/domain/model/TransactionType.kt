package com.revanthdev.expensetrackr.core.domain.model

/**
 * Whether a transaction is money going out (an [EXPENSE]) or coming in ([INCOME]).
 *
 * All existing spend/budget/analytics math operates on [EXPENSE] rows only — income is recorded
 * alongside expenses in the same table but is deliberately excluded from every expense calculation.
 */
enum class TransactionType {
    EXPENSE,
    INCOME;

    companion object {
        /** Parse a stored name, falling back to [EXPENSE] for any unknown/legacy value. */
        fun fromName(name: String?): TransactionType =
            runCatching { name?.let { valueOf(it) } }.getOrNull() ?: EXPENSE
    }
}
