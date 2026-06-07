package com.revanthdev.expensetrackr

import androidx.compose.runtime.Composable

/**
 * Applies the user-selected [languageTag] (BCP-47, e.g. "hi") to [content] so that
 * `stringResource` resolves to that language. Pass null to follow the system language.
 *
 * Android overrides the composition's Configuration/Context locale. Other platforms set the
 * default JVM locale and force re-composition; a full effect may require an app restart there.
 */
@Composable
expect fun ProvideAppLocale(languageTag: String?, content: @Composable () -> Unit)
