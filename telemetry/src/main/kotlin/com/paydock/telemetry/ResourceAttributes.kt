package com.paydock.telemetry

/** Spec § 5.1, once per batch. */
internal data class ResourceAttributes(
    val serviceVersion: String,
    val deploymentEnvironmentName: String,
    val osVersion: String,
    val deviceManufacturer: String,
    val deviceModelIdentifier: String,
    val appInstallationId: String,
    val appId: String,
    val appVersion: String,
    val appDebuggable: Boolean,
) {
    /**
     * The 14 § 5.1 keys with their documented types: Int for spec_version, Boolean for debuggable, String otherwise.
     */
    fun asMap(): Map<String, Any> = linkedMapOf(
        "service.name" to SERVICE_NAME,
        "service.namespace" to SERVICE_NAMESPACE,
        "service.version" to serviceVersion,
        "paydock.sdk.platform" to SDK_PLATFORM,
        "paydock.telemetry.spec_version" to SPEC_VERSION,
        "deployment.environment.name" to deploymentEnvironmentName,
        "os.name" to OS_NAME,
        "os.version" to osVersion,
        "device.manufacturer" to deviceManufacturer,
        "device.model.identifier" to deviceModelIdentifier,
        "app.installation.id" to appInstallationId,
        "paydock.app.id" to appId,
        "paydock.app.version" to appVersion,
        "paydock.app.debuggable" to appDebuggable
    )

    companion object {
        const val SERVICE_NAME = "paydock-mobile-sdk"
        const val SERVICE_NAMESPACE = "paydock"
        const val SDK_PLATFORM = "android"
        const val SPEC_VERSION = 1
        const val OS_NAME = "Android"
    }
}
