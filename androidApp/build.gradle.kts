import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

// Release signing credentials live in androidApp/keystore.properties, which is gitignored
// (this is a public repo — never commit the keystore or its passwords). Copy
// keystore.properties.template to keystore.properties and fill it in to build a signed release.
val keystorePropsFile = file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
}

kotlin {
    compilerOptions { jvmTarget = JvmTarget.JVM_11 }
}

android {
    namespace = "com.revanthdev.expensetrackr"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.revanthdev.expensetrackr"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 4
        versionName = "1.0.3"
    }
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
    signingConfigs {
        create("release") {
            if (keystorePropsFile.exists()) {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }
    buildTypes {
        getByName("release") {
            // Shrink + obfuscate to reduce app size. Keep rules live in proguard-rules.pro
            // (critical for kotlinx.serialization-based navigation routes).
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use the real release key when keystore.properties is present; otherwise fall back
            // to debug signing so the project still builds (that build is NOT uploadable to Play).
            signingConfig = if (keystorePropsFile.exists())
                signingConfigs.getByName("release")
            else
                signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.koin.android)
    implementation(libs.androidx.biometric)
    implementation(libs.kotlinx.coroutinesAndroid)

    // Firebase (Android-only): Crashlytics + Analytics. Versions managed by the BOM.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:presentation"))
    implementation(project(":core:design-system"))
    implementation(project(":core:database"))
    implementation(project(":feature:onboarding:presentation"))
    implementation(project(":feature:applock:presentation"))
    implementation(project(":feature:dashboard:presentation"))
    implementation(project(":feature:expenses:presentation"))
    implementation(project(":feature:analytics:presentation"))
    implementation(project(":feature:budget:presentation"))
    implementation(project(":feature:categories:presentation"))
    implementation(project(":feature:settings:presentation"))
}
