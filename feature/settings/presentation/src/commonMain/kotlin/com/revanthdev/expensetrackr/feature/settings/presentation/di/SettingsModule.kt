package com.revanthdev.expensetrackr.feature.settings.presentation

import com.revanthdev.expensetrackr.feature.settings.presentation.sync.SyncViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::NotificationSettingsViewModel)
    viewModelOf(::AppLockSetupViewModel)
    viewModelOf(::SyncViewModel)
}
