package com.paydock.core

import com.paydock.core.data.injection.modules.mockServerModule
import io.ktor.client.HttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.koin.core.context.GlobalContext
import org.koin.test.inject

abstract class BaskMockServerUnitTest : BaseKoinUnitTest() {

    // Create an OkHttp mock server
    protected val mockServer: MockWebServer by inject()
    protected val httpClient: HttpClient by inject()

    @Before
    fun setupUpServer() {
        GlobalContext.loadKoinModules(mockServerModule)
        mockServer.start()
    }

    @After
    fun tearDownServer() {
        // Shutdown the mock server
        mockServer.shutdown()
        httpClient.close()
    }
}