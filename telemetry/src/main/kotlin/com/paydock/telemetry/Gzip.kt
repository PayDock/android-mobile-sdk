package com.paydock.telemetry

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

/** gzip for the request body (`Content-Encoding: gzip`). */
internal object Gzip {
    fun compress(bytes: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        GZIPOutputStream(output).use { it.write(bytes) }
        return output.toByteArray()
    }
}
