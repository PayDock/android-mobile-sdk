package com.paydock.telemetry

/** Sends nothing and reports success: nothing leaves the device yet (R_02). */
internal object NoOpExporter : Exporter {
    override fun export(batch: ExportBatch): ExportResult = ExportResult.SUCCESS
}
