package com.paydock.telemetry

/**
 * The device context (R_09): the spec § 5.1 set, built once per process on first [current] (which only
 * [ActiveTelemetry.start] calls, on the worker), and the connection type, read live from the platform.
 */
internal class DefaultDeviceContextProvider(
    private val platform: PlatformInfo,
    private val installationIds: InstallationIdStore,
    private val sdkVersion: String,
    private val environmentName: String,
) : DeviceContextProvider {

    private val attributes: ResourceAttributes by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ResourceAttributes(
            serviceVersion = sdkVersion,
            deploymentEnvironmentName = environmentName,
            osVersion = platform.osVersion,
            deviceManufacturer = platform.manufacturer,
            deviceModelIdentifier = platform.model,
            appInstallationId = installationIds.getOrCreate(),
            appId = platform.appId,
            appVersion = platform.appVersion,
            appDebuggable = platform.appDebuggable
        )
    }

    override fun current(): ResourceAttributes = attributes

    override fun networkConnectionType(): NetworkConnectionType = platform.networkConnectionType()
}
