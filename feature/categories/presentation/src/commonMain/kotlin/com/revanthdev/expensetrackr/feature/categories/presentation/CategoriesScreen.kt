package com.revanthdev.expensetrackr.feature.categories.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanthdev.expensetrackr.core.designsystem.theme.hexToColor
import com.revanthdev.expensetrackr.core.domain.model.Category
import com.revanthdev.expensetrackr.core.domain.model.SubCategory
import com.revanthdev.expensetrackr.core.domain.repository.CategoryRepository
import com.revanthdev.expensetrackr.core.presentation.ObserveAsEvents
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf

@Serializable
data object ManageCategoriesRoute

@Serializable
data object ManageSubCategoriesRoute

// ---- Manage Categories ----

data class ManageCategoriesState(
    val categories: List<Category> = emptyList(),
    val showAddDialog: Boolean = false,
    val showEditDialog: Category? = null,
    val newName: String = "",
    val newIcon: String = "📦",
    val deleteError: String? = null
)

sealed interface ManageCategoriesAction {
    data object OnAddClick : ManageCategoriesAction
    data class OnEditClick(val category: Category) : ManageCategoriesAction
    data class OnDeleteClick(val category: Category) : ManageCategoriesAction
    data class OnNameChange(val name: String) : ManageCategoriesAction
    data class OnIconChange(val icon: String) : ManageCategoriesAction
    data object OnSaveNew : ManageCategoriesAction
    data object OnSaveEdit : ManageCategoriesAction
    data object OnDismissDialog : ManageCategoriesAction
    data object OnBack : ManageCategoriesAction
}

sealed interface ManageCategoriesEvent {
    data object NavigateBack : ManageCategoriesEvent
}

class ManageCategoriesViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {
    private val _state = MutableStateFlow(ManageCategoriesState())
    val state = _state.asStateFlow()
    private val _events = Channel<ManageCategoriesEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { cats ->
                _state.update { it.copy(categories = cats) }
            }
        }
    }

    fun onAction(action: ManageCategoriesAction) {
        when (action) {
            ManageCategoriesAction.OnAddClick -> _state.update { it.copy(showAddDialog = true, newName = "", newIcon = "📦") }
            is ManageCategoriesAction.OnEditClick -> _state.update { it.copy(showEditDialog = action.category, newName = action.category.name, newIcon = action.category.icon) }
            is ManageCategoriesAction.OnDeleteClick -> deleteCategory(action.category)
            is ManageCategoriesAction.OnNameChange -> _state.update { it.copy(newName = action.name) }
            is ManageCategoriesAction.OnIconChange -> _state.update { it.copy(newIcon = action.icon) }
            ManageCategoriesAction.OnSaveNew -> saveNew()
            ManageCategoriesAction.OnSaveEdit -> saveEdit()
            ManageCategoriesAction.OnDismissDialog -> _state.update { it.copy(showAddDialog = false, showEditDialog = null) }
            ManageCategoriesAction.OnBack -> viewModelScope.launch { _events.send(ManageCategoriesEvent.NavigateBack) }
        }
    }

    private fun saveNew() {
        val s = _state.value
        if (s.newName.isBlank()) return
        viewModelScope.launch {
            val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            categoryRepository.insertCategory(Category(name = s.newName.trim(), icon = s.newIcon, colorHex = "#616161", createdAt = now))
            _state.update { it.copy(showAddDialog = false) }
        }
    }

    private fun saveEdit() {
        val s = _state.value
        val editing = s.showEditDialog ?: return
        if (s.newName.isBlank()) return
        viewModelScope.launch {
            categoryRepository.updateCategory(editing.copy(name = s.newName.trim(), icon = s.newIcon))
            _state.update { it.copy(showEditDialog = null) }
        }
    }

    private fun deleteCategory(category: Category) {
        viewModelScope.launch {
            if (categoryRepository.hasExpensesForCategory(category.id)) {
                _state.update { it.copy(deleteError = "Cannot delete '${category.name}' — it has linked expenses.") }
            } else {
                categoryRepository.deleteCategory(category.id)
            }
        }
    }
}

@Composable
fun ManageCategoriesRoot(onNavigateBack: () -> Unit, viewModel: ManageCategoriesViewModel = koinViewModel()) {
    ObserveAsEvents(viewModel.events) { when (it) { ManageCategoriesEvent.NavigateBack -> onNavigateBack() } }
    val state by viewModel.state.collectAsState()
    ManageCategoriesScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(state: ManageCategoriesState, onAction: (ManageCategoriesAction) -> Unit) {
    if (state.showAddDialog || state.showEditDialog != null) {
        AlertDialog(
            onDismissRequest = { onAction(ManageCategoriesAction.OnDismissDialog) },
            title = { Text(if (state.showAddDialog) "New Category" else "Edit Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.newName,
                        onValueChange = { onAction(ManageCategoriesAction.OnNameChange(it)) },
                        label = { Text("Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.newIcon,
                        onValueChange = { if (it.length <= 2) onAction(ManageCategoriesAction.OnIconChange(it)) },
                        label = { Text("Icon (emoji)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { if (state.showAddDialog) onAction(ManageCategoriesAction.OnSaveNew) else onAction(ManageCategoriesAction.OnSaveEdit) }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { onAction(ManageCategoriesAction.OnDismissDialog) }) { Text("Cancel") } }
        )
    }

    state.deleteError?.let { error ->
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Cannot Delete") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = { }) { Text("OK") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories") },
                navigationIcon = { IconButton(onClick = { onAction(ManageCategoriesAction.OnBack) }) { Icon(Icons.Rounded.ArrowBack, "Back") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAction(ManageCategoriesAction.OnAddClick) }) {
                Icon(Icons.Rounded.Add, "Add")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.categories, key = { it.id }) { cat ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(cat.icon, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(12.dp))
                        Text(cat.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                        if (!cat.isDefault) {
                            IconButton(onClick = { onAction(ManageCategoriesAction.OnEditClick(cat)) }) { Icon(Icons.Rounded.Edit, "Edit") }
                            IconButton(onClick = { onAction(ManageCategoriesAction.OnDeleteClick(cat)) }) { Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
                        } else {
                            Text("Default", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

val categoriesModule = org.koin.dsl.module {
    viewModelOf(::ManageCategoriesViewModel)
}
