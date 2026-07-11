package com.revanthdev.expensetrackr.feature.settings.presentation.sync

data class SyncState(
    val locationLabel: String = "",
    val isBusy: Boolean = false,
)

sealed interface SyncAction {
    /** Export current data to the backup CSVs (full snapshot, overwrites). */
    data object OnSyncClick : SyncAction
    /** Restore/merge data back from the existing backup CSVs. */
    data object OnRestoreClick : SyncAction
}

/**
 * Structured outcome of a sync/restore. The screen resolves it to a localized message
 * (ViewModels can't call `stringResource`).
 */
sealed interface SyncResult {
    data class Exported(val expenses: Int, val incomes: Int, val location: String) : SyncResult
    data class Restored(val count: Int) : SyncResult
    data object NoBackupFound : SyncResult
    data object Failed : SyncResult
}

sealed interface SyncEvent {
    data class Show(val result: SyncResult) : SyncEvent
}
