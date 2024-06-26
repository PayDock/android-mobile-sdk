import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    // Version of Detekt that will be used. When unspecified the latest detekt
    // version found will be used. Override to stay on the same version.
    toolVersion = libs.versions.detekt.get()
    // Applies the config files on top of detekt's default config file. `false` by default.
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    autoCorrect = true
    parallel = true
    // The directories where detekt looks for source files.
    // Defaults to `files("src/main/java", "src/test/java", "src/main/kotlin", "src/test/kotlin")`.
    source.setFrom("src/main/java", "src/main/kotlin")
    // Define the detekt configuration(s) you want to use.
    // Defaults to the default detekt configuration.
    config.setFrom("../config/detekt/detekt.yml")
    // point to your custom config defining rules to run, overwriting default behavior
    // Adds debug output during task execution. `false` by default.
    debug = false
}

dependencies {
    // detekt itself provides a wrapper over ktlint as the formatting rule set which can be easily added to the Gradle configuration:
    detektPlugins(libs.detekt.formatting.plugin)
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = JavaVersion.VERSION_1_8.toString()
    reports {
        // Enable/Disable XML report (default: true)
        xml.required.set(true)
        xml.outputLocation.set(file("build/reports/detekt.xml"))
        // Enable/Disable HTML report (default: true)
        html.required.set(true) // observe findings in your browser with structure and code snippets
        html.outputLocation.set(file("build/reports/detekt.html"))
        // Enable/Disable TXT report (default: true)
        txt.required.set(true) // similar to the console output, contains issue signature to manually edit baseline files
        txt.outputLocation.set(file("build/reports/detekt.txt"))
        // Enable/Disable SARIF report (default: false)
        sarif.required.set(true)
        sarif.outputLocation.set(file("build/reports/detekt.sarif"))
        // Enable/Disable MD report (default: false)
        md.required.set(true) // simple Markdown format
        md.outputLocation.set(file("build/reports/detekt.md"))
    }
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = JavaVersion.VERSION_1_8.toString()
}

val analysisDir = file(projectDir)

val kotlinFiles = "**/*.kt"
val kotlinScriptFiles = "**/*.kts"
val resourceFiles = "**/resources/**"
val buildFiles = "**/build/**"

val detektFormat by tasks.registering(Detekt::class) {
    description = "Formats whole project."
    parallel = true
    disableDefaultRuleSets = true
    buildUponDefaultConfig = true
    autoCorrect = true
    setSource(analysisDir)
    config.setFrom("${project.rootDir}/config/detekt/detekt.yml")
    include(kotlinFiles)
    include(kotlinScriptFiles)
    exclude(resourceFiles)
    exclude(buildFiles)
    reports {
        xml.required.set(false)
        html.required.set(false)
        txt.required.set(false)
    }
}

val detektAll by tasks.registering(Detekt::class) {
    description = "Runs the whole project at once."
    parallel = true
    buildUponDefaultConfig = true
    setSource(analysisDir)
    config.setFrom("${project.rootDir}/config/detekt/detekt.yml")
    include(kotlinFiles)
    include(kotlinScriptFiles)
    exclude(resourceFiles)
    exclude(buildFiles)
    reports {
        xml.required.set(false)
        html.required.set(false)
        txt.required.set(false)
    }
}

val detektProjectBaseline by tasks.registering(DetektCreateBaselineTask::class) {
    description = "Overrides current baseline."
    buildUponDefaultConfig.set(true)
    ignoreFailures.set(true)
    parallel.set(true)
    setSource(analysisDir)
    config.setFrom("${project.rootDir}/config/detekt/detekt.yml")
    include(kotlinFiles)
    include(kotlinScriptFiles)
    exclude(resourceFiles)
    exclude(buildFiles)
}