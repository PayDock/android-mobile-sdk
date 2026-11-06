package com.paydock.telemetry

import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.Buffer
import okio.ForwardingSource
import okio.buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

/** Unit tests for [DropErrorBodyInterceptor], against a hand-written [Interceptor.Chain]. */
internal class DropErrorBodyInterceptorTest {

    private val request = Request.Builder().url("https://otlp.invalid/v1/logs").build()

    /** A body that records whether it was closed. */
    private class TrackedBody(text: String) {
        var closed = false
            private set
        private val source = object : ForwardingSource(Buffer().writeUtf8(text)) {
            override fun close() {
                closed = true
                super.close()
            }
        }
        val body = source.buffer().asResponseBody("application/json".toMediaType())
    }

    private class FakeChain(private val request: Request, private val response: Response) : Interceptor.Chain {
        override fun request(): Request = request
        override fun proceed(request: Request): Response = response
        override fun connection(): Connection? = null
        override fun call(): Call = throw UnsupportedOperationException()
        override fun connectTimeoutMillis(): Int = 0
        override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
        override fun readTimeoutMillis(): Int = 0
        override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
        override fun writeTimeoutMillis(): Int = 0
        override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
    }

    private fun response(code: Int, body: TrackedBody, vararg headers: Pair<String, String>): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("status $code")
            .apply { headers.forEach { (name, value) -> header(name, value) } }
            .body(body.body)
            .build()

    @Test
    fun intercept_errorResponse_keepsStatusAndHeadersAndEmptiesTheBody() {
        val original = TrackedBody("""{"error":{"message":"rate limited"}}""")

        val result = DropErrorBodyInterceptor.intercept(
            FakeChain(request, response(429, original, "Retry-After" to "7", "Content-Type" to "application/json"))
        )

        assertEquals(429, result.code)
        assertEquals("7", result.header("Retry-After"))
        assertEquals("application/json", result.header("Content-Type"))
        assertEquals("", result.body.string())
        assertTrue("the original body must be closed", original.closed)
    }

    @Test
    fun intercept_serverError_emptiesTheBody() {
        val original = TrackedBody("upstream unavailable")

        val result = DropErrorBodyInterceptor.intercept(FakeChain(request, response(503, original)))

        assertEquals(503, result.code)
        assertEquals("", result.body.string())
        assertTrue(original.closed)
    }

    @Test
    fun intercept_successResponse_isReturnedUntouched() {
        val original = TrackedBody("""{"partialSuccess":{"rejectedLogRecords":"2"}}""")
        val success = response(200, original)

        val result = DropErrorBodyInterceptor.intercept(FakeChain(request, success))

        assertSame(success, result)
        assertEquals("""{"partialSuccess":{"rejectedLogRecords":"2"}}""", result.body.string())
    }
}
