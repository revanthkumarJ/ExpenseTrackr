package com.revanthdev.expensetrackr.feature.settings.presentation.downloads

import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.DateFilter
import com.revanthdev.expensetrackr.core.domain.model.Expense
import com.revanthdev.expensetrackr.core.domain.model.ExpenseWithDetails
import com.revanthdev.expensetrackr.core.domain.model.TransactionType
import com.revanthdev.expensetrackr.core.domain.repository.BackupFileStore
import com.revanthdev.expensetrackr.core.domain.repository.ExpenseRepository
import com.revanthdev.expensetrackr.core.domain.repository.FileOpenResult
import com.revanthdev.expensetrackr.core.domain.util.DataError
import com.revanthdev.expensetrackr.core.domain.util.EmptyResult
import com.revanthdev.expensetrackr.core.domain.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Covers what happens *after* a report is generated: the file is opened, and every way that can
 * go wrong is reported as a message rather than an exception.
 */
class DownloadsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `a saved report is opened and reported as saved`() = runTest(dispatcher) {
        val store = FakeFileStore()
        val viewModel = DownloadsViewModel(FakeExpenseRepository(), store)

        val result = viewModel.download(ReportFormat.PDF)

        val saved = assertIs<DownloadResult.Saved>(result)
        assertEquals(FileOpenResult.Opened, saved.opened)
        assertTrue(saved.fileName.endsWith(".pdf"))
        assertEquals(listOf(saved.fileName to "application/pdf"), store.opened)
        assertTrue(store.written.getValue(saved.fileName).isNotEmpty())
    }

    @Test
    fun `no viewer app still counts as saved, with the no-app outcome`() = runTest(dispatcher) {
        val store = FakeFileStore(openResult = FileOpenResult.NoAppFound)
        val viewModel = DownloadsViewModel(FakeExpenseRepository(), store)

        val saved = assertIs<DownloadResult.Saved>(viewModel.download(ReportFormat.EXCEL))
        assertEquals(FileOpenResult.NoAppFound, saved.opened)
        // The report is still on disk — the user can open it from their file manager.
        assertTrue(store.written.containsKey(saved.fileName))
    }

    @Test
    fun `an exception while opening is contained and reported`() = runTest(dispatcher) {
        val store = FakeFileStore(openThrows = true)
        val viewModel = DownloadsViewModel(FakeExpenseRepository(), store)

        val saved = assertIs<DownloadResult.Saved>(viewModel.download(ReportFormat.PDF))
        assertEquals(FileOpenResult.Failed, saved.opened)
    }

    @Test
    fun `a failed write reports failure instead of throwing`() = runTest(dispatcher) {
        val store = FakeFileStore(writeSucceeds = false)
        val viewModel = DownloadsViewModel(FakeExpenseRepository(), store)

        assertEquals(DownloadResult.Failed, viewModel.download(ReportFormat.PDF))
        assertTrue(store.opened.isEmpty(), "nothing should be opened when the write failed")
    }

    @Test
    fun `a repository blowing up is contained`() = runTest(dispatcher) {
        val store = FakeFileStore()
        val viewModel = DownloadsViewModel(FakeExpenseRepository(throws = true), store)

        assertEquals(DownloadResult.Failed, viewModel.download(ReportFormat.PDF))
    }

    @Test
    fun `an empty period reports no data and writes nothing`() = runTest(dispatcher) {
        val store = FakeFileStore()
        val viewModel = DownloadsViewModel(FakeExpenseRepository(transactions = emptyList()), store)

        assertEquals(DownloadResult.NoData, viewModel.download(ReportFormat.PDF))
        assertTrue(store.written.isEmpty())
    }

    @Test
    fun `a second download is ignored while one is already running`() = runTest(dispatcher) {
        val viewModel = DownloadsViewModel(FakeExpenseRepository(), FakeFileStore())

        viewModel.onAction(DownloadsAction.OnFormatClick(ReportFormat.PDF))
        viewModel.onAction(DownloadsAction.OnPeriodPicked(ReportPeriod.ThisMonth))
        // The generation coroutine only starts once the dispatcher is pumped.
        runCurrent()
        assertEquals(ReportFormat.PDF, viewModel.state.value.generating)

        // Tapping the other card mid-generation must not open the picker again.
        viewModel.onAction(DownloadsAction.OnFormatClick(ReportFormat.EXCEL))
        assertEquals(null, viewModel.state.value.choosingPeriodFor)
    }

    @Test
    fun `the period picker opens and dismisses without starting a download`() = runTest(dispatcher) {
        val store = FakeFileStore()
        val viewModel = DownloadsViewModel(FakeExpenseRepository(), store)

        viewModel.onAction(DownloadsAction.OnFormatClick(ReportFormat.EXCEL))
        assertEquals(ReportFormat.EXCEL, viewModel.state.value.choosingPeriodFor)

        viewModel.onAction(DownloadsAction.OnDismissPeriodPicker)
        runCurrent()
        assertEquals(null, viewModel.state.value.choosingPeriodFor)
        assertTrue(store.written.isEmpty(), "dismissing must not generate anything")
    }

    @Test
    fun `picking a period with no format chosen is a no-op`() = runTest(dispatcher) {
        val store = FakeFileStore()
        val viewModel = DownloadsViewModel(FakeExpenseRepository(), store)

        viewModel.onAction(DownloadsAction.OnPeriodPicked(ReportPeriod.LastYear))
        runCurrent()

        assertEquals(null, viewModel.state.value.generating)
        assertTrue(store.written.isEmpty())
    }

    /** Picks a format, chooses a period, and returns the resulting event payload. */
    private suspend fun DownloadsViewModel.download(format: ReportFormat): DownloadResult {
        onAction(DownloadsAction.OnFormatClick(format))
        onAction(DownloadsAction.OnPeriodPicked(ReportPeriod.ThisMonth))
        return (events.first() as DownloadsEvent.Show).result
    }

    // ---- fakes ----

    private class FakeFileStore(
        private val writeSucceeds: Boolean = true,
        private val openResult: FileOpenResult = FileOpenResult.Opened,
        private val openThrows: Boolean = false,
    ) : BackupFileStore {
        val written = mutableMapOf<String, ByteArray>()
        val opened = mutableListOf<Pair<String, String>>()

        override val locationLabel = "Downloads/ExpenseTrackr"

        override suspend fun writeText(fileName: String, content: String) =
            writeBytes(fileName, content.encodeToByteArray(), "text/csv")

        override suspend fun readText(fileName: String): String? = null

        override suspend fun writeBytes(fileName: String, bytes: ByteArray, mimeType: String): Boolean {
            if (!writeSucceeds) return false
            written[fileName] = bytes
            return true
        }

        override suspend fun openFile(fileName: String, mimeType: String): FileOpenResult {
            if (openThrows) throw IllegalStateException("platform blew up")
            opened += fileName to mimeType
            return openResult
        }
    }

    private class FakeExpenseRepository(
        private val transactions: List<ExpenseWithDetails> = sample(),
        private val throws: Boolean = false,
    ) : ExpenseRepository {

        override fun getTransactionsWithDetails(filter: DateFilter): Flow<List<ExpenseWithDetails>> {
            if (throws) throw IllegalStateException("database unavailable")
            return flowOf(transactions)
        }

        override fun getExpensesWithDetails(filter: DateFilter) = flowOf(transactions)
        override fun getExpensesForCategory(categoryId: Long, filter: DateFilter) = flowOf(transactions)
        override fun getExpensesForCategoryAndSubCategory(
            categoryId: Long,
            subCategoryId: Long?,
            filter: DateFilter,
        ) = flowOf(transactions)

        override suspend fun getExpenseById(id: Long): Result<Expense, DataError.Local> =
            Result.Error(DataError.Local.NOT_FOUND)

        override suspend fun getAllTransactions(): Result<List<Expense>, DataError.Local> =
            Result.Success(emptyList())

        override suspend fun insertExpense(expense: Expense): Result<Long, DataError.Local> =
            Result.Success(0L)

        override suspend fun updateExpense(expense: Expense): EmptyResult<DataError.Local> =
            Result.Success(Unit)

        override suspend fun deleteExpense(id: Long): EmptyResult<DataError.Local> =
            Result.Success(Unit)

        override fun getTotalSpend(filter: DateFilter) = flowOf(0.0)
        override fun getTotalIncome(filter: DateFilter) = flowOf(0.0)
        override fun getSpendByCategory(filter: DateFilter) = flowOf(emptyMap<Long, Double>())

        companion object {
            fun sample(): List<ExpenseWithDetails> {
                val timestamp = LocalDateTime(2026, 8, 1, 12, 0)
                return listOf(
                    ExpenseWithDetails(
                        id = 1,
                        name = "Groceries",
                        amount = 250.0,
                        category = Category(
                            name = "Food",
                            icon = "🍎",
                            colorHex = "#4CAF50",
                            createdAt = timestamp,
                        ),
                        subCategory = null,
                        notes = null,
                        type = TransactionType.EXPENSE,
                        expenseDate = timestamp,
                        createdAt = timestamp,
                    ),
                )
            }
        }
    }
}
