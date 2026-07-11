package com.revanthdev.expensetrackr.feature.settings.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.Expense
import com.revanthdev.expensetrackr.core.domain.model.SubCategory
import com.revanthdev.expensetrackr.core.domain.model.TransactionType
import com.revanthdev.expensetrackr.core.domain.repository.BackupFileStore
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.domain.util.Result
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private const val EXPENSES_FILE = "expenses.csv"
private const val INCOMES_FILE = "incomes.csv"

class SyncViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val fileStore: BackupFileStore,
) : ViewModel() {

    private val _state = MutableStateFlow(SyncState(locationLabel = fileStore.locationLabel))
    val state = _state.asStateFlow()

    private val _events = Channel<SyncEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: SyncAction) {
        when (action) {
            SyncAction.OnSyncClick -> export()
            SyncAction.OnRestoreClick -> restore()
        }
    }

    private fun export() = viewModelScope.launch {
        _state.update { it.copy(isBusy = true) }
        val result = runCatching { doExport() }.getOrDefault(SyncResult.Failed)
        _state.update { it.copy(isBusy = false) }
        _events.send(SyncEvent.Show(result))
    }

    private fun restore() = viewModelScope.launch {
        _state.update { it.copy(isBusy = true) }
        val result = runCatching { doRestore() }.getOrDefault(SyncResult.Failed)
        _state.update { it.copy(isBusy = false) }
        _events.send(SyncEvent.Show(result))
    }

    private suspend fun doExport(): SyncResult {
        val transactions = when (val r = expenseRepository.getAllTransactions()) {
            is Result.Success -> r.data
            is Result.Error -> return SyncResult.Failed
        }
        val categories = categoryRepository.getAllCategories().first().associateBy { it.id }
        val subCategories = categoryRepository.getAllSubCategories().first().associateBy { it.id }

        fun rowsFor(type: TransactionType): List<BackupRow> =
            transactions.filter { it.type == type }.map { e ->
                val cat = categories[e.categoryId]
                BackupRow(
                    id = e.id,
                    name = e.name,
                    amount = e.amount,
                    categoryName = cat?.name.orEmpty(),
                    categoryIcon = cat?.icon.orEmpty(),
                    categoryColor = cat?.colorHex.orEmpty(),
                    subCategoryName = e.subCategoryId?.let { subCategories[it]?.name },
                    notes = e.notes,
                    date = e.expenseDate,
                    createdAt = e.createdAt,
                )
            }

        val expenseRows = rowsFor(TransactionType.EXPENSE)
        val incomeRows = rowsFor(TransactionType.INCOME)

        val okExpenses = fileStore.writeText(EXPENSES_FILE, TransactionCsv.encode(expenseRows))
        val okIncomes = fileStore.writeText(INCOMES_FILE, TransactionCsv.encode(incomeRows))

        return if (okExpenses && okIncomes) {
            SyncResult.Exported(expenseRows.size, incomeRows.size, fileStore.locationLabel)
        } else {
            SyncResult.Failed
        }
    }

    private suspend fun doRestore(): SyncResult {
        val expensesCsv = fileStore.readText(EXPENSES_FILE)
        val incomesCsv = fileStore.readText(INCOMES_FILE)
        if (expensesCsv == null && incomesCsv == null) return SyncResult.NoBackupFound

        // Live snapshots we grow as we create missing categories, so we don't duplicate them.
        val categories = categoryRepository.getAllCategories().first().toMutableList()
        val subCategories = categoryRepository.getAllSubCategories().first().toMutableList()

        suspend fun resolveCategoryId(row: BackupRow, type: TransactionType): Long? {
            if (row.categoryName.isBlank()) return null
            categories.find { it.name.equals(row.categoryName, ignoreCase = true) && it.type == type }
                ?.let { return it.id }
            val newCat = Category(
                name = row.categoryName,
                icon = row.categoryIcon.ifBlank { "🏷️" },
                colorHex = row.categoryColor.ifBlank { "#607D8B" },
                type = type,
                createdAt = now(),
            )
            val id = (categoryRepository.insertCategory(newCat) as? Result.Success)?.data ?: return null
            categories.add(newCat.copy(id = id))
            return id
        }

        suspend fun resolveSubCategoryId(name: String?, categoryId: Long): Long? {
            if (name.isNullOrBlank()) return null
            subCategories.find { it.name.equals(name, ignoreCase = true) && it.categoryId == categoryId }
                ?.let { return it.id }
            val newSub = SubCategory(name = name, categoryId = categoryId, createdAt = now())
            val id = (categoryRepository.insertSubCategory(newSub) as? Result.Success)?.data ?: return null
            subCategories.add(newSub.copy(id = id))
            return id
        }

        var imported = 0
        suspend fun importFile(csv: String?, type: TransactionType) {
            if (csv == null) return
            for (row in TransactionCsv.decode(csv)) {
                val categoryId = resolveCategoryId(row, type) ?: continue
                val subCategoryId = resolveSubCategoryId(row.subCategoryName, categoryId)
                val expense = Expense(
                    id = row.id, // preserve id so re-restore updates instead of duplicating
                    name = row.name,
                    amount = row.amount,
                    categoryId = categoryId,
                    subCategoryId = subCategoryId,
                    notes = row.notes,
                    type = type,
                    expenseDate = row.date,
                    createdAt = row.createdAt,
                )
                if (expenseRepository.insertExpense(expense) is Result.Success) imported++
            }
        }

        importFile(expensesCsv, TransactionType.EXPENSE)
        importFile(incomesCsv, TransactionType.INCOME)
        return SyncResult.Restored(imported)
    }

    private fun now(): LocalDateTime =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}
