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
    // Ensure to include the sourceSets
    sourceSets {
        getByName("main") {
            kotlin.srcDir("src/main/kotlin")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
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

fun getPropertyValue(propertyName: String): String {
    val envValue = System.getenv(propertyName)
    if (envValue != null) {
        return envValue
    }
    val localProperties = gradleLocalProperties(rootDir, providers)
    return localProperties.getProperty(propertyName) ?: ""
}

dependencies {
    // Paydock Modules (Libs)
    api(libs.paydock.core.networking)
    // Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.sdk)
    implementation(libs.compose.webview)
    // Kotlin
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlinx.html.jvm)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlinx.coroutines.test)
    // Coroutines
    implementation(libs.kotlinx.coroutines)
    // Koin - Injection
    implementation(libs.bundles.koin)
    testImplementation(libs.koin.test)
    // Ktor - Networking
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.okhttp)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.okhttp3.mockwebserver)
    // Google Services
    implementation(libs.bundles.google.pay.services)
    // Afterpay SDK
    implementation(libs.afterpay.android)
    // PayPal SDK
    implementation(libs.paypal.web.payments)
    implementation(libs.paypal.fraud.protection)
    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.mockito)
    testImplementation(libs.turbine)
    // UI Testing
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4.android)
    androidTestImplementation(libs.koin.test)
    androidTestImplementation(libs.mockk.android) {
        exclude(group = "io.mockk", module = "mockk-agent-android")
    }
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}