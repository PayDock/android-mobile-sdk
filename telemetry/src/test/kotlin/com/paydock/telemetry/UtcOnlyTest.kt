package com.paydock.telemetry

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Source check (R_11): telemetry time values are UTC epoch milliseconds only. No main source names a time zone,
 * a local date or time, a calendar, a formatter or `java.util.Date`; formatting happens only where a record is
 * encoded.
 *
 * The working directory of a module's unit tests is the module directory, so `src/main/kotlin` is this module's
 * main source set.
 */
internal class UtcOnlyTest {

    private val sourceRoot = File("src/main/kotlin")

    private val forbidden = listOf(
        "TimeZone",
        "ZoneId",
        "ZonedDateTime",
        "LocalDateTime",
        "LocalDate",
        "Calendar",
        "SimpleDateFormat",
        "DateTimeFormatter",
        "java.util.Date"
    ).map { name -> name to Regex("""(?<!\w)${Regex.escape(name)}(?!\w)""") }

    private fun sourceFiles(): List<File> =
        sourceRoot.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()

    @Test
    fun sourceDirectory_isNotEmpty() {
        assertTrue("no Kotlin sources found under ${sourceRoot.absolutePath}", sourceFiles().isNotEmpty())
    }

    @Test
    fun noSource_usesLocalTimeZonesOrFormatters() {
        sourceFiles().forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                forbidden.forEach { (name, pattern) ->
                    assertFalse("${file.path}:${index + 1} must not use $name: $line", pattern.containsMatchIn(line))
                }
            }
        }
    }

    @Test
    fun forbiddenPatterns_matchQualifiedAndImportedUses() {
        val samples = listOf(
            "import java.util.TimeZone",
            "val zone = ZoneId.systemDefault()",
            "val now = java.time.ZonedDateTime.now()",
            "LocalDateTime.now()",
            "LocalDate.now()",
            "Calendar.getInstance()",
            "SimpleDateFormat(\"yyyy\")",
            "DateTimeFormatter.ISO_INSTANT",
            "val d = java.util.Date()"
        )

        samples.zip(forbidden).forEach { (sample, entry) ->
            assertTrue("${entry.first} must match: $sample", entry.second.containsMatchIn(sample))
        }
    }
}
