package com.revanthdev.expensetrackr.feature.expenses.presentation

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.revanthdev.expensetrackr.core.designsystem.theme.ExpenseTrackerTheme
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_cancel
import expensetrackr.core.presentation.generated.resources.action_ok
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
private val PastOrTodayDates = object : SelectableDates {
    // Block future days — you can't have spent money tomorrow.
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        utcTimeMillis <= Clock.System.now().toEpochMilliseconds()

    override fun isSelectableYear(year: Int): Boolean =
        year <= Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpenseDatePickerDialog(
    dateTime: LocalDateTime,
    onConfirm: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    val dpState = rememberDatePickerState(
        initialSelectedDateMillis = dateTime.date.atStartOfDayIn(TimeZone.UTC)
            .toEpochMilliseconds(),
        selectableDates = PastOrTodayDates
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                dpState.selectedDateMillis?.let { ms ->
                    val pickedDate =
                        Instant.fromEpochMilliseconds(ms).toLocalDateTime(TimeZone.UTC).date
                    onConfirm(LocalDateTime(pickedDate, dateTime.time))
                }
                onDismiss()
            }) { Text(stringResource(Res.string.action_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
        }
    ) {
        DatePicker(state = dpState)
    }
}

@Preview
@Composable
private fun ExpenseDatePickerDialogPreview() {
    ExpenseTrackerTheme {
        ExpenseDatePickerDialog(
            dateTime = LocalDateTime(2024, 1, 1, 12, 0),
            onConfirm = {},
            onDismiss = {}
        )
    }
}
