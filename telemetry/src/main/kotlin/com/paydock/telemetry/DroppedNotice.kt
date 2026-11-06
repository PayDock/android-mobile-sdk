package com.paydock.telemetry

/** The dropped-records notice of one reason in the current hour, not yet sent (spec § 5.8). */
internal data class DroppedNotice(
    val id: String, // "<sessionId>-<sequence>", the notice record's log.record.uid
    val count: Int,
    val createdAt: Long, // UTC epoch ms
)
