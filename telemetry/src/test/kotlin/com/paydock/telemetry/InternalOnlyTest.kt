package com.paydock.telemetry

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Source check (RS-5): every telemetry declaration is internal (or private), and nothing is public.
 *
 * The working directory of a module's unit tests is the module directory, so `src/main/kotlin` is this module's
 * main source set.
 */
internal class InternalOnlyTest {

    private val sourceRoot = File("src/main/kotlin")

    private val declaration = Regex("""^(?:\w+\s+)*(class|interface|object|fun|val|var|typealias)\b""")
    private val publicWord = Regex("""\bpublic\b""")
    private val packageLine = Regex("""^package\s+com\.paydock\.telemetry\s*$""")

    private fun sourceFiles(): List<File> =
        sourceRoot.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()

    /** Returns the file's lines with comments removed, keeping the column of the remaining code. */
    private fun codeLines(file: File): List<String> {
        var inBlockComment = false
        return file.readLines().map { line ->
            val code = StringBuilder()
            var i = 0
            while (i < line.length) {
                when {
                    inBlockComment && line.startsWith("*/", i) -> {
                        inBlockComment = false
                        i += 2
                    }
                    inBlockComment -> i++
                    line.startsWith("/*", i) -> {
                        inBlockComment = true
                        i += 2
                    }
                    line.startsWith("//", i) -> i = line.length
                    else -> code.append(line[i++])
                }
            }
            code.toString()
        }
    }

    @Test
    fun sourceDirectory_isNotEmpty() {
        assertTrue("no Kotlin sources found under ${sourceRoot.absolutePath}", sourceFiles().isNotEmpty())
    }

    @Test
    fun everyFile_isInTheTelemetryPackage() {
        val files = sourceFiles()
        assertTrue(files.isNotEmpty())
        files.forEach { file ->
            assertTrue(
                "${file.path} must declare package com.paydock.telemetry",
                codeLines(file).any { packageLine.matches(it.trim()) }
            )
        }
    }

    @Test
    fun everyTopLevelDeclaration_isInternalOrPrivate() {
        val files = sourceFiles()
        assertTrue(files.isNotEmpty())
        files.forEach { file ->
            codeLines(file).forEachIndexed { index, line ->
                val isSkipped = line.isBlank() ||
                    line.first().isWhitespace() ||
                    line.startsWith("package ") ||
                    line.startsWith("import ") ||
                    line.startsWith("@")
                if (!isSkipped && declaration.containsMatchIn(line)) {
                    assertTrue(
                        "${file.path}:${index + 1} must be internal or private: $line",
                        line.startsWith("internal ") || line.startsWith("private ")
                    )
                }
            }
        }
    }

    @Test
    fun noSource_usesPublic() {
        val files = sourceFiles()
        assertTrue(files.isNotEmpty())
        files.forEach { file ->
            codeLines(file).forEachIndexed { index, line ->
                assertFalse("${file.path}:${index + 1} must not be public: $line", publicWord.containsMatchIn(line))
            }
        }
    }
}
