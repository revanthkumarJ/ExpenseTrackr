import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework { baseName = "CoreData"; isStatic = true }
    }
    jvm()
    androidLibrary {
        namespace = "com.revanthdev.expensetrackr.core.data"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:database"))
            implementation(libs.datastore.preferencesCore)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serializationJson)
            implementation(libs.kermit)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.datastore.preferences)
            implementation(libs.kotlinx.coroutinesAndroid)
            implementation(libs.koin.android)
        }
    }
}
