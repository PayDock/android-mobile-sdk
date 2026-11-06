package com.paydock.telemetry

/** What one flush did: the records it dropped by reason, what is left, and whether sending stopped. */
internal data class FlushReport(
    val tooOld: Int = 0,
    val storeFull: Int = 0,
    val rejected: Int = 0,
    val pending: Int,
    val stopped: Boolean = false, // this flush stopped sending for the process (401/403 or give-up)
    val exportedNoticeIds: Set<String> = emptySet(), // notices whose batch was accepted or rejected this flush
)
