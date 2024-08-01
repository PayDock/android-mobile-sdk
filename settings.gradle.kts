rootProject.name = "SampleApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
includeBuild("convention-plugins")

include(":sample")
include(":mobile-sdk")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // Used for Paydock submodules
        maven { setUrl("https://www.jitpack.io") }
    }
}