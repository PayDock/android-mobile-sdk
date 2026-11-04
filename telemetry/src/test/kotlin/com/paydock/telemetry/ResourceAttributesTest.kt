package com.paydock.telemetry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [ResourceAttributes.asMap]: exactly the 14 spec § 5.1 keys, with their documented types (R_09). */
internal class ResourceAttributesTest {

    private val attributes = TelemetryTestFakes.resourceAttributes

    @Test
    fun asMap_hasExactlyTheFourteenKeys() {
        assertEquals(
            setOf(
                "service.name",
                "service.namespace",
                "service.version",
                "paydock.sdk.platform",
                "paydock.telemetry.spec_version",
                "deployment.environment.name",
                "os.name",
                "os.version",
                "device.manufacturer",
                "device.model.identifier",
                "app.installation.id",
                "paydock.app.id",
                "paydock.app.version",
                "paydock.app.debuggable"
            ),
            attributes.asMap().keys
        )
        assertEquals(14, attributes.asMap().size)
    }

    @Test
    fun asMap_specVersionIsInt_debuggableIsBoolean_restAreStrings() {
        val map = attributes.asMap()

        assertEquals(1, map["paydock.telemetry.spec_version"])
        assertTrue(map["paydock.telemetry.spec_version"] is Int)
        assertEquals(true, map["paydock.app.debuggable"])
        assertTrue(map["paydock.app.debuggable"] is Boolean)
        map.filterKeys { it != "paydock.telemetry.spec_version" && it != "paydock.app.debuggable" }
            .forEach { (key, value) -> assertTrue("$key must be a String", value is String) }
    }

    @Test
    fun asMap_carriesTheFixedAndTheGivenValues() {
        val map = attributes.asMap()

        assertEquals("paydock-mobile-sdk", map["service.name"])
        assertEquals("paydock", map["service.namespace"])
        assertEquals("android", map["paydock.sdk.platform"])
        assertEquals("Android", map["os.name"])
        assertEquals(attributes.serviceVersion, map["service.version"])
        assertEquals(attributes.deploymentEnvironmentName, map["deployment.environment.name"])
        assertEquals(attributes.osVersion, map["os.version"])
        assertEquals(attributes.deviceManufacturer, map["device.manufacturer"])
        assertEquals(attributes.deviceModelIdentifier, map["device.model.identifier"])
        assertEquals(TelemetryTestFakes.INSTALLATION_ID, map["app.installation.id"])
        assertEquals(attributes.appId, map["paydock.app.id"])
        assertEquals(attributes.appVersion, map["paydock.app.version"])
    }
}
