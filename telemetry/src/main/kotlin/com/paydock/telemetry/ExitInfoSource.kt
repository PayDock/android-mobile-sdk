package com.paydock.telemetry

/** The process's own recent exits, any order. May throw (the reader catches). */
internal fun interface ExitInfoSource {
    fun exits(): List<ProcessExit>

    companion object {
        val NONE: ExitInfoSource = ExitInfoSource { emptyList() }
    }
}
