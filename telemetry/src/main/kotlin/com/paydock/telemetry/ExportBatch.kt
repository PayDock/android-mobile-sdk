package com.paydock.telemetry

/** What the exporter sends: the device context (with app.installation.id) and records that each carry session.id. */
internal data class ExportBatch(val resource: ResourceAttributes, val records: List<PendingRecord>)
