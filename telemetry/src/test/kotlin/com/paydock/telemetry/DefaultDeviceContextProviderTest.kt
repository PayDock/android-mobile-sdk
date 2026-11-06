package com.paydock.telemetry

import com.paydock.telemetry.TelemetryTestFakes.FakePlatformInfo
import com.paydock.telemetry.TelemetryTestFakes.MapKeyValueStore
import com.paydock.telemetry.TelemetryTestFakes.SequentialIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [DefaultDeviceContextProvider]: built once per process, connection type read live (R_09). */
internal class DefaultDeviceContextProviderTest {

    private val platform = FakePlatformInfo()
    private val keyValues = MapKeyValueStore()

    private val provider = DefaultDeviceContextProvider(
        platform = platform,
        installationIds = InstallationIdStore(keyValues, SequentialIds()),
        sdkVersion = "9.9.9-test",
        environmentName = "sandbox"
    )

    @Test
    fun current_mapsThePlatformTheIdTheVersionAndTheEnvironment() {
        assertEquals(
            ResourceAttributes(
                serviceVersion = "9.9.9-test",
                deploymentEnvironmentName = "sandbox",
                osVersion = "15",
                deviceManufacturer = "TestMaker",
                deviceModelIdentifier = "TestModel",
                appInstallationId = TelemetryTestFakes.uuid(1),
                appId = "com.example.merchant",
                appVersion = "1.0.0",
                appDebuggable = true
            ),
            provider.current()
        )
    }

    @Test
    fun current_readsThePlatformAndTheIdStoreOnce() {
        val first = provider.current()
        repeat(9) { assertSame(first, provider.current()) }

        assertEquals(1, platform.osVersionReads)
        assertEquals(1, platform.manufacturerReads)
        assertEquals(1, platform.modelReads)
        assertEquals(1, platform.appIdReads)
        assertEquals(1, platform.appVersionReads)
        assertEquals(1, platform.appDebuggableReads)
        assertEquals(1, keyValues.reads)
        assertEquals(1, keyValues.writes)
    }

    @Test
    fun networkConnectionType_isReadLiveEachTime() {
        provider.current()
        platform.connection = NetworkConnectionType.WIFI
        assertEquals(NetworkConnectionType.WIFI, provider.networkConnectionType())
        platform.connection = NetworkConnectionType.CELL
        assertEquals(NetworkConnectionType.CELL, provider.networkConnectionType())
        platform.connection = NetworkConnectionType.NONE
        assertEquals(NetworkConnectionType.NONE, provider.networkConnectionType())

        assertEquals(3, platform.connectionReads)
    }

    @Test
    fun construction_readsNothingAndWritesNothing() {
        assertTrue(keyValues.values.isEmpty())
        assertEquals(0, keyValues.reads)
        assertEquals(0, platform.osVersionReads)
    }
}
