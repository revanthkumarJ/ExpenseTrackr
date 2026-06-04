package com.revanthdev.expensetrackr

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform