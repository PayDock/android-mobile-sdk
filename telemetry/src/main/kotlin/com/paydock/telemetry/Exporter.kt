package com.paydock.telemetry

/** Sends one batch, once. */
internal interface Exporter {
    fun export(batch: ExportBatch): ExportResult // one attempt, one batch; never throws
}
