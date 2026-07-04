package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_close
import expensetrackr.core.presentation.generated.resources.settings_theme
import expensetrackr.core.presentation.generated.resources.settings_theme_dark
import expensetrackr.core.presentation.generated.resources.settings_theme_light
import expensetrackr.core.presentation.generated.resources.settings_theme_system
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ThemeDialog(
    current: Boolean?,
    onSelect: (Boolean?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_theme)) },
        text = {
            Column {
                listOf(
                    null to stringResource(Res.string.settings_theme_system),
                    false to stringResource(Res.string.settings_theme_light),
                    true to stringResource(Res.string.settings_theme_dark)
                ).forEach { (value, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable {
                            onSelect(value)
                            onDismiss()
                        }.padding(vertical = 8.dp)
                    ) {
                        RadioButton(selected = current == value, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_close)) } }
    )
}

@Preview
@Composable
private fun ThemeDialogPreview() {
    ExpenseTrackerTheme {
        ThemeDialog(current = null, onSelect = {}, onDismiss = {})
    }
}
