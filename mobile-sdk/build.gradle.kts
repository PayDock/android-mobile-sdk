import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
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
    // Held at 36 (not the latest 37) pending mobile-lib-networking-android validating AGP 9.x —
    // it's currently still pinned to AGP 8.13.0. Bumping this raises `minCompileSdk` in the
    // published AAR's metadata, forcing every integrator to also have that compileSdk available
    // just to keep compiling — not something to move ahead of schedule without a driving need.
    compileSdk = libs.versions.sdk.compileSdk.get().toInt()

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
            // TODO: TEMPORARY WORKAROUND
            // Including 'bin-processor' source code directly until it is published
            // as a standalone library to Maven/JitPack.
            // Link to issue: [Insert Jira/GitHub Link if applicable]
            // 'telemetry' is compiled in the same way (internal sources, no project dependency) and is covered by
            // this same follow-up: publish it separately, then depend on it.
            kotlin.srcDirs("src/main/kotlin", "../bin-processor/src/main/kotlin", "../telemetry/src/main/kotlin")
            res.srcDirs("src/main/res", "../bin-processor/src/main/res") // Added in case there are UI resources
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // Required by afterpay-android 4.8.x, which depends on desugared core library APIs.
        isCoreLibraryDesugaringEnabled = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
        // Disable window animations during instrumented tests (command-line) to reduce flakiness
        animationsDisabled = true
    }
    // Gradle Managed Devices for instrumentation tests (CI-friendly images).
    // - pixel6Api35: the max supported OS (default single-device run).
    // - minApi24: the minimum supported OS (minSdk = 24). ATD images only exist for API 30+, so
    //   the floor device uses the plain "aosp" image.
    // The "osMatrix" group runs the suite on both floor and ceiling: `:mobile-sdk:osMatrixGroupDebugAndroidTest`.
    testOptions.managedDevices {
        localDevices {
            create("pixel6Api35") {
                device = "Pixel 6"
                apiLevel = 35
                systemImageSource = "aosp-atd"
            }
            create("minApi24") {
                device = "Pixel 6"
                apiLevel = 24
                systemImageSource = "aosp"
            }
        }
        groups {
            create("osMatrix") {
                targetDevices.add(localDevices.getByName("minApi24"))
                targetDevices.add(localDevices.getByName("pixel6Api35"))
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

/** Writes the internal `com.paydock.core.telemetry.TelemetryBuildValues` object of one variant. */
abstract class GenerateTelemetryBuildValues : DefaultTask() {
    @get:Input
    abstract val buildEnabled: Property<Boolean>

    @get:Input
    abstract val endpoint: Property<String>

    @get:Input
    abstract val auth: Property<String>

    @get:Input
    abstract val level: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        fun literal(value: String): String =
            "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$") + "\""
        val file = outputDir.file("com/paydock/core/telemetry/TelemetryBuildValues.kt").get().asFile
        file.parentFile.mkdirs()
        file.writeText(
            listOf(
                "package com.paydock.core.telemetry",
                "",
                "/** Generated per variant by the build. Debug: from local.properties. " +
                    "Release: always disabled and empty (D14). */",
                "internal object TelemetryBuildValues {",
                "    const val ENABLED: Boolean = ${buildEnabled.get()}",
                "    const val OTLP_ENDPOINT: String = ${literal(endpoint.get())}",
                "    const val OTLP_AUTH: String = ${literal(auth.get())}",
                "    const val LEVEL: String = ${literal(level.get())}",
                "}",
                ""
            ).joinToString("\n")
        )
    }
}

// Debug values come from the engineer's uncommitted local.properties (spec § 8); never commit them.
// Every other build type is always disabled and empty (D14): local.properties is not read for it.
// No release-variant unit test guards this on purpose: AGP 9 creates no release unit-test task, and the release
// inputs below are fixed literals, so no local value can reach a release build. Revisit with the turn-on ticket.
androidComponents {
    onVariants { variant ->
        val isDebug = variant.buildType == "debug"
        val localProperties = Properties()
        if (isDebug) {
            val localFile = rootProject.file("local.properties")
            if (localFile.isFile) localFile.inputStream().use { localProperties.load(it) }
        }
        fun localValue(key: String): String = localProperties.getProperty(key)?.trim().orEmpty()
        val variantName = variant.name.replaceFirstChar { it.uppercase() }
        val task = tasks.register<GenerateTelemetryBuildValues>("generate${variantName}TelemetryBuildValues") {
            buildEnabled.set(isDebug)
            endpoint.set(if (isDebug) localValue("paydock.telemetry.otlpEndpoint") else "")
            auth.set(if (isDebug) localValue("paydock.telemetry.otlpAuth") else "")
            level.set(if (isDebug) localValue("paydock.telemetry.level") else "")
        }
        variant.sources.kotlin?.addGeneratedSourceDirectory(task, GenerateTelemetryBuildValues::outputDir)
    }
}

dependencies {
    // Paydock Modules (Libs)
    api(libs.paydock.core.networking)
    // The 'bin-processor' is currently included via sourceSets (above) to avoid
    // "unspecified" dependency errors in the published SDK.
    // Uncomment and use remote coordinates once the module is published independently.
    // implementation(project(":bin-processor"))
    // implementation("com.paydock:bin-processor:1.0.0")
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
    // Core library desugaring runtime required by afterpay-android 4.8.x
    coreLibraryDesugaring(libs.android.desugar.jdk.libs)
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