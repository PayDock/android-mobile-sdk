package com.paydock.feature.googlepay.data.repository

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.injection.modules.mockFailureNetworkModule
import com.paydock.core.data.injection.modules.mockSuccessNetworkModule
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.googlepay.data.dto.CreateGooglePayTokenRequest
import com.paydock.feature.googlepay.data.dto.GooglePayTokenResponse
import com.paydock.feature.googlepay.data.mapper.asEntity
import com.paydock.feature.googlepay.domain.repository.GooglePayRepository
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

internal class GooglePayRepositoryTest : BaseKoinUnitTest() {

    private val httpMockClient: HttpClient by inject()
    private val testScope: TestScope by inject()
    private lateinit var repository: GooglePayRepository

    @Test
    fun `GIVEN valid Google Pay token request WHEN creating payment token THEN should succeed with token details`() =
        testScope.runTest {
            // GIVEN
            repository = GooglePayRepositoryImpl(get(), httpMockClient)

            val request = CreateGooglePayTokenRequest(
                serviceId = "test_service_id",
                serviceType = "GooglePay",
                serviceGroup = "wallet",
                payload = "base64EncodedPayload",
                payloadFormat = "encrypted_string"
            )

            val response =
                readResourceFile("googlepay/success_googlepay_token_response.json")
                    .convertToDataClass<GooglePayTokenResponse>()
            val entity = response.asEntity()

            // WHEN - Call the method to be tested
            val result = repository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )

            // THEN - Verify the result
            assertNotNull(response)
            assertEquals(entity, result)
        }

    @Test(expected = ClientRequestException::class)
    fun `GIVEN invalid Google Pay token request WHEN creating payment token THEN should fail with error response`() =
        testScope.runTest {
            unloadKoinModules(mockSuccessNetworkModule)
            loadKoinModules(mockFailureNetworkModule)
            // GIVEN
            repository = GooglePayRepositoryImpl(get(), httpMockClient)
            val request = CreateGooglePayTokenRequest(
                serviceId = "invalid_service_id",
                serviceType = "GooglePay",
                serviceGroup = "wallet",
                payload = "invalidPayload",
                payloadFormat = "encrypted_string"
            )
            // WHEN - Call the method to be tested
            val result = repository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )
            // THEN - It should throw an exception
            assertNotNull(result)
        }

    @Test
    fun `GIVEN valid request WHEN creating payment token THEN should return correct token type`() =
        testScope.runTest {
            // GIVEN
            repository = GooglePayRepositoryImpl(get(), httpMockClient)

            val request = CreateGooglePayTokenRequest(
                serviceId = "test_service_id",
                serviceType = "GooglePay",
                serviceGroup = "wallet",
                payload = "base64EncodedPayload",
                payloadFormat = "encrypted_string"
            )

            val response =
                readResourceFile("googlepay/success_googlepay_token_response.json")
                    .convertToDataClass<GooglePayTokenResponse>()
            val entity = response.asEntity()

            // WHEN
            val result = repository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )

            // THEN
            assertNotNull(response)
            assertEquals(entity, result)
        }
}
