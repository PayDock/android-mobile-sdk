package com.paydock.core.telemetry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Source scan for MD-52 (R_03): no local value can reach any build, because nothing is read from `local.properties`.
 *
 * The collector table is the only input of the telemetry binding. This test scans the build scripts and the main
 * sources and fails if a `paydock.telemetry.*` key, `local.properties` or the removed generated `TelemetryBuildValues`
 * comes back, or if `bindTelemetry` takes its endpoint from anywhere but [TelemetryCollector.logsUrl]. A missing file
 * or an empty source root fails the test; it never skips.
 *
 * The working directory of a module's unit tests is the module directory.
 */
internal class TelemetryNoLocalValuesTest {

    /** The SDK's build script and the convention plugins it applies. */
    private val buildScript = File("build.gradle.kts")
    private val conventionPlugins = File("../convention-plugins/src/main/kotlin")

    /** The main sources compiled into the SDK: its own and the telemetry module's. */
    private val mainSourceRoots = listOf(File("src/main/kotlin"), File("../telemetry/src/main/kotlin"))

    private val binding = File("src/main/kotlin/com/paydock/core/telemetry/TelemetryBinding.kt")

    /** Any mention of the old key prefix in a build script, the telemetry package (`com.paydock.telemetry`) aside. */
    private val anyTelemetryKey = Regex("""(?<!com\.)paydock\.telemetry\b""")

    /**
     * A `paydock.telemetry.<name>` key in a source. The package (`com.paydock.telemetry.`) is not one, nor are the
     * record names in [recordNames], which are event and attribute names the records carry, never settings.
     */
    private val telemetryKey = Regex("""(?<![\w.])paydock\.telemetry\.(\w+)""")
    private val recordNames = setOf("dropped", "spec_version")

    private val forbiddenNames = listOf("local.properties", "TelemetryBuildValues")

    private fun requireFile(file: File): File {
        assertTrue("missing file: ${file.absolutePath}", file.isFile)
        return file
    }

    private fun sourceFiles(root: File, extension: String = "kt"): List<File> {
        assertTrue("missing directory: ${root.absolutePath}", root.isDirectory)
        val files = root.walkTopDown().filter { it.isFile && it.name.endsWith(".$extension") }.toList()
        assertTrue("no .$extension files found under ${root.absolutePath}", files.isNotEmpty())
        return files
    }

    private fun buildScripts(): List<File> = listOf(requireFile(buildScript)) + sourceFiles(conventionPlugins, "kts")

    private fun mainSources(): List<File> = mainSourceRoots.flatMap { sourceFiles(it) }

    @Test
    fun noLocalValueReachesAnyBuild_theBuildScriptsNeverReadATelemetryKey() {
        buildScripts().forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                assertFalse(
                    "${file.path}:${index + 1} must not read a paydock.telemetry key: $line",
                    anyTelemetryKey.containsMatchIn(line)
                )
            }
        }
    }

    @Test
    fun noLocalValueReachesAnyBuild_noMainSourceNamesLocalPropertiesBuildValuesOrATelemetryKey() {
        mainSources().forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                forbiddenNames.forEach { name ->
                    assertFalse("${file.path}:${index + 1} must not name $name: $line", line.contains(name))
                }
                telemetryKey.findAll(line).forEach { match ->
                    assertTrue(
                        "${file.path}:${index + 1} must not name a paydock.telemetry key: ${match.value}",
                        match.groupValues[1] in recordNames
                    )
                }
            }
        }
    }

    @Test
    fun noLocalValueReachesAnyBuild_bindTelemetryTakesItsEndpointOnlyFromTheCollectorTable() {
        val source = requireFile(binding).readText()
        val start = source.indexOf("internal fun bindTelemetry(")
        assertTrue("bindTelemetry not found in ${binding.path}", start >= 0)
        val end = source.indexOf("\n}", start)
        assertTrue("the end of bindTelemetry not found in ${binding.path}", end > start)
        val body = source.substring(start, end)

        assertEquals(
            "bindTelemetry takes exactly (koin, environment, context): no injectable values",
            "internal fun bindTelemetry(koin: Koin, environment: Environment, context: Context): Telemetry",
            body.substringBefore(" = runCatching").trim()
        )
        assertTrue(
            "the endpoint must come from TelemetryCollector.logsUrl(environment)",
            body.contains("val endpoint = runCatching { TelemetryCollector.logsUrl(environment) }.getOrNull()")
        )
        assertEquals("one endpoint declaration", 1, Regex("""\bval endpoint\b""").findAll(body).count())
        assertEquals("one selection", 1, Regex("""\bselectTelemetry\(""").findAll(body).count())
        assertTrue("the selection must use that endpoint", body.contains("selectTelemetry(endpoint)"))
        assertEquals("one lookup", 1, Regex("""\bTelemetryCollector\.""").findAll(body).count())
    }
}
