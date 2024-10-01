rootProject.name = "SampleApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
includeBuild("convention-plugins")

// Only include sample app if not on JitPack
if (System.getenv("JITPACK") == "false") { // Only include sample app if not on JitPack
    include(":sample")
}
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