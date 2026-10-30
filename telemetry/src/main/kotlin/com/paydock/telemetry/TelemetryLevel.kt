package com.paydock.telemetry

/** How much telemetry records: nothing, errors only, or everything. */
internal enum class TelemetryLevel {
    OFF, ERRORS, ALL;

    companion object {
        /** Absent or blank → ALL (D3); a level name, case-insensitive and trimmed → that level; anything else → OFF. */
        fun parse(raw: String?): TelemetryLevel {
            val name = raw?.trim().orEmpty()
            if (name.isEmpty()) return ALL
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: OFF
        }
    }
}
