// Sources ship inside :mobile-sdk via sourceSets: see the TEMPORARY WORKAROUND TODO in mobile-sdk/build.gradle.kts.
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    // linting
    id("detekt-convention")
    // test coverage
    id("test-coverage-convention")
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
    // No main dependencies: the module ships inside :mobile-sdk and must not change its runtime classpath.

    // Unit Testing
    testImplementation(libs.junit)
}
