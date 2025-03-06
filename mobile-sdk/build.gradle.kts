plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    // linting
    id("detekt-convention")
    // publishing
    id("github-publish-convention")
}

android {
    namespace = "com.paydock"
    compileSdk = 35

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
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    composeOptions {
        // https://developer.android.com/jetpack/androidx/releases/compose-compiler
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtension.get()
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
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

dependencies {
    // Paydock Modules (Libs)
    api(libs.paydock.core.networking)
    // Android
    implementation(libs.bundles.androidx)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso.core)
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    // Release requires this to be included for previews
    implementation(libs.bundles.composeDebug)
    androidTestImplementation(libs.androidx.ui.test.junit4.android)
    androidTestImplementation(libs.androidx.ui.test.manifest)
    // Kotlin
    implementation(platform(libs.kotlin.bom))
    implementation(libs.bundles.kotlin)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    // Coroutines
    implementation(libs.bundles.coroutins)
    // Koin - Injection
    implementation(libs.bundles.koin)
    testImplementation(libs.koin.test)
    androidTestImplementation(libs.koin.test)
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
    implementation(libs.bundles.paypal)
    // Mocking
    implementation(libs.slf4j.jdk14)
    // Unit Testing (General)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.mockito)
    testImplementation(libs.turbine)
    testImplementation(libs.json)
    // UI Testing (General)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockito.android)
}