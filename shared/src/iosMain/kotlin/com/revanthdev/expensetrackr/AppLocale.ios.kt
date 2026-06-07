package com.revanthdev.expensetrackr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key

@Composable
actual fun ProvideAppLocale(languageTag: String?, content: @Composable () -> Unit) {
    // iOS resolves the bundle language at launch; force re-composition on change as best effort.
    // A full language switch may require relaunching the app.
    key(languageTag) { content() }
}
