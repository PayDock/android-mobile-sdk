package com.paydock.telemetry

/** The spec § 6 parameters, in one place; every other type reads them from here. */
internal object TelemetryLimits {
    const val FLUSH_INTERVAL_MILLIS = 10_000L
    const val FLUSH_THRESHOLD = 20 // pending records that trigger an export at once
    const val BATCH_MAX_RECORDS = 50
    const val BATCH_MAX_BYTES = 262_144 // JSON before gzip
    const val STORE_MAX_RECORDS = 100
    const val STORE_MAX_BYTES = 1_048_576L
    const val RECORD_MAX_AGE_MILLIS = 561_600_000L // 156 h
    const val REQUEST_TIMEOUT_MILLIS = 15_000L
    const val RETRY_MAX_ATTEMPTS = 5
    const val RETRY_BASE_DELAY_MILLIS = 5_000L
    const val RETRY_MAX_DELAY_MILLIS = 60_000L
    const val HOURLY_CAP = 400 // records created per hour, notices excluded
    const val HOUR_MILLIS = 3_600_000L
}
