package com.paydock.core.telemetry

import java.io.File

/**
 * Reads the SDK's own sources for the composable failure paths that a unit test cannot drive: the handler lambdas
 * and private handler functions of the widget composables. The working directory of a module's unit tests is the
 * module directory.
 */
internal object WidgetSourceScan {

    private const val MAIN_ROOT = "src/main/kotlin/com/paydock/"

    /** The source of [relativePath], relative to `src/main/kotlin/com/paydock/`. */
    fun read(relativePath: String): String = File(MAIN_ROOT + relativePath).readText()

    /**
     * The text of the top-level function [name] in [source]: from its `fun <name>(` declaration to the first line
     * that is a lone closing brace.
     */
    fun function(source: String, name: String): String {
        val start = source.indexOf("fun $name(")
        require(start >= 0) { "no function $name" }
        val end = source.indexOf("\n}\n", start)
        require(end >= 0) { "no end for function $name" }
        return source.substring(start, end)
    }

    /** The text of [source] from the first [from] to the next [to] after it. */
    fun between(source: String, from: String, to: String): String {
        val start = source.indexOf(from)
        require(start >= 0) { "no $from" }
        val end = source.indexOf(to, start + from.length)
        require(end >= 0) { "no $to after $from" }
        return source.substring(start, end)
    }

    /** How many times [call] appears in [text]. */
    fun count(text: String, call: String): Int = Regex(Regex.escape(call)).findAll(text).count()
}
