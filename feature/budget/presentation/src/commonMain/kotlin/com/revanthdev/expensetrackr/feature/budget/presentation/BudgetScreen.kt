package com.revanthdev.expensetrackr.feature.budget.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.revanthdev.expensetrackr.core.designsystem.component.GradientIconTile
import com.revanthdev.expensetrackr.core.designsystem.theme.hexToColor
import com.revanthdev.expensetrackr.core.domain.model.AppSettings
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import com.revanthdev.expensetrackr.core.domain.repository.SettingsRepository
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import expensetrackr.core.presentation.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.revanthdev.expensetrackr.core.presentation.util.DecimalFormatter
import com.revanthdev.expensetrackr.core.presentation.util.DecimalInputVisualTransformation
import com.revanthdev.expensetrackr.core.presentation.util.toCurrencyString
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf

@Serializable
data object BudgetRoute

data class BudgetState(
    val overallBudgetEnabled: Boolean = false,
    val overallBudgetText: String = "",
    val allowExceedBudget: Boolean = true,
    val categories: List<Category> = emptyList(),
    val categoryBudgets: Map<Long, String> = emptyMap(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

sealed interface BudgetAction {
    data class OnOverallToggle(val enabled: Boolean) : BudgetAction
    data class OnOverallBudgetChange(val text: String) : BudgetAction
    data class OnAllowExceedToggle(val allow: Boolean) : BudgetAction
    data class OnCategoryBudgetChange(val categoryId: Long, val text: String) : BudgetAction
    data object OnSave : BudgetAction
    data object OnResetAll : BudgetAction
    data object OnBack : BudgetAction
}

sealed interface BudgetEvent {
    data object NavigateBack : BudgetEvent
}

class BudgetViewModel(
    private val settingsRepository: SettingsRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _state = MutableStateFlow(BudgetState())
    val state = _state.asStateFlow()
    private val _events = Channel<BudgetEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.getSettings(),
                categoryRepository.getAllCategories()
            ) { settings, cats ->
                BudgetState(
                    overallBudgetEnabled = settings.overallMonthlyBudget != null,
                    overallBudgetText = settings.overallMonthlyBudget?.let { "%.2f".format(it) } ?: "",
                    allowExceedBudget = settings.allowExceedBudget,
                    categories = cats,
                    categoryBudgets = cats.associate { cat ->
                        cat.id to (cat.budgetAmount?.let { "%.2f".format(it) } ?: "")
                    },
                    isLoading = false
                )
            }.collect { _state.value = it }
        }
    }

    fun onAction(action: BudgetAction) {
        when (action) {
            is BudgetAction.OnOverallToggle -> _state.update { it.copy(overallBudgetEnabled = action.enabled) }
            is BudgetAction.OnOverallBudgetChange -> _state.update { it.copy(overallBudgetText = action.text) }
            is BudgetAction.OnAllowExceedToggle -> _state.update { it.copy(allowExceedBudget = action.allow) }
            is BudgetAction.OnCategoryBudgetChange -> _state.update {
                it.copy(categoryBudgets = it.categoryBudgets + (action.categoryId to action.text))
            }
            BudgetAction.OnSave -> save()
            BudgetAction.OnResetAll -> resetAll()
            BudgetAction.OnBack -> viewModelScope.launch { _events.send(BudgetEvent.NavigateBack) }
        }
    }

    private fun save() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val s = _state.value
            val settings = settingsRepository.getSettingsOnce()
            val newBudget = if (s.overallBudgetEnabled) s.overallBudgetText.toDoubleOrNull() else null
            settingsRepository.updateSettings(
                settings.copy(overallMonthlyBudget = newBudget, allowExceedBudget = s.allowExceedBudget)
            )
            s.categories.forEach { cat ->
                val budgetText = s.categoryBudgets[cat.id] ?: ""
                val budgetAmount = if (budgetText.isBlank()) null else budgetText.toDoubleOrNull()
                categoryRepository.updateCategory(cat.copy(budgetAmount = budgetAmount))
            }
            _state.update { it.copy(isSaving = false) }
            _events.send(BudgetEvent.NavigateBack)
        }
    }

    private fun resetAll() {
        viewModelScope.launch {
            val settings = settingsRepository.getSettingsOnce()
            settingsRepository.updateSettings(settings.copy(overallMonthlyBudget = null))
            _state.value.categories.forEach { cat ->
                categoryRepository.updateCategory(cat.copy(budgetAmount = null))
            }
            _state.update { it.copy(overallBudgetEnabled = false, overallBudgetText = "", categoryBudgets = emptyMap()) }
        }
    }
}

@Composable
fun BudgetRoot(onNavigateBack: () -> Unit, viewModel: BudgetViewModel = koinViewModel()) {
    ObserveAsEvents(viewModel.events) { when (it) { BudgetEvent.NavigateBack -> onNavigateBack() } }
    val state by viewModel.state.collectAsState()
    BudgetScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(state: BudgetState, onAction: (BudgetAction) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.budget_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(BudgetAction.OnBack) }) {
                        Icon(Icons.Rounded.ArrowBack, stringResource(Res.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().imePadding()) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(Res.string.budget_monthly), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Switch(checked = state.overallBudgetEnabled, onCheckedChange = { onAction(BudgetAction.OnOverallToggle(it)) })
                        }
                        if (state.overallBudgetEnabled) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = state.overallBudgetText,
                                onValueChange = { onAction(BudgetAction.OnOverallBudgetChange(it)) },
                                label = { Text(stringResource(Res.string.budget_amount)) },
                                prefix = { Text("₹") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                visualTransformation = DecimalInputVisualTransformation(DecimalFormatter()),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(stringResource(Res.string.budget_allow_over_title), style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        stringResource(if (state.allowExceedBudget) Res.string.budget_allow_over_on else Res.string.budget_allow_over_off),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Switch(
                                    checked = state.allowExceedBudget,
                                    onCheckedChange = { onAction(BudgetAction.OnAllowExceedToggle(it)) }
                                )
                            }
                        }
                    }
                }
            }
            item { Text(stringResource(Res.string.budget_per_category), style = MaterialTheme.typography.titleMedium) }
            items(state.categories, key = { it.id }) { cat ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.animateItem()
                ) {
                    GradientIconTile(cat.icon, hexToColor(cat.colorHex), size = 40)
                    Text(cat.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = state.categoryBudgets[cat.id] ?: "",
                        onValueChange = { onAction(BudgetAction.OnCategoryBudgetChange(cat.id, it)) },
                        placeholder = { Text("0") },
                        prefix = { Text("₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        visualTransformation = DecimalInputVisualTransformation(DecimalFormatter()),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.width(130.dp)
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            OutlinedButton(onClick = { onAction(BudgetAction.OnResetAll) }, modifier = Modifier.weight(1f)) {
                Text(stringResource(Res.string.budget_reset_all))
            }
            Button(
                onClick = { onAction(BudgetAction.OnSave) },
                modifier = Modifier.weight(1f),
                enabled = !state.isSaving
            ) {
                if (state.isSaving) CircularProgressIndicator(Modifier.size(20.dp))
                else Text(stringResource(Res.string.action_save))
            }
        }
        }
    }
}

val budgetModule = org.koin.dsl.module {
    viewModelOf(::BudgetViewModel)
}
