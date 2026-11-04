package com.paydock.telemetry

/** What the platform reports. Never a hardware or advertising identifier (principle 10). */
internal interface PlatformInfo {
    val osVersion: String
    val manufacturer: String
    val model: String
    val appId: String
    val appVersion: String
    val appDebuggable: Boolean
    fun networkConnectionType(): NetworkConnectionType
}
