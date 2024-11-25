package com.paydock.api.gateways.data.repository

import com.paydock.api.gateways.data.dto.WalletConfigResponse
import com.paydock.api.gateways.domain.repository.GatewayRepository
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.injection.modules.mockFailureNetworkModule
import com.paydock.core.data.injection.modules.mockSuccessNetworkModule
import com.paydock.core.network.extensions.convertToDataClass
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.core.component.get
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.test.inject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class GatewayRepositoryTest : BaseKoinUnitTest() {

    private val httpMockClient: HttpClient by inject()

    private val testScope: TestScope by inject()
    private lateinit var repository: GatewayRepository

    @Test
    fun `GIVEN paypal gatewayId WHEN fetching wallet config THEN should succeed with paypal clientId`() =
        testScope.runTest {
            // GIVEN
            repository = GatewayRepositoryImpl(get(), httpMockClient)

            val response =
                readResourceFile("gateway/success_wallet_config_response.json")
                    .convertToDataClass<WalletConfigResponse>()
            val setupToken = response.resource.data?.credentials?.clientAuth
            // WHEN - Call the method to be tested
            val result = repository.getGatewayClientId(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.General.MOCK_GATEWAY_ID
            )
            // THEN - Verify the result
            assertNotNull(response)
            assertEquals(setupToken, result)
        }

    @Test(expected = ClientRequestException::class)
    fun `GIVEN invalid gatewayId WHEN fetching wallet config THEN should fail with error response resource`() =
        testScope.runTest {
            unloadKoinModules(mockSuccessNetworkModule)
            loadKoinModules(mockFailureNetworkModule)
            // GIVEN
            repository = GatewayRepositoryImpl(get(), httpMockClient)
            // WHEN - Call the method to be tested
            val result = repository.getGatewayClientId(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.General.MOCK_INVALID_GATEWAY_ID
            )
            // THEN - It should throw an exception
            assertNotNull(result)
        }

}