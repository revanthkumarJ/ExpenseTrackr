import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    jvm()
    androidLibrary {
        namespace = "com.revanthdev.expensetrackr.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
        androidResources { enable = true }
        withHostTest { isIncludeAndroidResources = true }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:data"))
            implementation(project(":core:presentation"))
            implementation(project(":core:design-system"))
            implementation(project(":feature:onboarding:presentation"))
            implementation(project(":feature:applock:presentation"))
            implementation(project(":feature:dashboard:presentation"))
            implementation(project(":feature:expenses:presentation"))
            implementation(project(":feature:analytics:presentation"))
            implementation(project(":feature:budget:presentation"))
            implementation(project(":feature:categories:presentation"))
            implementation(project(":feature:settings:presentation"))
            implementation(libs.bundles.compose)
            implementation(compose.materialIconsExtended)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serializationJson)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewmodel)
        }
        commonTest.dependencies { implementation(libs.kotlin.test) }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
