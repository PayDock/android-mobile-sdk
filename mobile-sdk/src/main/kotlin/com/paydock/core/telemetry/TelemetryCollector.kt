package com.paydock.core.telemetry

import com.paydock.core.domain.model.Environment

/**
 * The SDK's built-in collector URL per environment (D22). The table is the only switch: an empty URL turns that
 * environment off. A release build sends here, with no credential.
 */
internal object TelemetryCollector {
    const val DEV_LOGS_URL = "https://mobile-telemetry-dev.shared.apac.paydock.com/v1/logs"

    /** The collector's OTLP logs URL for [environment], or "" when it has none (D22). */
    fun logsUrl(environment: Environment): String = when (environment) {
        Environment.SANDBOX, Environment.STAGING -> DEV_LOGS_URL
        Environment.PRODUCTION -> "" // until the platform team provides the prod route
    }
}
