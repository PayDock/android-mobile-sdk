rootProject.name = "SampleApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
includeBuild("convention-plugins")

// Only include sample app if not on JitPack
if (!System.getenv().containsKey("JITPACK")) {
    include(":sample")
}
include(":mobile-sdk")
include(":bin-processor")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}