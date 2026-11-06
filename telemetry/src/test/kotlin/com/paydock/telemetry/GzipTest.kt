package com.paydock.telemetry

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.zip.GZIPInputStream

/** Unit tests for [Gzip]. */
internal class GzipTest {

    @Test
    fun compress_roundTripsThroughGzipInputStream() {
        val json = """{"resourceLogs":[{"scopeLogs":[{"scope":{"name":"paydock.telemetry"}}]}]}""".repeat(50)
            .toByteArray(Charsets.UTF_8)

        val compressed = Gzip.compress(json)

        assertArrayEquals(json, GZIPInputStream(compressed.inputStream()).use { it.readBytes() })
    }

    @Test
    fun compress_writesTheGzipMagicNumber() {
        val compressed = Gzip.compress(ByteArray(0))

        assertEquals(0x1f.toByte(), compressed[0])
        assertEquals(0x8b.toByte(), compressed[1])
        assertArrayEquals(ByteArray(0), GZIPInputStream(compressed.inputStream()).use { it.readBytes() })
    }
}
