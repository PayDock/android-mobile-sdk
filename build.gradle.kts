// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.dagger.hilt) apply false
    alias(libs.plugins.ksp.devtools) apply false
    // Dependency Validation Plugins
    alias(libs.plugins.dependency.analysis) apply true
    alias(libs.plugins.dependency.guard) apply false
    // JaCoCo for merged coverage report
    id("jacoco")
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

buildscript {
    configurations.configureEach {
        resolutionStrategy {
            force("org.apache.commons:commons-compress:1.27.1")
        }
    }
}

// Force commons-compress version for JReleaser 1.19.0 compatibility
configurations.all {
    resolutionStrategy {
        force("org.apache.commons:commons-compress:1.27.1")
    }
}

tasks.register("clean").configure {
    delete("build")
}

// Merged JaCoCo report aggregating bin-processor and mobile-sdk coverage
// Run: ./gradlew jacocoMergedReport
tasks.register<JacocoReport>("jacocoMergedReport") {
    group = "verification"
    description = "Generates a merged JaCoCo coverage report from bin-processor and mobile-sdk"

    dependsOn(
//        ":bin-processor:testDebugUnitTest",
        ":mobile-sdk:testDebugUnitTest"
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(file("${layout.buildDirectory.get().asFile}/reports/jacoco/merged/jacocoMergedReport.xml"))
        html.outputLocation.set(file("${layout.buildDirectory.get().asFile}/reports/jacoco/merged/html"))
    }

    val excludes = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "**/Hilt_*.*",
        "**/Dagger*.*",
        "**/*_Factory.*",
        "**/*_MembersInjector.*",
        "**/*\$Lambda\$*.*",
        "**/*\$inlined\$*.*",
        "**/*Module*.class",
        "**/*KoinModule*.class",
        "**/injection/**/*.class",
        "**/presentation/components/**/*.class",
        "**/*Widget*.class",
        "**/*Activity*.class",
        "**/*Fragment*.class",
        "**/presentation/viewmodels/**/*.class",
        "**/*ViewModel*.class",
        "**/designsystems/**/*.class",
        "**/core/presentation/ui/**/*.class",
        "**/core/presentation/extensions/**/*.class",
        "**/core/utils/reader/**/*.class",
        "**/core/utils/decoder/**/*.class"
    )

    val binProcessorBuild = file("${rootDir}/bin-processor/build")
    val mobileSdkBuild = file("${rootDir}/mobile-sdk/build")

    val binProcessorClasses = files(
        fileTree("${binProcessorBuild}/intermediates/javac/debug") { exclude(excludes) },
        fileTree("${binProcessorBuild}/tmp/kotlin-classes/debug") { exclude(excludes) },
        fileTree("${binProcessorBuild}/intermediates/runtime_library_classes_dir/debug") { exclude(excludes) }
    )
    val mobileSdkClasses = files(
        fileTree("${mobileSdkBuild}/intermediates/javac/debug") { exclude(excludes) },
        fileTree("${mobileSdkBuild}/tmp/kotlin-classes/debug") { exclude(excludes) },
        fileTree("${mobileSdkBuild}/intermediates/runtime_library_classes_dir/debug") { exclude(excludes) }
    )

    classDirectories.setFrom(files(binProcessorClasses, mobileSdkClasses))
    sourceDirectories.setFrom(
        files(
            file("${rootDir}/bin-processor/src/main/java"),
            file("${rootDir}/bin-processor/src/main/kotlin"),
            file("${rootDir}/mobile-sdk/src/main/java"),
            file("${rootDir}/mobile-sdk/src/main/kotlin")
        )
    )
    executionData.setFrom(
        fileTree(rootDir) {
            include(
                "bin-processor/build/jacoco/*.exec",
                "bin-processor/build/outputs/unit_test_code_coverage/**/*.exec",
                "mobile-sdk/build/jacoco/*.exec",
                "mobile-sdk/build/outputs/unit_test_code_coverage/**/*.exec"
            )
        }
    )
}

tasks.register("copyGitHooks", Copy::class.java) {
    description = "Copies the git hooks from /git-hooks to the .git folder."
    group = "git hooks"
    from("$rootDir/scripts/pre-commit")
    into("$rootDir/.git/hooks/")
}

// Diagnostic: print which commons-compress jar Gradle/JVM loads at runtime
tasks.register("printCompressClasspath") {
    doLast {
        val clazz = Class.forName("org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream")
        val location = clazz.protectionDomain.codeSource.location
        val version = clazz.`package`?.implementationVersion
        println("commons-compress loaded from: $location (version=$version)")
    }
}

tasks.register("installGitHooks", Exec::class.java) {
    description = "Installs the pre-commit git hooks from /git-hooks."
    group = "git hooks"
    workingDir = rootDir
    commandLine = listOf("chmod")
    args("-R", "+x", ".git/hooks/")
    dependsOn("copyGitHooks")
    doLast {
        logger.info("Git hook installed successfully.")
    }
}

afterEvaluate {
    tasks.getByPath(":mobile-sdk:preBuild").dependsOn(":installGitHooks")
}

// Dependency Analysis Plugin Configuration
// Docs: https://github.com/autonomousapps/dependency-analysis-gradle-plugin

dependencyAnalysis {
    issues {
        all {
            onAny {
                severity("fail")
            }
            onUnusedDependencies {
                severity("fail")
            }
            onUsedTransitiveDependencies {
                severity("ignore")
            }
            onIncorrectConfiguration {
                severity("fail")
            }
            onCompileOnly {
                severity("fail")
            }
            onRuntimeOnly {
                severity("fail")
            }
        }
        
        project(":mobile-sdk") {
            onAny {
                exclude(
                    "com.afterpay:afterpay-android",
                    "com.paypal.android:paypal-web-payments",
                    "com.paypal.android:fraud-protection"
                )
            }
            // Fail on unused dependencies (strict)
            onUnusedDependencies {
                severity("fail")
                exclude(
                    "com.afterpay:afterpay-android",
                    "com.paypal.android:payment-buttons",
                    "com.google.pay.button:compose-pay-button",
                    "io.mockk:mockk-android",  // Used in androidTest but plugin can't detect
                    "org.mockito:mockito-android",  // Used in androidTest but plugin can't detect
                    "androidx.test.ext:junit-ktx"  // Used in androidTest but plugin can't detect
                )
            }
            // IGNORE transitive dependencies (using strictly() constraints for version control)
            onUsedTransitiveDependencies {
                severity("ignore")
            }
            // Don't fail on test-junit (we need testImplementation for @Test, not testRuntimeOnly)
            onRuntimeOnly {
                exclude("org.jetbrains.kotlin:kotlin-test-junit")
            }
        }
    }
    
    // Structure for reports
    structure {
        bundle("androidx") {
            primary("androidx.core:core-ktx")
            includeGroup("androidx.compose")
            includeGroup("androidx.lifecycle")
        }
        bundle("compose") {
            primary("androidx.compose.runtime:runtime")
            includeGroup("androidx.compose")
        }
    }
}