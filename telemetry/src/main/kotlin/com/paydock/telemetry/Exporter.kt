package com.paydock.telemetry

/** Sends one batch. */
internal interface Exporter {
    fun export(batch: ExportBatch): ExportResult
}
