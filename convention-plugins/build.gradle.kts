plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // This allows us to use Version Catalog in sub-gradle scripts
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.detekt.plugin) // Convention fully configures detekt
    implementation(libs.org.jreleaser.gradle.plugin) // Convention fully configures JReleaser
    implementation(libs.android.gradlePlugin) // For Android LibraryExtension access
}