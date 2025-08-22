import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
    id("jacoco")
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

// Configure JaCoCo reports for unit tests (Debug and Release)
// These tasks generate XML reports that GitLab can consume
tasks.register<JacocoReport>("jacocoDebugUnitTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(file("${layout.buildDirectory.get().asFile}/reports/jacoco/jacocoDebugUnitTestReport/jacocoDebugUnitTestReport.xml"))
        html.outputLocation.set(file("${layout.buildDirectory.get().asFile}/reports/jacoco/jacocoDebugUnitTestReport/html"))
    }

    val buildDir = layout.buildDirectory.get().asFile
    val excludes = listOf(
        // Generated Android files
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",

        // Dependency injection generated code
        "**/Hilt_*.*",
        "**/Dagger*.*",
        "**/*_Factory.*",
        "**/*_MembersInjector.*",
        "**/*\$Lambda\$*.*",
        "**/*\$inlined\$*.*",

        // Koin injection modules (dependency setup, not business logic)
        "**/*Module*.class",
        "**/*KoinModule*.class",
        "**/injection/**/*.class",

        // UI Components (require UI testing, not unit testing)
        "**/presentation/components/**/*.class",
        "**/*Widget*.class",
        "**/*Activity*.class",
        "**/*Fragment*.class",

        // ViewModels (complex to unit test, better with integration tests)
        "**/presentation/viewmodels/**/*.class",
        "**/*ViewModel*.class",

        // Design system (all UI-related components and theming)
        "**/designsystems/**/*.class",

        // Core presentation UI (UI utilities, extensions, gradients)
        "**/core/presentation/ui/**/*.class",
        "**/core/presentation/extensions/**/*.class",

        // Core utility classes that are typically hard to unit test
        "**/core/utils/reader/**/*.class",
        "**/core/utils/decoder/**/*.class"
    )

    val debugJavaClasses = fileTree("${buildDir}/intermediates/javac/debug") {
        exclude(excludes)
    }
    val debugKotlinClasses = fileTree("${buildDir}/tmp/kotlin-classes/debug") {
        exclude(excludes)
    }
    val debugRuntimeClasses = fileTree("${buildDir}/intermediates/runtime_library_classes_dir/debug") {
        exclude(excludes)
    }

    classDirectories.setFrom(files(debugJavaClasses, debugKotlinClasses, debugRuntimeClasses))
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(
        fileTree(layout.buildDirectory.get().asFile) {
            include(
                "jacoco/testDebugUnitTest.exec",
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "jacoco/*.exec"
            )
        }
    )
}

tasks.register<JacocoReport>("jacocoReleaseUnitTestReport") {
    dependsOn("testReleaseUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(file("${layout.buildDirectory.get().asFile}/reports/jacoco/jacocoReleaseUnitTestReport/jacocoReleaseUnitTestReport.xml"))
        html.outputLocation.set(file("${layout.buildDirectory.get().asFile}/reports/jacoco/jacocoReleaseUnitTestReport/html"))
    }

    val releaseBuildDir = layout.buildDirectory.get().asFile
    val releaseJavaClasses = fileTree("${releaseBuildDir}/intermediates/javac/release") {
        exclude(excludes)
    }
    val releaseKotlinClasses = fileTree("${releaseBuildDir}/tmp/kotlin-classes/release") {
        exclude(excludes)
    }
    val releaseRuntimeClasses = fileTree("${releaseBuildDir}/intermediates/runtime_library_classes_dir/release") {
        exclude(excludes)
    }

    classDirectories.setFrom(files(releaseJavaClasses, releaseKotlinClasses, releaseRuntimeClasses))
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(
        fileTree(layout.buildDirectory.get().asFile) {
            include(
                "jacoco/testReleaseUnitTest.exec",
                "outputs/unit_test_code_coverage/releaseUnitTest/testReleaseUnitTest.exec",
                "jacoco/*.exec"
            )
        }
    )
}
