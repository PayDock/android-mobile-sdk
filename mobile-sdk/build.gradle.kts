import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    // linting
    id("detekt-convention")
    // publishing
    id("github-publish-convention")
}

android {
    namespace = "com.paydock"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("boolean", "DEV_BUILD", "true")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("release") {
            buildConfigField("boolean", "DEV_BUILD", "false")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    composeOptions {
        // https://developer.android.com/jetpack/androidx/releases/compose-compiler
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    testOptions.unitTests.all {
        it.jvmArgs(
            "--add-opens",
            "java.base/java.lang=ALL-UNNAMED",
            "--add-opens",
            "java.base/java.lang.reflect=ALL-UNNAMED"
        )
    }
    packaging {
        resources {
            excludes += "/META-INF/{LICENSE.md,LICENSE-notice.md}"
        }
    }
}

publishingConfig {
    groupId = "com.paydock"
    version = "1.1.0"
    artifactId = "mobile-sdk"
    projectId = "52671182"
    projectGithubUrl = "https://github.com/PayDock/android-mobile-sdk"
    projectDescription = "The Paydock Mobile Android SDK provides an easy way to build and integrate with the Paydock " +
        "orchestration platform for an Android app. We provide powerful and customizable UI elements that can be used " +
        "out-of-the-box to collect your users' payment details. We also expose the low-level APIs that power those UIs so " +
        "that you can build fully custom experiences."
    packagingOption = "aar"
    includeSources = false // This is included by default for Android libraries
}

fun getPropertyValue(propertyName: String): String {
    val envValue = System.getenv(propertyName)
    if (envValue != null) {
        return envValue
    }
    val localProperties = gradleLocalProperties(rootDir, providers)
    return localProperties.getProperty(propertyName) ?: ""
}

dependencies {
    // Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.sdk)
    implementation(libs.compose.webview)
    debugImplementation(libs.bundles.compose.debug)
    // Kotlin
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlinx.html.jvm)
    testImplementation(libs.kotlinx.coroutines.test)
    // Coroutines
    implementation(libs.kotlinx.coroutines)
    // Koin - Injection
    implementation(libs.bundles.koin)
    testImplementation(libs.koin.test)
    // Ktor - Networking
    implementation(libs.bundles.ktor)
    implementation(libs.okhttp3.logging)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.okhttp3.mockwebserver)
    // Google Services
    implementation(libs.bundles.google.pay.services)
    // Afterpay SDK
    implementation(libs.afterpay.android)
    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.mockito)
    testImplementation(libs.turbine)
    // UI Testing
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit)
    androidTestImplementation(libs.koin.test)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}