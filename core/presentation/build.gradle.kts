import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework { baseName = "CorePresentation"; isStatic = true }
    }
    jvm()
    androidLibrary {
        namespace = "com.revanthdev.expensetrackr.core.presentation"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
        // Required for the string-resource catalog to be packaged into the APK as Android assets.
        // The com.android.kotlin.multiplatform.library plugin disables Android resources by default.
        androidResources { enable = true }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(libs.bundles.compose)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewmodel)
        }
    }
}

// App-wide string resources live here (every feature, design-system and shared depend on
// core:presentation), exposed publicly so other modules can use them via `Res.string.*`.
// NOTE: do NOT set a custom packageOfResClass — on Android the physical resources are packaged
// under the default `expensetrackr.core.presentation.generated.resources` path, while a custom
// package only moves the generated Res class, causing a runtime MissingResourceException.
compose.resources {
    publicResClass = true
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
