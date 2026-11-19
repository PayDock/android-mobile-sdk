package com.paydock.core.telemetry

import com.paydock.core.domain.model.Environment

/**
 * The SDK's built-in collector URL per environment (D22). The table is the only switch, and every environment has a
 * collector: `SANDBOX` and `STAGING` send to the dev collector, `PRODUCTION` to the production one. Every build sends
 * here, with no `Authorization` header.
 */
internal object TelemetryCollector {
    const val DEV_LOGS_URL = "https://mobile-telemetry-dev.shared.apac.paydock.com/v1/logs"
    const val PROD_LOGS_URL = "https://mobile-telemetry.shared.apac.paydock.com/v1/logs"

    /** The collector's OTLP logs URL for [environment] (D22). */
    fun logsUrl(environment: Environment): String = when (environment) {
        Environment.SANDBOX, Environment.STAGING -> DEV_LOGS_URL
        Environment.PRODUCTION -> PROD_LOGS_URL
    }
}
