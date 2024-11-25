package com.paydock.core.data.network.auth

import com.paydock.core.BaskMockServerUnitTest
import com.paydock.core.data.injection.modules.mockAuthInterceptorOkHttpModule
import com.paydock.core.data.injection.modules.mockSuccessNetworkModule
import com.paydock.core.network.interceptor.AuthInterceptor
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.koin.core.context.GlobalContext

/**
 * Unit test for the [AuthInterceptor] using [MockWebServer].
 */
internal class AuthInterceptorTest : BaskMockServerUnitTest() {

    /**
     * Test case to verify that [AuthInterceptor] correctly adds the public key as a header to requests.
     */
    @Test
    fun `should succeed using AuthInterceptor to add public key to requests`() = runTest {
        // Unload the mock network module and load the sslSuccessNetworkTestModule.
        GlobalContext.unloadKoinModules(mockSuccessNetworkModule)
        GlobalContext.loadKoinModules(mockAuthInterceptorOkHttpModule)

        // Sample request URL
        val requestUrl = "http://${mockServer.hostName}:${mockServer.port}"

        // Define the expected header name and value
        val expectedHeaderName = "x-user-public-key"
        val expectedHeaderValue = "sample_public_key"

        // Enqueue a mock response from the server
        val responseBody = "Mock response body"
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(responseBody)
        mockServer.enqueue(mockResponse)

        // Make the HTTP request
        val response: HttpResponse = httpClient.get(requestUrl)

        // Verify the request intercepted by the AuthInterceptor
        val request = mockServer.takeRequest()
        val actualHeaderValue = request.getHeader(expectedHeaderName)

        // Verify the response body
        assertEquals(responseBody, response.bodyAsText())

        // Verify the intercepted header value
        assertEquals(expectedHeaderValue, actualHeaderValue)
    }
}