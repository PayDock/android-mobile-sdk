/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.core.data.network

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.data.injection.modules.mockSuccessNetworkModule
import com.paydock.core.data.injection.modules.sslFailNetworkTestModule
import com.paydock.core.data.injection.modules.sslSuccessNetworkTestModule
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.test.runTest
import org.koin.core.context.GlobalContext.loadKoinModules
import org.koin.core.context.GlobalContext.unloadKoinModules
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

/**
 * This class contains unit tests for testing SSL pinning.
 */
class SslPinningTest : BaseKoinUnitTest() {

    /**
     * Tests that the application fails if the certificate for `paydock.com` is not pinned.
     */
    @Test
    fun `should fail if incorrect certificate is pinned`() = runTest {
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
        // Unload the mock network module and load the sslSuccessNetworkTestModule.
        unloadKoinModules(mockSuccessNetworkModule)
        loadKoinModules(sslSuccessNetworkTestModule)

        // Create a client and try to make a request to `https://publicobject.com`.
        val client: HttpClient by inject()
        assertFails { client.get("https://publicobject.com") }
    }

    /**
     * Tests that the application succeeds if the certificate for `paydock.com` is pinned.
     */
    @Test
    fun `should succeed if certificate is pinned`() = runTest {
        // Unload the mock network module and load the sslSuccessNetworkTestModule.
        unloadKoinModules(mockSuccessNetworkModule)
        loadKoinModules(sslSuccessNetworkTestModule)

        // Create a client and try to make a request to `https://paydock.com`.
        val client: HttpClient by inject()

        // Assert that the request succeeds and the status code is 200.
        val response = client.get("https://paydock.com")
        assertEquals(200, response.status.value)
    }
}