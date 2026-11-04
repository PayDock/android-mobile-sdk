package com.paydock.telemetry

/** The device's network at export time, as the spec names it. */
internal enum class NetworkConnectionType(val value: String) {
    WIFI("wifi"),
    CELL("cell"),
    NONE("none"),
    UNKNOWN("unknown"),
}
