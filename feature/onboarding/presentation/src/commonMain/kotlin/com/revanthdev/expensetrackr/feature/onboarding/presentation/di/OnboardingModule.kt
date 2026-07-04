package com.revanthdev.expensetrackr.feature.onboarding.presentation

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingModule = module {
    viewModelOf(::OnboardingViewModel)
}
