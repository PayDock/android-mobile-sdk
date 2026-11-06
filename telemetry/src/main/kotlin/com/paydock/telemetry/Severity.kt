package com.paydock.telemetry

/** A record's OTLP severity: its severityNumber, and its name as the severityText (spec § 5.2). */
internal enum class Severity(val number: Int) {
    INFO(number = 9),
    WARN(number = 13),
    ERROR(number = 17),
    FATAL(number = 21),
}
