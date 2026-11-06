// Sources ship inside :mobile-sdk via sourceSets: see the TEMPORARY WORKAROUND TODO in mobile-sdk/build.gradle.kts.
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    // linting
    id("detekt-convention")
    // test coverage
    id("test-coverage-convention")
    // dependency validation (buildHealth)
    id("dependency-analysis-convention")
}

android {
    namespace = "com.paydock.telemetry"
    // Shared with :mobile-sdk, which compiles these sources: no API the library does not compile against.
    compileSdk = libs.versions.sdk.compileSdk.get().toInt()

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // Compile-only: everything below already ships inside :mobile-sdk, so the module adds nothing to its runtime
    // classpath.
    compileOnly(libs.paydock.core.networking)
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.coroutines)
    compileOnly(libs.androidx.lifecycle.process)

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.paydock.core.networking)
}
