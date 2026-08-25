import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

// Release signing credentials live in androidApp/keystore.properties, which is gitignored
// (this is a public repo — never commit the keystore or its passwords). Copy
// keystore.properties.template to keystore.properties and fill it in to build a signed release.
val keystorePropsFile = file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}

// AdMob identifiers are machine/release configuration, not source code. Keep them in the
// gitignored root local.properties (see local.properties.example for the switchable layout).
val rootLocalProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) propertiesFile.inputStream().use { load(it) }
}
fun requiredLocalProperty(name: String): String =
    rootLocalProperties.getProperty(name)?.takeIf { it.isNotBlank() }
        ?: error("Missing $name in the gitignored root local.properties. See local.properties.example.")
fun buildConfigString(value: String): String = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

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
        versionCode = 8
        versionName = "1.0.7"
        resValue("string", "admob_app_id", requiredLocalProperty("ADMOB_APP_ID"))
        buildConfigField(
            "String",
            "ADMOB_BANNER_ID",
            buildConfigString(requiredLocalProperty("ADMOB_BANNER_ID")),
        )
        buildConfigField(
            "String",
            "ADMOB_NATIVE_ID",
            buildConfigString(requiredLocalProperty("ADMOB_NATIVE_ID")),
        )
        buildConfigField(
            "String",
            "ADMOB_REWARDED_ID",
            buildConfigString(requiredLocalProperty("ADMOB_REWARDED_ID")),
        )
    }
    // BuildConfig.VERSION_NAME / VERSION_CODE are the single source of truth for the version the
    // UI shows (see AppInfo / the About screen). AGP 8+ leaves this feature off by default.
    buildFeatures {
        buildConfig = true
        resValues = true
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
    // Play In-App Updates — backs PlayAppUpdateManager.
    implementation(libs.play.appUpdate)

    // Home-screen widget (Glance). Android-only by nature — widgets are a platform feature.
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
    // The widget reads spend totals directly, so it needs LocalDate/Clock for "today".
    implementation(libs.kotlinx.datetime)

    // Firebase (Android-only): Crashlytics + Analytics. Versions managed by the BOM.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // AdMob is Android-only. Debug/test integration uses Google's official sample IDs.
    implementation(libs.google.mobileAds)

    implementation(libs.compose.uiToolingPreview)
    implementation(libs.compose.foundation)
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
