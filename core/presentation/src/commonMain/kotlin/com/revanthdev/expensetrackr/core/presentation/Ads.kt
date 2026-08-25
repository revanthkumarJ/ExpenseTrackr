package com.revanthdev.expensetrackr.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/** Platform-provided ad surfaces. Empty defaults keep non-Android targets ad-free. */
val LocalBannerAd = staticCompositionLocalOf<@Composable () -> Unit> { {} }
val LocalNativeAd = staticCompositionLocalOf<@Composable () -> Unit> { {} }

fun interface RewardedActionGate {
    fun run(operation: String, action: () -> Unit)
}

val LocalRewardedActionGate = staticCompositionLocalOf<RewardedActionGate> {
    RewardedActionGate { _, action -> action() }
}
