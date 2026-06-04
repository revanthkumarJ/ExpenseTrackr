pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ExpenseTrackr"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":androidApp")
include(":desktopApp")
include(":shared")
include(":core:domain")
include(":core:database")
include(":core:data")
include(":core:presentation")
include(":core:design-system")
include(":feature:onboarding:presentation")
include(":feature:applock:presentation")
include(":feature:dashboard:presentation")
include(":feature:expenses:presentation")
include(":feature:analytics:presentation")
include(":feature:budget:presentation")
include(":feature:categories:presentation")
include(":feature:settings:presentation")
