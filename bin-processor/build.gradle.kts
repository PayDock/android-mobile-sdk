import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    // linting
    id("detekt-convention")
    // test coverage
    id("test-coverage-convention")
}

android {
    namespace = "com.paydock.binprocessor"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

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
    // Paydock Core Networking (Ktor HTTP Client)
    api(libs.paydock.core.networking)

    // Kotlin
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlinx.serialization.json)

    // Coroutines (core only; no Play Services - module doesn't use Play APIs)
    implementation(libs.kotlinx.coroutines)

    // Koin - Dependency Injection (core + android only; no Compose - module has no UI)
    implementation(libs.koin)
    implementation(libs.koin.android)

    // AndroidX Lifecycle (for ProcessLifecycleOwner)
    implementation(libs.androidx.lifecycle.process)

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.mockito)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.koin.test)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.arch.core.testing)
}
