package com.revanthdev.expensetrackr.core.designsystem.theme

import androidx.compose.ui.graphics.Color

val PrimaryIndigo = Color(0xFF3D52A0)
val PrimaryIndigoDark = Color(0xFF7091E6)
val SecondaryTeal = Color(0xFF00897B)
val SecondaryTealDark = Color(0xFF4DB6AC)

// Containers & accents — light
val PrimaryContainerLight = Color(0xFFDFE3FF)
val OnPrimaryContainerLight = Color(0xFF0C1565)
val SecondaryContainerLight = Color(0xFFB6F2EA)
val OnSecondaryContainerLight = Color(0xFF00201C)
val TertiaryLight = Color(0xFFB8467E)
val TertiaryContainerLight = Color(0xFFFFD9E5)
val OnTertiaryContainerLight = Color(0xFF3E0021)

// Containers & accents — dark
val PrimaryContainerDark = Color(0xFF273573)
val OnPrimaryContainerDark = Color(0xFFDFE3FF)
val SecondaryContainerDark = Color(0xFF005048)
val OnSecondaryContainerDark = Color(0xFFB6F2EA)
val TertiaryDark = Color(0xFFFFB0CC)
val TertiaryContainerDark = Color(0xFF8E2A5E)
val OnTertiaryContainerDark = Color(0xFFFFD9E5)

val BackgroundLight = Color(0xFFF6F7FB)
val BackgroundDark = Color(0xFF111318)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1A1C22)
// Tonal surface containers for layered depth
val SurfaceContainerLowLight = Color(0xFFF1F2F8)
val SurfaceContainerLight = Color(0xFFECEEF6)
val SurfaceContainerHighLight = Color(0xFFE6E8F2)
val SurfaceContainerLowDark = Color(0xFF1E2026)
val SurfaceContainerDark = Color(0xFF22242B)
val SurfaceContainerHighDark = Color(0xFF2C2F37)
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
