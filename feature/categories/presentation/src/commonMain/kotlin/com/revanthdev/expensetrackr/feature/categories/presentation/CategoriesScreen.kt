package com.revanthdev.expensetrackr.feature.categories.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.revanthdev.expensetrackr.core.designsystem.component.EmptyState
import com.revanthdev.expensetrackr.core.designsystem.component.GradientIconTile
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
            ExtendedFloatingActionButton(
                onClick = { onAction(ManageCategoriesAction.OnAddClick) },
                icon = { Icon(Icons.Rounded.Add, "Add") },
                text = { Text("New") }
            )
        }
    ) { padding ->
        if (state.categories.isEmpty()) {
            EmptyState(
                modifier = Modifier.padding(padding),
                title = "No categories",
                message = "Tap New to create your first category",
                emoji = "🗂️"
            )
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.categories, key = { it.id }) { cat ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        tonalElevation = 1.dp
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            GradientIconTile(cat.icon, hexToColor(cat.colorHex), size = 44)
                            Spacer(Modifier.width(12.dp))
                            Text(cat.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                            if (!cat.isDefault) {
                                IconButton(onClick = { onAction(ManageCategoriesAction.OnEditClick(cat)) }) { Icon(Icons.Rounded.Edit, "Edit") }
                                IconButton(onClick = { onAction(ManageCategoriesAction.OnDeleteClick(cat)) }) { Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
                            } else {
                                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                    Text("Default", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---- Manage Sub-Categories ----

data class ManageSubCategoriesState(
    val subCategories: List<SubCategory> = emptyList(),
    val categories: List<Category> = emptyList(),
    val showAddDialog: Boolean = false,
    val showEditDialog: SubCategory? = null,
    val newName: String = "",
    val newCategoryId: Long = 0L,
    val showDetachDialog: SubCategory? = null,
    val deleteError: String? = null
)

sealed interface ManageSubCategoriesAction {
    data object OnAddClick : ManageSubCategoriesAction
    data class OnEditClick(val subCategory: SubCategory) : ManageSubCategoriesAction
    data class OnDeleteClick(val subCategory: SubCategory) : ManageSubCategoriesAction
    data class OnNameChange(val name: String) : ManageSubCategoriesAction
    data class OnCategoryChange(val categoryId: Long) : ManageSubCategoriesAction
    data object OnSaveNew : ManageSubCategoriesAction
    data object OnSaveEdit : ManageSubCategoriesAction
    data object OnDismissDialog : ManageSubCategoriesAction
    data object OnDetachConfirm : ManageSubCategoriesAction
    data object OnDetachDismiss : ManageSubCategoriesAction
    data object OnBack : ManageSubCategoriesAction
}

sealed interface ManageSubCategoriesEvent {
    data object NavigateBack : ManageSubCategoriesEvent
}

class ManageSubCategoriesViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {
    private val _state = MutableStateFlow(ManageSubCategoriesState())
    val state = _state.asStateFlow()
    private val _events = Channel<ManageSubCategoriesEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                categoryRepository.getAllSubCategories(),
                categoryRepository.getAllCategories()
            ) { subs, cats -> subs to cats }.collect { (subs, cats) ->
                _state.update { it.copy(subCategories = subs, categories = cats) }
            }
        }
    }

    fun onAction(action: ManageSubCategoriesAction) {
        when (action) {
            ManageSubCategoriesAction.OnAddClick -> {
                val firstCatId = _state.value.categories.firstOrNull()?.id ?: 0L
                _state.update { it.copy(showAddDialog = true, newName = "", newCategoryId = firstCatId) }
            }
            is ManageSubCategoriesAction.OnEditClick -> _state.update {
                it.copy(showEditDialog = action.subCategory, newName = action.subCategory.name, newCategoryId = action.subCategory.categoryId)
            }
            is ManageSubCategoriesAction.OnDeleteClick -> checkAndDelete(action.subCategory)
            is ManageSubCategoriesAction.OnNameChange -> _state.update { it.copy(newName = action.name) }
            is ManageSubCategoriesAction.OnCategoryChange -> _state.update { it.copy(newCategoryId = action.categoryId) }
            ManageSubCategoriesAction.OnSaveNew -> saveNew()
            ManageSubCategoriesAction.OnSaveEdit -> saveEdit()
            ManageSubCategoriesAction.OnDismissDialog -> _state.update { it.copy(showAddDialog = false, showEditDialog = null) }
            ManageSubCategoriesAction.OnDetachConfirm -> detachAndDelete()
            ManageSubCategoriesAction.OnDetachDismiss -> _state.update { it.copy(showDetachDialog = null) }
            ManageSubCategoriesAction.OnBack -> viewModelScope.launch { _events.send(ManageSubCategoriesEvent.NavigateBack) }
        }
    }

    private fun saveNew() {
        val s = _state.value
        if (s.newName.isBlank() || s.newCategoryId == 0L) return
        viewModelScope.launch {
            val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            categoryRepository.insertSubCategory(SubCategory(name = s.newName.trim(), categoryId = s.newCategoryId, createdAt = now))
            _state.update { it.copy(showAddDialog = false) }
        }
    }

    private fun saveEdit() {
        val s = _state.value
        val editing = s.showEditDialog ?: return
        if (s.newName.isBlank() || s.newCategoryId == 0L) return
        viewModelScope.launch {
            categoryRepository.updateSubCategory(editing.copy(name = s.newName.trim(), categoryId = s.newCategoryId))
            _state.update { it.copy(showEditDialog = null) }
        }
    }

    private fun checkAndDelete(subCategory: SubCategory) {
        viewModelScope.launch {
            if (categoryRepository.hasExpensesForSubCategory(subCategory.id)) {
                _state.update { it.copy(showDetachDialog = subCategory) }
            } else {
                categoryRepository.deleteSubCategory(subCategory.id)
            }
        }
    }

    private fun detachAndDelete() {
        val sub = _state.value.showDetachDialog ?: return
        viewModelScope.launch {
            categoryRepository.detachExpensesFromSubCategory(sub.id)
            categoryRepository.deleteSubCategory(sub.id)
            _state.update { it.copy(showDetachDialog = null) }
        }
    }
}

@Composable
fun ManageSubCategoriesRoot(onNavigateBack: () -> Unit, viewModel: ManageSubCategoriesViewModel = koinViewModel()) {
    ObserveAsEvents(viewModel.events) { when (it) { ManageSubCategoriesEvent.NavigateBack -> onNavigateBack() } }
    val state by viewModel.state.collectAsState()
    ManageSubCategoriesScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSubCategoriesScreen(state: ManageSubCategoriesState, onAction: (ManageSubCategoriesAction) -> Unit) {
    var showCategoryDropdown by remember { mutableStateOf(false) }

    if (state.showAddDialog || state.showEditDialog != null) {
        AlertDialog(
            onDismissRequest = { onAction(ManageSubCategoriesAction.OnDismissDialog) },
            title = { Text(if (state.showAddDialog) "New Sub-Category" else "Edit Sub-Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.newName,
                        onValueChange = { onAction(ManageSubCategoriesAction.OnNameChange(it)) },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenuBox(
                        expanded = showCategoryDropdown,
                        onExpandedChange = { showCategoryDropdown = it }
                    ) {
                        val selectedCat = state.categories.find { it.id == state.newCategoryId }
                        OutlinedTextField(
                            value = selectedCat?.let { "${it.icon} ${it.name}" } ?: "Select Category",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Parent Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showCategoryDropdown) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = showCategoryDropdown, onDismissRequest = { showCategoryDropdown = false }) {
                            state.categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text("${cat.icon} ${cat.name}") },
                                    onClick = {
                                        onAction(ManageSubCategoriesAction.OnCategoryChange(cat.id))
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { if (state.showAddDialog) onAction(ManageSubCategoriesAction.OnSaveNew) else onAction(ManageSubCategoriesAction.OnSaveEdit) }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { onAction(ManageSubCategoriesAction.OnDismissDialog) }) { Text("Cancel") } }
        )
    }

    state.showDetachDialog?.let { sub ->
        AlertDialog(
            onDismissRequest = { onAction(ManageSubCategoriesAction.OnDetachDismiss) },
            title = { Text("Sub-Category Has Expenses") },
            text = { Text("'${sub.name}' has linked expenses. Deleting it will remove the sub-category from those expenses. Continue?") },
            confirmButton = {
                TextButton(onClick = { onAction(ManageSubCategoriesAction.OnDetachConfirm) }) { Text("Delete & Detach", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { onAction(ManageSubCategoriesAction.OnDetachDismiss) }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Sub-Categories") },
                navigationIcon = { IconButton(onClick = { onAction(ManageSubCategoriesAction.OnBack) }) { Icon(Icons.Rounded.ArrowBack, "Back") } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction(ManageSubCategoriesAction.OnAddClick) },
                icon = { Icon(Icons.Rounded.Add, "Add") },
                text = { Text("New") }
            )
        }
    ) { padding ->
        if (state.subCategories.isEmpty()) {
            EmptyState(
                modifier = Modifier.padding(padding),
                title = "No sub-categories yet",
                message = "Tap New to create one",
                emoji = "🏷️"
            )
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.subCategories, key = { it.id }) { sub ->
                    val parentCat = state.categories.find { it.id == sub.categoryId }
                    Surface(
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        tonalElevation = 1.dp
                    ) {
                        Row(modifier = Modifier.padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sub.name, style = MaterialTheme.typography.titleSmall)
                                if (parentCat != null) {
                                    Text("${parentCat.icon} ${parentCat.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            IconButton(onClick = { onAction(ManageSubCategoriesAction.OnEditClick(sub)) }) { Icon(Icons.Rounded.Edit, "Edit") }
                            IconButton(onClick = { onAction(ManageSubCategoriesAction.OnDeleteClick(sub)) }) { Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }
    }
}

val categoriesModule = org.koin.dsl.module {
    viewModelOf(::ManageCategoriesViewModel)
    viewModelOf(::ManageSubCategoriesViewModel)
}
