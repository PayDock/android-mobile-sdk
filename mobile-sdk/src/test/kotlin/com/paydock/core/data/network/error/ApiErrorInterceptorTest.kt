package com.paydock.core.data.network.error

import com.paydock.core.BaskMockServerUnitTest
import com.paydock.core.data.injection.modules.mockApiInterceptorOkHttpModule
import com.paydock.core.data.injection.modules.mockSuccessNetworkModule
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.exceptions.UnknownApiException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.GlobalContext
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@Suppress("MaxLineLength")
internal class ApiErrorInterceptorTest : BaskMockServerUnitTest() {

    @Before
    fun setup() {
        // Unload the mock network module and load the mockApiInterceptorOkHttpModule.
        GlobalContext.unloadKoinModules(mockSuccessNetworkModule)
        GlobalContext.loadKoinModules(mockApiInterceptorOkHttpModule)
    }

    /**
     * Test the [ApiErrorInterceptor] behavior when the response is successful.
     * The interceptor should return the original response in case of a successful response.
     */
    @Test
    fun `should succeed using ApiInterceptor with expected response body`() = runTest {
        // Sample request URL
        val requestUrl = "http://${mockServer.hostName}:${mockServer.port}"

        // Define the mock response body
        val responseBody = "Success response body"
        val mockResponse = MockResponse()
            .setResponseCode(HttpStatusCode.OK.value)
            .setBody(responseBody)
        mockServer.enqueue(mockResponse)

        // Make the HTTP request
        val response: HttpResponse = httpClient.get(requestUrl)

        // Verify that the response body matches the expected value
        assertEquals(responseBody, response.bodyAsText())
    }

    /**
     * Test the [ApiErrorInterceptor] behavior when the response is not successful and contains an error body.
     * The interceptor should handle the error body and throw an [ApiException] with the corresponding error information.
     */
    @Test
    fun `should fail tokenise credit card request with ApiErrorListResponse and throw an ApiException with expected error information`() =
        runTest {
            // Sample request URL
            val requestUrl = "http://${mockServer.hostName}:${mockServer.port}"
            // Sample error body in JSON format.
            val errorBodyJson = readResourceFile("card/failure_credit_card_token_response.json")
            val mockResponse = MockResponse()
                .setResponseCode(HttpStatusCode.BadRequest.value)
                .setBody(errorBodyJson)
            mockServer.enqueue(mockResponse)

            // Make the HTTP request and expect it to throw a [ResponseException] with the corresponding error information.
            val exception = try {
                httpClient.get(requestUrl)
                null // If no exception is thrown, set the exception to null.
            } catch (e: Exception) {
                e // If a [ApiErrorInterceptor] exception is thrown, store it in the 'exception' variable.
            }
            // Verify that the interceptor throws a [ApiErrorInterceptor] exception with the corresponding error information.
            assertIs<UnknownApiException>(exception)
            assertNotNull(exception.errorBody)
            assertEquals(
                HttpStatusCode.BadRequest.value,
                exception.status
            )
            assertEquals("Unexpected error model - unable to decode JSON", exception.message)
        }

    /**
     * Test the [ApiErrorInterceptor] behavior when the response is not successful and contains an error body.
     * The interceptor should handle the error body and throw an [ApiException] with the corresponding error information.
     */
    @Test
    fun `should fail wallet callback request with ApiErrorListResponse and throw an ApiException with expected error information`() =
        runTest {
            // Sample request URL
            val requestUrl = "http://${mockServer.hostName}:${mockServer.port}"
            // Sample error body in JSON format.
            val errorBodyJson = readResourceFile("wallet/failure_wallet_callback_response.json")
            val mockResponse = MockResponse()
                .setResponseCode(HttpStatusCode.BadRequest.value)
                .setBody(errorBodyJson)
            mockServer.enqueue(mockResponse)

            // Make the HTTP request and expect it to throw a [ResponseException] with the corresponding error information.
            val exception = try {
                httpClient.get(requestUrl)
                null // If no exception is thrown, set the exception to null.
            } catch (e: Exception) {
                e // If a [ApiErrorInterceptor] exception is thrown, store it in the 'exception' variable.
            }
            // Verify that the interceptor throws a [ApiErrorInterceptor] exception with the corresponding error information.
            assertIs<ApiException>(exception)
            assertNotNull(exception.error)
            assertIs<ApiErrorResponse>(exception.error)
            assertEquals(
                HttpStatusCode.BadRequest.value,
                exception.error.status
            )
            assertEquals("body validation failed", exception.message)
        }

    /**
     * Test the [ApiErrorInterceptor] behavior when the response is not successful and contains an error body.
     * The interceptor should handle the error body and throw an [ApiException] with the corresponding error information.
     */
    @Test
    fun `should fail wallet capture request with ApiErrorResponse and throw an ApiException with expected error information`() =
        runTest {
            // Sample request URL
            val requestUrl = "http://${mockServer.hostName}:${mockServer.port}"
            // Sample error body in JSON format.
            val errorBodyJson = readResourceFile("wallet/failure_capture_wallet_response.json")
            val mockResponse = MockResponse()
                .setResponseCode(HttpStatusCode.BadRequest.value)
                .setBody(errorBodyJson)
            mockServer.enqueue(mockResponse)

            // Make the HTTP request and expect it to throw a [ResponseException] with the corresponding error information.
            val exception = try {
                httpClient.get(requestUrl)
                null // If no exception is thrown, set the exception to null.
            } catch (e: Exception) {
                e // If a [ApiErrorInterceptor] exception is thrown, store it in the 'exception' variable.
            }
            // Verify that the interceptor throws a [ApiErrorInterceptor] exception with the corresponding error information.
            assertIs<ApiException>(exception)
            assertNotNull(exception.error)
            assertIs<ApiErrorResponse>(exception.error)
            assertEquals(
                HttpStatusCode.Forbidden.value,
                exception.error.status
            )
            assertEquals("Access forbidden", exception.message)
        }

    /**
     * Test the [ApiErrorInterceptor] behavior when the response is not successful and contains an error body.
     * The interceptor should handle the error body and throw an [ApiException] with the corresponding error information.
     */
    @Test
    fun `should fail wallet capture request with ApiErrorMessagesListResponse and throw an ApiException with expected error information`() =
        runTest {
            // Sample request URL
            val requestUrl = "http://${mockServer.hostName}:${mockServer.port}"
            // Sample error body in JSON format.
            val errorBodyJson = readResourceFile("wallet/failure_capture_wallet_full_response.json")
            val mockResponse = MockResponse()
                .setResponseCode(HttpStatusCode.BadRequest.value)
                .setBody(errorBodyJson)
            mockServer.enqueue(mockResponse)

            // Make the HTTP request and expect it to throw a [ResponseException] with the corresponding error information.
            val exception = try {
                httpClient.get(requestUrl)
                null // If no exception is thrown, set the exception to null.
            } catch (e: Exception) {
                e // If a [ApiErrorInterceptor] exception is thrown, store it in the 'exception' variable.
            }
            // Verify that the interceptor throws a [ApiErrorInterceptor] exception with the corresponding error information.
            assertIs<UnknownApiException>(exception)
            assertNotNull(exception.errorBody)
            assertEquals(
                HttpStatusCode.BadRequest.value,
                exception.status
            )
            assertEquals("Unexpected error model - unable to decode JSON", exception.message)
        }

    /**
     * Test the [ApiErrorInterceptor] behavior when the response is not successful and does not contain an error body.
     * The interceptor should return the response with an empty error body to prevent an [ResponseException].
     */
    @Test
    fun `should fail and throw an ServerResponseException not being able to map to ApiException`() =
        runTest {
            // Sample request URL
            val requestUrl = "http://${mockServer.hostName}:${mockServer.port}"
            // Sample error body in JSON format.
            val mockResponse = MockResponse()
                .setResponseCode(HttpStatusCode.InternalServerError.value)
            mockServer.enqueue(mockResponse)

            // Make the HTTP request
            val exception = try {
                httpClient.get(requestUrl)
                null // If no exception is thrown, set the exception to null.
            } catch (e: Exception) {
                e // If a [ServerResponseException] exception is thrown, store it in the 'exception' variable.
            }
            // Verify that the response code matches the expected value (500 Internal Server Error)
            assertTrue(exception is ServerResponseException)
            assertEquals(
                HttpStatusCode.InternalServerError,
                (exception as ServerResponseException).response.status
            )
        }
}