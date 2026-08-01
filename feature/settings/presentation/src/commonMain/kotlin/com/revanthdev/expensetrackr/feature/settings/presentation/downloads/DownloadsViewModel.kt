package com.revanthdev.expensetrackr.feature.settings.presentation.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.repository.BackupFileStore
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.domain.repository.FileOpenResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Generates the PDF / Excel expense report for a chosen period and writes it to the device's
 * downloads folder (the same place the CSV backups go — see [BackupFileStore]).
 */
class DownloadsViewModel(
    private val expenseRepository: ExpenseRepository,
    private val fileStore: BackupFileStore,
) : ViewModel() {

    private val _state = MutableStateFlow(DownloadsState(locationLabel = fileStore.locationLabel))
    val state = _state.asStateFlow()

    private val _events = Channel<DownloadsEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: DownloadsAction) {
        when (action) {
            is DownloadsAction.OnFormatClick ->
                if (!_state.value.isBusy) {
                    _state.update { it.copy(choosingPeriodFor = action.format) }
                }

            DownloadsAction.OnDismissPeriodPicker ->
                _state.update { it.copy(choosingPeriodFor = null) }

            is DownloadsAction.OnPeriodPicked -> {
                val format = _state.value.choosingPeriodFor ?: return
                generate(format, action.period)
            }
        }
    }

    private fun generate(format: ReportFormat, period: ReportPeriod) = viewModelScope.launch {
        _state.update { it.copy(choosingPeriodFor = null, generating = format) }
        val result = runCatching { export(format, period) }.getOrDefault(DownloadResult.Failed)
        _state.update { it.copy(generating = null) }
        _events.send(DownloadsEvent.Show(result))
    }

    private suspend fun export(format: ReportFormat, period: ReportPeriod): DownloadResult {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val (start, end) = period.resolve(now.date)

        // CustomRange covers every period the picker offers, so reports reuse the same query the
        // rest of the app uses. Both ends are inclusive whole days.
        val transactions = expenseRepository
            .getTransactionsWithDetails(DateFilter.CustomRange(start, end))
            .first()
        if (transactions.isEmpty()) return DownloadResult.NoData

        // Rendering is pure CPU work over potentially thousands of rows — keep it off the main thread.
        val bytes = withContext(Dispatchers.Default) {
            val data = ReportBuilder.build(transactions, start, end, now)
            when (format) {
                ReportFormat.PDF -> ReportPdf.render(data)
                ReportFormat.EXCEL -> ReportXlsx.render(data)
            }
        }

        val fileName = reportFileName(start, end, format)
        if (!fileStore.writeBytes(fileName, bytes, format.mimeType)) return DownloadResult.Failed

        // The file is on disk either way — a missing viewer app only changes the message shown.
        val opened = runCatching { fileStore.openFile(fileName, format.mimeType) }
            .getOrDefault(FileOpenResult.Failed)

        return DownloadResult.Saved(fileName, fileStore.locationLabel, transactions.size, opened)
    }
}
