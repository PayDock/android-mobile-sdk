package com.paydock.telemetry

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Empties every non-2xx response body before anything parses it: the original body is closed and the response comes
 * back with the same status and headers and an empty body. The networking library's error interceptor then sees a
 * blank body and never throws, so the status and `Retry-After` reach the transport. No error body is ever read.
 * A 2xx response is returned untouched.
 */
internal object DropErrorBodyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.isSuccessful) return response
        response.body.close()
        return response.newBuilder().body(ByteArray(0).toResponseBody()).build()
    }
}
