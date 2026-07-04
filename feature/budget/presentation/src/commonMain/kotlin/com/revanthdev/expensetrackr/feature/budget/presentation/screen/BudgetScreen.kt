package com.revanthdev.expensetrackr.feature.budget.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_back
import expensetrackr.core.presentation.generated.resources.budget_per_category
import expensetrackr.core.presentation.generated.resources.budget_title
import org.jetbrains.compose.resources.stringResource

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
                    OverallBudgetCard(
                        enabled = state.overallBudgetEnabled,
                        budgetText = state.overallBudgetText,
                        allowExceedBudget = state.allowExceedBudget,
                        onToggle = { onAction(BudgetAction.OnOverallToggle(it)) },
                        onBudgetChange = { onAction(BudgetAction.OnOverallBudgetChange(it)) },
                        onAllowExceedToggle = { onAction(BudgetAction.OnAllowExceedToggle(it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item { Text(stringResource(Res.string.budget_per_category), style = MaterialTheme.typography.titleMedium) }
                items(state.categories, key = { it.id }) { cat ->
                    CategoryBudgetRow(
                        category = cat,
                        budgetText = state.categoryBudgets[cat.id] ?: "",
                        onBudgetChange = { onAction(BudgetAction.OnCategoryBudgetChange(cat.id, it)) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
            BudgetActionsBar(
                isSaving = state.isSaving,
                onReset = { onAction(BudgetAction.OnResetAll) },
                onSave = { onAction(BudgetAction.OnSave) }
            )
        }
    }
}
