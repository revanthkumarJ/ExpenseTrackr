package com.revanthdev.expensetrackr.feature.budget.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_save
import expensetrackr.core.presentation.generated.resources.budget_reset_all
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun BudgetActionsBar(
    isSaving: Boolean,
    onReset: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth().padding(16.dp)
    ) {
        OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) {
            Text(stringResource(Res.string.budget_reset_all))
        }
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f),
            enabled = !isSaving
        ) {
            if (isSaving) CircularProgressIndicator(Modifier.size(20.dp))
            else Text(stringResource(Res.string.action_save))
        }
    }
}

@Preview
@Composable
private fun BudgetActionsBarPreview() {
    ExpenseTrackerTheme {
        BudgetActionsBar(isSaving = false, onReset = {}, onSave = {})
    }
}
