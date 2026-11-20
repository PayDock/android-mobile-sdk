package com.paydock.telemetry

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Golden fixtures (R_01 to R_03, R_11, spec § 9.2): each shared and Android input, run through [RecordFactory],
 * [Allowlist.filter], [SendStamp.stamp] and [OtlpLogEncoder.encode], equals its `*.android.expected.json` as parsed
 * JSON. Arrays compare in order, so attribute order counts. `crash-anr` proves the late stamp and
 * `connection-failure` the clock offset. A failure here is fixed in the factory or the encoder, never in the copied
 * fixture.
 */
internal class GoldenFixturesTest {

    @Test
    fun fixtures_areTheSixteenSharedAndAndroidInputs() {
        val inputs = GoldenFixtures.inputs()

        assertEquals(inputs.joinToString { it.path }, EXPECTED_INPUTS, inputs.size)
        inputs.forEach { input ->
            val expected = GoldenFixtures.expectedFile(input)
            assertTrue("missing $expected", expected.isFile)
        }
    }

    @Test
    fun everyInput_encodesToItsExpectedFile() {
        val resource = GoldenFixtures.resource()

        GoldenFixtures.inputs().forEach { input ->
            val actual = GoldenFixtures.encode(GoldenFixtures.read(input), resource)
            val expected = GoldenFixtures.expectedFile(input).readText(Charsets.UTF_8)

            assertEquals(input.name, Json.parseToJsonElement(expected), Json.parseToJsonElement(actual))
        }
    }

    private companion object {
        const val EXPECTED_INPUTS = 16
    }
}
