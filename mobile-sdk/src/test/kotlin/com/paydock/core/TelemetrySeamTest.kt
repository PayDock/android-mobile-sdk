package com.paydock.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Seam guard for the telemetry module (R_08).
 *
 * The telemetry sources are compiled into this library, so the compiler alone cannot stop SDK code from reaching
 * past the facade. This test scans the SDK's own sources (and bin-processor's, which compile alongside them) and
 * allows them to refer only to the `Telemetry` facade and to `TelemetryEvent` (with its nested types). The one
 * exception is the activation binding in [allowlist], which decides, binds and starts telemetry at SDK start.
 *
 * Java sources, where they exist, may not mention the telemetry package at all: Java does not see Kotlin's
 * `internal`, so a Java file could reach any telemetry class.
 *
 * The working directory of a module's unit tests is the module directory.
 */
internal class TelemetrySeamTest {

    private val sourceRoots = listOf(
        File("src/main/kotlin"),
        File("../bin-processor/src/main/kotlin")
    )

    private val javaSourceRoots = listOf(
        File("src/main/java"),
        File("../bin-processor/src/main/java")
    )

    /**
     * Files, relative to the module directory (`invariantSeparatorsPath`), allowed to reach past the facade: the
     * activation binding only (MD-24). It selects and starts telemetry, keeps the lifecycle handle to itself and
     * registers only the facade in Koin.
     */
    private val allowlist: Set<String> = setOf("src/main/kotlin/com/paydock/core/telemetry/TelemetryBinding.kt")

    private val telemetryPackageDeclaration = Regex("""^\s*package\s+com\.paydock\.telemetry(\.|\s|$)""")
    private val telemetryStarImport = Regex("""^\s*import\s+com\.paydock\.telemetry(\.\w+)*\.\*""")
    private val telemetryReference = Regex("""com\.paydock\.telemetry\.""")
    private val telemetryPackageMention = Regex("""com\.paydock\.telemetry\b""")
    private val allowedReference = Regex("""com\.paydock\.telemetry\.(?:Telemetry(?![\w.])|TelemetryEvent(?!\w))""")

    private fun sourceFiles(): List<File> = sourceRoots.flatMap { root ->
        root.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }.filterNot { it.invariantSeparatorsPath in allowlist }

    private fun javaSourceFiles(): List<File> = javaSourceRoots.filter { it.isDirectory }.flatMap { root ->
        root.walkTopDown().filter { it.isFile && it.extension == "java" }.toList()
    }

    @Test
    fun sourceRoots_containSources() {
        sourceRoots.forEach { root ->
            assertTrue("no Kotlin sources found under ${root.absolutePath}", root.walkTopDown().any { it.extension == "kt" })
        }
    }

    @Test
    fun sdkSources_doNotDeclareTheTelemetryPackage() {
        sourceFiles().forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                assertFalse(
                    "${file.path}:${index + 1} must not declare the telemetry package",
                    telemetryPackageDeclaration.containsMatchIn(line)
                )
            }
        }
    }

    @Test
    fun sdkSources_doNotStarImportTelemetry() {
        sourceFiles().forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                assertFalse(
                    "${file.path}:${index + 1} must not star-import telemetry: $line",
                    telemetryStarImport.containsMatchIn(line)
                )
            }
        }
    }

    @Test
    fun sdkSources_referOnlyToTheFacadeAndTheEvents() {
        sourceFiles().forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                val references = telemetryReference.findAll(line).map { it.range.first }.toList()
                val allowed = allowedReference.findAll(line).map { it.range.first }.toSet()
                references.forEach { position ->
                    assertTrue(
                        "${file.path}:${index + 1} may refer only to Telemetry or TelemetryEvent: $line",
                        position in allowed
                    )
                }
            }
        }
    }

    @Test
    fun javaSources_doNotMentionTelemetry() {
        javaSourceFiles().forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                assertFalse(
                    "${file.path}:${index + 1} must not mention the telemetry package: $line",
                    telemetryPackageMention.containsMatchIn(line)
                )
            }
        }
    }
}
