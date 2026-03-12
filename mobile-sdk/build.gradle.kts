import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    // linting
    id("detekt-convention")
    // publishing
    id("github-publish-convention")
    id("maven-central-publish-convention")
    // test coverage
    id("test-coverage-convention")
    // dependency validation
    id("dependency-analysis-convention")
    alias(libs.plugins.dependency.guard)
}

android {
    namespace = "com.paydock"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("release") {
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
    testOptions {
        unitTests.isReturnDefaultValues = true
        // Disable window animations during instrumented tests (command-line) to reduce flakiness
        animationsDisabled = true
    }
    // Gradle Managed Devices for instrumentation tests (CI-friendly aosp-atd image)
    testOptions.managedDevices {
        devices {
            maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel6Api35").apply {
                device = "Pixel 6"
                apiLevel = 35
                systemImageSource = "aosp-atd"
            }
        }
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

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // Paydock Modules (Libs)
    api(libs.paydock.core.networking)
    // BIN Processor Module
    implementation(project(":bin-processor"))
    // Android
    implementation(libs.bundles.androidx)
    implementation(libs.androidx.lifecycle.ktx)
    implementation(libs.androidx.lifecycle.process)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.test.espresso.accessibility)
    // Compose - BOM 2025.06.01 (Compose 1.8.3)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.composeDebug)
    androidTestImplementation(libs.androidx.ui.test.junit4.android)
    androidTestRuntimeOnly(libs.androidx.ui.test.manifest)
    // Kotlin
    implementation(platform(libs.kotlin.bom))
    implementation(libs.bundles.kotlin)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    // Coroutines
    implementation(libs.bundles.coroutins)
    // Koin - Dependency Injection
    implementation(libs.bundles.koin)
    testImplementation(libs.koin.test)
    androidTestImplementation(libs.koin.test)
    // Google Services
    implementation(libs.bundles.google.pay.services)
    // Google Pay SDK
    api(libs.google.compose.pay.button)
    // Afterpay SDK
    api(libs.afterpay.android)
    // PayPal SDK
    // Expose only the button types to consumers; keep the rest internal to the SDK
    api(libs.paypal.payment.buttons)
    implementation(libs.bundles.paypal)
    // Logging (runtime only)
    runtimeOnly(libs.slf4j.jdk14)
    // Unit Testing (General)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.mockito)
    testImplementation(libs.turbine)
    testImplementation(libs.json)
    testImplementation(libs.androidx.arch.core.testing)
    // UI Testing (General)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockito.android)
}

// Dependency Guard Configuration
// Detects transitive dependency version changes
dependencyGuard {
    // Monitor production runtime classpath for version changes
    configuration("releaseRuntimeClasspath") {
        // Track both modules and artifacts for comprehensive detection
        modules = true
        artifacts = true
    }
}