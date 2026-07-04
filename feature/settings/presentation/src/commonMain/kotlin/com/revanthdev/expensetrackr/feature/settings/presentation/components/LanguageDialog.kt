package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import com.revanthdev.expensetrackr.core.presentation.appLanguages
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_close
import expensetrackr.core.presentation.generated.resources.language_system_default
import expensetrackr.core.presentation.generated.resources.settings_language
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LanguageDialog(
    currentLanguage: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val systemDefault = stringResource(Res.string.language_system_default)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_language)) },
        text = {
            LazyColumn {
                item {
                    LanguageRow(systemDefault, selected = currentLanguage == null) {
                        onSelect(null); onDismiss()
                    }
                }
                items(appLanguages) { lang ->
                    LanguageRow(lang.nativeName, selected = currentLanguage == lang.tag) {
                        onSelect(lang.tag); onDismiss()
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_close)) } }
    )
}

@Composable
private fun LanguageRow(name: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(8.dp))
        Text(name)
    }
}

@Preview
@Composable
private fun LanguageDialogPreview() {
    ExpenseTrackerTheme {
        LanguageDialog(currentLanguage = null, onSelect = {}, onDismiss = {})
    }
}
