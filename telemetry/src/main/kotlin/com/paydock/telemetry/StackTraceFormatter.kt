package com.paydock.telemetry

/**
 * Formats a sanitised [TelemetryEvent.ErrorInfo] as `exception.stacktrace` (spec § 5.5): the type line, or
 * `Caused by: <type>` for a cause, then `\tat <frame>` per frame, joined with `\n`. A missing type writes no type
 * line. At most [MAX_FRAMES] frames per exception and [MAX_CAUSES] causes; the text is cut at the last whole line
 * that keeps it within [MAX_BYTES] UTF-8 bytes.
 */
internal object StackTraceFormatter {
    const val MAX_FRAMES = 64 // per exception
    const val MAX_CAUSES = 8
    const val MAX_BYTES = 16_384 // UTF-8, whole lines only

    private const val CAUSED_BY = "Caused by: "
    private const val FRAME_PREFIX = "\tat "
    private const val SEPARATOR = "\n"

    fun format(error: TelemetryEvent.ErrorInfo): String {
        val chain = generateSequence(error) { it.cause }.take(MAX_CAUSES + 1)
        val lines = chain.flatMapIndexed { index, info ->
            val typeLine = info.type?.let { type -> if (index == 0) type else CAUSED_BY + type }
            listOfNotNull(typeLine) + info.frames.take(MAX_FRAMES).map { FRAME_PREFIX + it }
        }
        val kept = mutableListOf<String>()
        var bytes = 0
        for (line in lines) {
            val added = line.toByteArray(Charsets.UTF_8).size + if (kept.isEmpty()) 0 else SEPARATOR.length
            if (bytes + added > MAX_BYTES) break
            kept += line
            bytes += added
        }
        return kept.joinToString(SEPARATOR)
    }
}
