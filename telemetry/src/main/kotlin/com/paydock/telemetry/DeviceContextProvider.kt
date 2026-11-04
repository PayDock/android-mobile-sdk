package com.paydock.telemetry

/** The device context attached to every export (R_12). */
internal interface DeviceContextProvider {
    fun current(): ResourceAttributes // built once per process, then cached
    fun networkConnectionType(): NetworkConnectionType // read live, per batch
}
