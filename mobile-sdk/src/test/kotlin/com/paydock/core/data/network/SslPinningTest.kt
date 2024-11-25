package com.paydock.core.data.network

import android.content.Context
import com.paydock.MobileSDK
import com.paydock.core.BaseUnitTest
import com.paydock.core.data.injection.modules.mockSuccessNetworkModule
import com.paydock.core.data.injection.modules.sslFailNetworkTestModule
import com.paydock.core.data.injection.modules.sslSuccessNetworkTestModule
import com.paydock.core.domain.model.Environment
import com.paydock.core.network.exceptions.ApiException
import com.paydock.initializeMobileSDK
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.path
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.koin.core.context.GlobalContext.loadKoinModules
import org.koin.core.context.GlobalContext.unloadKoinModules
import org.koin.core.context.stopKoin
import org.koin.test.inject
import javax.net.ssl.SSLPeerUnverifiedException
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.fail

/**
 * This class contains unit tests for testing SSL pinning.
 */
internal class SslPinningTest : BaseUnitTest() {

    private lateinit var context: Context

    @Before
    override fun setUpMocks() {
        super.setUpMocks()
        // Mock the Context object
        context = mockk()
        // Configure the getApplicationContext() method to return the mock Context
        every { context.applicationContext } returns context
    }

    @After
    fun resetMocks() {
        MobileSDK.reset() // Reset MobileSDK before each test
    }

    @After
    fun tearDownKoin() {
        // As the SDK will startKoin, we need to ensure that after each test we stop koin to be able to restart it in each test
        stopKoin()
    }

    /**
     * Tests that the application fails if the certificate for `paydock.com` is not pinned.
     */
    @Test
    fun `should fail if incorrect certificate is pinned`() = runTest {
        val environment = Environment.STAGING

        context.initializeMobileSDK(environment)
        // Unload the mock network module and load the sslFailNetworkTestModule.
        unloadKoinModules(mockSuccessNetworkModule)
        loadKoinModules(sslFailNetworkTestModule)

        // Create a client and try to make a request to `https://paydock.com`.
        val client: HttpClient by inject()
        assertFails { client.get("https://paydock.com") }
    }

    /**
     * Tests that the application fails if using invalid hostname that does not match `paydock.com'.
     */
    @Test
    fun `should fail if requesting invalid hostname`() = runTest {
        val environment = Environment.STAGING

        context.initializeMobileSDK(environment)
        // Unload the mock network module and load the sslSuccessNetworkTestModule.
        unloadKoinModules(mockSuccessNetworkModule)
        loadKoinModules(sslSuccessNetworkTestModule)

        // Create a client and try to make a request to `https://publicobject.com`.
        val client: HttpClient by inject()
        assertFails { client.get("https://publicobject.com") }
    }

    /**
     * Tests that the application succeeds (throw ApiException and not SSLPeerUnverifiedException)
     * if the certificate for STAGING is pinned.
     */
    @Test
    fun `should not throw SSLPeerUnverifiedException if certificate is pinned correctly on STAGING`() = runTest {
        val environment = Environment.STAGING

        context.initializeMobileSDK(environment)
        // Unload the mock network module and load the sslSuccessNetworkTestModule.
        unloadKoinModules(mockSuccessNetworkModule)
        loadKoinModules(sslSuccessNetworkTestModule)

        // Create a client and try to make a request to `https://www.commbank.com.au/`.
        val client: HttpClient by inject()

        // Assert that the request succeeds and the status code is 200.

        try {
            client.post {
                url { path("/v1/charges/66ab783fbf97c12c63bd312e") }
            }
        } catch (e: SSLPeerUnverifiedException) {
            fail(e.message)
        } catch (e: Exception) {
            // It should fail here to invalid authentication: Access forbidden
            assertIs<ApiException>(e)
        }
    }

    /**
     * Tests that the application succeeds (throw ApiException and not SSLPeerUnverifiedException)
     * if the certificate for PRE_PRODUCTION is pinned.
     */
    @Test
    fun `should not throw SSLPeerUnverifiedException if certificate is pinned correctly on PRE_PRODUCTION`() = runTest {
        val environment = Environment.SANDBOX

        context.initializeMobileSDK(environment)
        // Unload the mock network module and load the sslSuccessNetworkTestModule.
        unloadKoinModules(mockSuccessNetworkModule)
        loadKoinModules(sslSuccessNetworkTestModule)

        // Create a client and try to make a request to `https://www.commbank.com.au/`.
        val client: HttpClient by inject()

        // Assert that the request succeeds and the status code is 200.

        try {
            client.post {
                url { path("/v1/charges/66ab783fbf97c12c63bd312e") }
            }
        } catch (e: SSLPeerUnverifiedException) {
            fail(e.message)
        } catch (e: Exception) {
            // It should fail here to invalid authentication: Access forbidden
            assertIs<ApiException>(e)
        }
    }

    /**
     * Tests that the application succeeds (throw ApiException and not SSLPeerUnverifiedException)
     * if the certificate for PRODUCTION is pinned.
     */
    @Test
    fun `should not throw SSLPeerUnverifiedException if certificate is pinned correctly on PRODUCTION`() = runTest {
        val environment = Environment.PRODUCTION

        context.initializeMobileSDK(environment)
        // Unload the mock network module and load the sslSuccessNetworkTestModule.
        unloadKoinModules(mockSuccessNetworkModule)
        loadKoinModules(sslSuccessNetworkTestModule)

        // Create a client and try to make a request to `https://www.commbank.com.au/`.
        val client: HttpClient by inject()

        // Assert that the request succeeds and the status code is 200.

        try {
            client.post {
                url { path("/v1/charges/66ab783fbf97c12c63bd312e") }
            }
        } catch (e: SSLPeerUnverifiedException) {
            fail(e.message)
        } catch (e: Exception) {
            // It should fail here to invalid authentication: Access forbidden
            assertIs<ApiException>(e)
        }
    }
}