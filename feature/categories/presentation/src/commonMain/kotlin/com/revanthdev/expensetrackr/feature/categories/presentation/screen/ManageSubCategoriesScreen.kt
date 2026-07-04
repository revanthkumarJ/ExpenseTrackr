package com.revanthdev.expensetrackr.feature.categories.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.component.EmptyState
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_add
import expensetrackr.core.presentation.generated.resources.action_back
import expensetrackr.core.presentation.generated.resources.action_new
import expensetrackr.core.presentation.generated.resources.subcategories_empty_message
import expensetrackr.core.presentation.generated.resources.subcategories_empty_title
import expensetrackr.core.presentation.generated.resources.subcategories_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSubCategoriesScreen(state: ManageSubCategoriesState, onAction: (ManageSubCategoriesAction) -> Unit) {
    if (state.showAddDialog || state.showEditDialog != null) {
        SubCategoryEditDialog(
            isNew = state.showAddDialog,
            name = state.newName,
            categories = state.categories,
            selectedCategoryId = state.newCategoryId,
            onNameChange = { onAction(ManageSubCategoriesAction.OnNameChange(it)) },
            onCategoryChange = { onAction(ManageSubCategoriesAction.OnCategoryChange(it)) },
            onSave = { if (state.showAddDialog) onAction(ManageSubCategoriesAction.OnSaveNew) else onAction(ManageSubCategoriesAction.OnSaveEdit) },
            onDismiss = { onAction(ManageSubCategoriesAction.OnDismissDialog) }
        )
    }

    state.showDetachDialog?.let { sub ->
        DetachSubCategoryDialog(
            subCategoryName = sub.name,
            onConfirm = { onAction(ManageSubCategoriesAction.OnDetachConfirm) },
            onDismiss = { onAction(ManageSubCategoriesAction.OnDetachDismiss) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.subcategories_title)) },
                navigationIcon = { IconButton(onClick = { onAction(ManageSubCategoriesAction.OnBack) }) { Icon(Icons.Rounded.ArrowBack, stringResource(Res.string.action_back)) } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction(ManageSubCategoriesAction.OnAddClick) },
                icon = { Icon(Icons.Rounded.Add, stringResource(Res.string.action_add)) },
                text = { Text(stringResource(Res.string.action_new)) }
            )
        }
    ) { padding ->
        if (state.subCategories.isEmpty()) {
            EmptyState(
                modifier = Modifier.padding(padding),
                title = stringResource(Res.string.subcategories_empty_title),
                message = stringResource(Res.string.subcategories_empty_message),
                emoji = "🏷️"
            )
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.subCategories, key = { it.id }) { sub ->
                    val parentCat = state.categories.find { it.id == sub.categoryId }
                    SubCategoryRow(
                        subCategory = sub,
                        parentCategory = parentCat,
                        onEdit = { onAction(ManageSubCategoriesAction.OnEditClick(sub)) },
                        onDelete = { onAction(ManageSubCategoriesAction.OnDeleteClick(sub)) },
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
