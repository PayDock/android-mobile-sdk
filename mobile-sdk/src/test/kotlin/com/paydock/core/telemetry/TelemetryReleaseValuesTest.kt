package com.paydock.core.telemetry

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Release safety (R_06, R_09): reads the release variant's generated `TelemetryBuildValues.kt`, which the build passes
 * as the system property [PROPERTY], and fails if any value is set. A release build must carry no override, no
 * endpoint, no credential and no debug flag, so it sends only to the SDK's built-in collector URL with no credential.
 * A missing property or file fails the test: it never skips.
 */
internal class TelemetryReleaseValuesTest {

    private fun releaseValues(): String {
        val path = System.getProperty(PROPERTY)
        assertNotNull("system property $PROPERTY is not set: the build must pass the release values file", path)
        val file = File(path!!)
        assertTrue("release values file not found: $path", file.isFile)
        return file.readText(Charsets.UTF_8)
    }

    @Test
    fun releaseValues_areDisabledAndEmpty() {
        val source = releaseValues()

        listOf(
            "const val ENABLED: Boolean = false",
            "const val OTLP_ENDPOINT: String = \"\"",
            "const val OTLP_AUTH: String = \"\"",
            "const val LEVEL: String = \"\"",
            "const val SEND_TEST_RECORD: Boolean = false",
            "const val TEST_CRASH: Boolean = false"
        ).forEach { expected ->
            assertTrue("release values must contain `$expected`", source.contains(expected))
        }
    }

    @Test
    fun releaseValues_nameNoGrafanaAndNoBasicCredential() {
        val source = releaseValues()

        assertFalse("release values must not name grafana.net", source.contains("grafana.net"))
        assertFalse("release values must not hold a Basic credential", source.contains("Basic "))
    }

    private companion object {
        const val PROPERTY = "paydock.telemetry.releaseValuesFile"
    }
}
