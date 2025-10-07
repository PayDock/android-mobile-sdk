// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.dagger.hilt) apply false
    alias(libs.plugins.ksp.devtools) apply false
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