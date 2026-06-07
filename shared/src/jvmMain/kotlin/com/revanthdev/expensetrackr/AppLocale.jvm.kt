package com.revanthdev.expensetrackr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import java.util.Locale

@Composable
actual fun ProvideAppLocale(languageTag: String?, content: @Composable () -> Unit) {
    if (!languageTag.isNullOrBlank()) {
        remember(languageTag) { Locale.setDefault(Locale(languageTag)); languageTag }
    }
    // Force the subtree to recompose when the language changes.
    key(languageTag) { content() }
}
