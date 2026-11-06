package com.paydock.telemetry

/** One record of a merge, and the ids of every stored record it stands for, its own included. */
internal data class MergedRecord(val record: TelemetryRecord, val sourceIds: List<String>)
