plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

// Ensure the included build (convention-plugins) resolves a compatible commons-compress version
// so Gradle plugin classpaths (e.g., JReleaser) don't pick an older transitive in CI.
configurations.configureEach {
    resolutionStrategy {
        force("org.apache.commons:commons-compress:1.27.1")
    }
}

dependencies {
    // This allows us to use Version Catalog in sub-gradle scripts
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.detekt.plugin) // Convention fully configures detekt
    implementation(libs.org.jreleaser.gradle.plugin) // Convention fully configures JReleaser
    implementation(libs.android.gradlePlugin) // For Android LibraryExtension access
}