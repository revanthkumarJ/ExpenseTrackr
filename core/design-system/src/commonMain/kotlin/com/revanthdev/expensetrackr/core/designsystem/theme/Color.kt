package com.revanthdev.expensetrackr.core.designsystem.theme

import androidx.compose.ui.graphics.Color

val PrimaryIndigo = Color(0xFF3D52A0)
val PrimaryIndigoDark = Color(0xFF7091E6)
val SecondaryTeal = Color(0xFF00897B)
val SecondaryTealDark = Color(0xFF4DB6AC)

val BackgroundLight = Color(0xFFF8F9FA)
val BackgroundDark = Color(0xFF121212)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E1E)
val ErrorLight = Color(0xFFD32F2F)
val ErrorDark = Color(0xFFEF9A9A)

val BudgetGreen = Color(0xFF388E3C)
val BudgetYellow = Color(0xFFF57F17)
val BudgetRed = Color(0xFFC62828)

fun hexToColor(hex: String): Color = try {
    val cleaned = hex.trimStart('#')
    val argb = if (cleaned.length == 6) "FF$cleaned" else cleaned
    Color(argb.toLong(16).toInt())
} catch (e: Exception) {
    Color(0xFF616161)
}

val categoryColorPalette = listOf(
    Color(0xFFFF6D00), Color(0xFF1565C0), Color(0xFF00796B), Color(0xFFC62828),
    Color(0xFF6A1B9A), Color(0xFFAD1457), Color(0xFF283593), Color(0xFFF9A825), Color(0xFF616161)
)
