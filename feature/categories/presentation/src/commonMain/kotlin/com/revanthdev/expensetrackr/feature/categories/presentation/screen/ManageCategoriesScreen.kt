package com.revanthdev.expensetrackr.feature.categories.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.component.EmptyState
import com.revanthdev.expensetrackr.core.domain.model.TransactionType
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_add
import expensetrackr.core.presentation.generated.resources.action_back
import expensetrackr.core.presentation.generated.resources.action_new
import expensetrackr.core.presentation.generated.resources.categories_empty_message
import expensetrackr.core.presentation.generated.resources.categories_empty_title
import expensetrackr.core.presentation.generated.resources.categories_title
import expensetrackr.core.presentation.generated.resources.type_expense
import expensetrackr.core.presentation.generated.resources.type_income
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(state: ManageCategoriesState, onAction: (ManageCategoriesAction) -> Unit) {
    if (state.showAddDialog || state.showEditDialog != null) {
        CategoryEditDialog(
            isNew = state.showAddDialog,
            name = state.newName,
            icon = state.newIcon,
            onNameChange = { onAction(ManageCategoriesAction.OnNameChange(it)) },
            onIconChange = { onAction(ManageCategoriesAction.OnIconChange(it)) },
            onSave = { if (state.showAddDialog) onAction(ManageCategoriesAction.OnSaveNew) else onAction(ManageCategoriesAction.OnSaveEdit) },
            onDismiss = { onAction(ManageCategoriesAction.OnDismissDialog) }
        )
    }

    state.deleteError?.let { categoryName ->
        CannotDeleteCategoryDialog(
            categoryName = categoryName,
            onDismiss = { onAction(ManageCategoriesAction.OnDismissDialog) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.categories_title)) },
                navigationIcon = { IconButton(onClick = { onAction(ManageCategoriesAction.OnBack) }) { Icon(Icons.Rounded.ArrowBack, stringResource(Res.string.action_back)) } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction(ManageCategoriesAction.OnAddClick) },
                icon = { Icon(Icons.Rounded.Add, stringResource(Res.string.action_add)) },
                text = { Text(stringResource(Res.string.action_new)) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            val typeOptions = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                typeOptions.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = state.selectedType == type,
                        onClick = { onAction(ManageCategoriesAction.OnTypeTabChange(type)) },
                        shape = SegmentedButtonDefaults.itemShape(index, typeOptions.size)
                    ) {
                        Text(stringResource(if (type == TransactionType.INCOME) Res.string.type_income else Res.string.type_expense))
                    }
                }
            }

            if (state.categories.isEmpty()) {
                EmptyState(
                    title = stringResource(Res.string.categories_empty_title),
                    message = stringResource(Res.string.categories_empty_message),
                    emoji = "🗂️"
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.categories, key = { it.id }) { cat ->
                        CategoryRow(
                            category = cat,
                            onEdit = { onAction(ManageCategoriesAction.OnEditClick(cat)) },
                            onDelete = { onAction(ManageCategoriesAction.OnDeleteClick(cat)) },
                            modifier = Modifier.animateItem()
                        )
                    }
                    item {
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
