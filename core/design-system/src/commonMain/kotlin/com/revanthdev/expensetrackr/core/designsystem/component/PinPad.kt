package com.revanthdev.expensetrackr.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * The shared PIN-entry experience: an emoji medallion, [pinLength] dots, an error line and a
 * springy numeric keypad. The parent owns the [pin] string and decides what happens when it is
 * complete (e.g. verify, advance to a confirm step, etc).
 *
 * Pass [onBiometric] (with [biometricIcon]) only where biometric makes sense — i.e. the unlock
 * screen. During PIN setup / confirmation leave them null so the biometric key is hidden.
 */
@Composable
fun PinEntryScreen(
    title: String,
    pin: String,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    error: String? = null,
    pinLength: Int = 6,
    emoji: String = "🔒",
    biometricIcon: ImageVector? = null,
    onBiometric: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, style = MaterialTheme.typography.displaySmall)
        }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            repeat(pinLength) { i ->
                val filled = i < pin.length
                val dotScale by animateFloatAsState(if (filled) 1f else 0.6f, label = "dot")
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { scaleX = dotScale; scaleY = dotScale }
                        .clip(CircleShape)
                        .background(
                            if (filled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
                        )
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            error ?: " ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9")).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { key -> KeypadKey(key, Modifier.weight(1f)) { onDigit(key) } }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                    if (biometricIcon != null && onBiometric != null) {
                        KeypadIconKey(biometricIcon, Modifier.fillMaxSize(), onBiometric)
                    }
                }
                KeypadKey("0", Modifier.weight(1f)) { onDigit("0") }
                KeypadKey("⌫", Modifier.weight(1f), onBackspace)
            }
        }
    }
}

@Composable
private fun KeypadKey(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.aspectRatio(1f).bounceClick(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun KeypadIconKey(icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.aspectRatio(1f).bounceClick(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = "Biometric", tint = MaterialTheme.colorScheme.primary)
        }
    }
}
