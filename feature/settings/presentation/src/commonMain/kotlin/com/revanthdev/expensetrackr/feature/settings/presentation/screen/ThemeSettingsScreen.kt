package com.revanthdev.expensetrackr.feature.settings.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.revanthdev.expensetrackr.core.designsystem.component.bounceClick
import expensetrackr.core.presentation.generated.resources.Res
import expensetrackr.core.presentation.generated.resources.action_back
import expensetrackr.core.presentation.generated.resources.settings_theme
import expensetrackr.core.presentation.generated.resources.settings_theme_dark
import expensetrackr.core.presentation.generated.resources.settings_theme_dark_desc
import expensetrackr.core.presentation.generated.resources.settings_theme_desc
import expensetrackr.core.presentation.generated.resources.settings_theme_light
import expensetrackr.core.presentation.generated.resources.settings_theme_light_desc
import expensetrackr.core.presentation.generated.resources.settings_theme_system
import expensetrackr.core.presentation.generated.resources.settings_theme_system_desc
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ThemeSettingsRoot(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    ThemeSettingsScreen(
        current = state.settings.isDarkMode,
        onSelect = { viewModel.onAction(SettingsAction.OnThemeChange(it)) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    current: Boolean?,
    onSelect: (Boolean?) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_theme)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, stringResource(Res.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SelectionHeader(
                emoji = "🎨",
                title = stringResource(Res.string.settings_theme),
                description = stringResource(Res.string.settings_theme_desc)
            )
            ThemeOption(
                icon = Icons.Rounded.BrightnessAuto,
                title = stringResource(Res.string.settings_theme_system),
                subtitle = stringResource(Res.string.settings_theme_system_desc),
                selected = current == null,
                onClick = { onSelect(null) }
            )
            ThemeOption(
                icon = Icons.Rounded.LightMode,
                title = stringResource(Res.string.settings_theme_light),
                subtitle = stringResource(Res.string.settings_theme_light_desc),
                selected = current == false,
                onClick = { onSelect(false) }
            )
            ThemeOption(
                icon = Icons.Rounded.DarkMode,
                title = stringResource(Res.string.settings_theme_dark),
                subtitle = stringResource(Res.string.settings_theme_dark_desc),
                selected = current == true,
                onClick = { onSelect(true) }
            )
        }
    }
}

@Composable
private fun ThemeOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow
    )
    Surface(
        modifier = Modifier.fillMaxWidth().bounceClick(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
