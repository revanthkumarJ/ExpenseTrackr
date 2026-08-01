package com.revanthdev.expensetrackr.feature.settings.presentation.downloads

import com.revanthdev.expensetrackr.core.domain.repository.FileOpenResult

data class DownloadsState(
    val locationLabel: String = "",
    /** Non-null while a report is being generated — the format tells the UI which card to spin. */
    val generating: ReportFormat? = null,
    /** Non-null while the period picker is open, carrying the format the user tapped. */
    val choosingPeriodFor: ReportFormat? = null,
) {
    val isBusy: Boolean get() = generating != null
}

sealed interface DownloadsAction {
    /** Tapping "Download PDF" / "Download Excel" opens the period picker. */
    data class OnFormatClick(val format: ReportFormat) : DownloadsAction
    data object OnDismissPeriodPicker : DownloadsAction
    data class OnPeriodPicked(val period: ReportPeriod) : DownloadsAction
}

/**
 * Structured outcome of a download. The screen resolves it to a localized message
 * (ViewModels can't call `stringResource`).
 */
sealed interface DownloadResult {
    /**
     * The report was written. [opened] says whether a viewer app could be launched for it — the
     * file is saved regardless, so this only changes the wording of the confirmation.
     */
    data class Saved(
        val fileName: String,
        val location: String,
        val transactions: Int,
        val opened: FileOpenResult,
    ) : DownloadResult

    data object NoData : DownloadResult
    data object Failed : DownloadResult
}

sealed interface DownloadsEvent {
    data class Show(val result: DownloadResult) : DownloadsEvent
}
