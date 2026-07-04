package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import com.revanthdev.expensetrackr.core.domain.model.AppSettings
import com.revanthdev.expensetrackr.core.domain.model.SalaryCalculator
import com.revanthdev.expensetrackr.core.domain.model.SalaryEntry
import com.revanthdev.expensetrackr.core.presentation.util.DecimalFormatter
import com.revanthdev.expensetrackr.core.presentation.util.DecimalInputVisualTransformation
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_cancel
import expensetrackr.core.presentation.generated.resources.action_save
import expensetrackr.core.presentation.generated.resources.salary_amount
import expensetrackr.core.presentation.generated.resources.salary_apply_all
import expensetrackr.core.presentation.generated.resources.salary_apply_from_current
import expensetrackr.core.presentation.generated.resources.salary_apply_scope
import expensetrackr.core.presentation.generated.resources.salary_dialog_hint
import expensetrackr.core.presentation.generated.resources.salary_remove
import expensetrackr.core.presentation.generated.resources.settings_salary
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

/** Resolves the salary that applies to the current calendar month. */
internal fun currentMonthSalary(history: List<SalaryEntry>): Double {
    if (history.isEmpty()) return 0.0
    val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return SalaryCalculator.salaryForMonth(history, SalaryCalculator.monthIndexOf(now.year, now.monthNumber))
}

@Composable
internal fun SalaryDialog(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onSave: (amount: Double?, applyToAll: Boolean) -> Unit
) {
    val existing = currentMonthSalary(settings.salaryHistory)
    var amountText by remember { mutableStateOf(if (existing > 0.0) existing.toString().trimEnd('0').trimEnd('.') else "") }
    var applyToAll by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_salary)) },
        text = {
            Column {
                Text(
                    stringResource(Res.string.salary_dialog_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(stringResource(Res.string.salary_amount)) },
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    visualTransformation = DecimalInputVisualTransformation(DecimalFormatter()),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text(stringResource(Res.string.salary_apply_scope), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                SalaryScopeRow(
                    label = stringResource(Res.string.salary_apply_all),
                    selected = applyToAll,
                    onClick = { applyToAll = true }
                )
                SalaryScopeRow(
                    label = stringResource(Res.string.salary_apply_from_current),
                    selected = !applyToAll,
                    onClick = { applyToAll = false }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(amountText.toDoubleOrNull()?.takeIf { it > 0.0 }, applyToAll) }
            ) { Text(stringResource(Res.string.action_save)) }
        },
        dismissButton = {
            Row {
                if (existing > 0.0) {
                    TextButton(onClick = { onSave(null, false) }) {
                        Text(stringResource(Res.string.salary_remove), color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
            }
        }
    )
}

@Composable
private fun SalaryScopeRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview
@Composable
private fun SalaryDialogPreview() {
    ExpenseTrackerTheme {
        SalaryDialog(settings = AppSettings(), onDismiss = {}, onSave = { _, _ -> })
    }
}
