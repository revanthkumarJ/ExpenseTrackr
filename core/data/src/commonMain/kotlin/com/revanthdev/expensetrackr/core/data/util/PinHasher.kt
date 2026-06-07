package com.revanthdev.expensetrackr.core.data.util

/** SHA-256 hash of a PIN, hex-encoded. Used for app-lock PIN storage & verification. */
expect fun hashPin(pin: String): String
