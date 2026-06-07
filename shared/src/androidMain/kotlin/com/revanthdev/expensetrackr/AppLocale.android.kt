package com.revanthdev.expensetrackr

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
actual fun ProvideAppLocale(languageTag: String?, content: @Composable () -> Unit) {
    if (languageTag.isNullOrBlank()) {
        content()
        return
    }
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val localized = remember(languageTag, configuration) {
        val locale = Locale(languageTag)
        // compose-resources reads Locale.current (≈ Locale.getDefault()) on Android, so set both.
        Locale.setDefault(locale)
        val newConfig = Configuration(configuration).apply { setLocale(locale) }
        newConfig to context.createConfigurationContext(newConfig)
    }
    CompositionLocalProvider(
        LocalConfiguration provides localized.first,
        LocalContext provides localized.second,
    ) {
        content()
    }
}
