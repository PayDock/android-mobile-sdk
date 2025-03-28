package com.paydock.feature.paypal.data.repository

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.injection.modules.mockFailureNetworkModule
import com.paydock.core.data.injection.modules.mockSuccessNetworkModule
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.paypal.core.data.dto.CreateSetupTokenRequest
import com.paydock.feature.paypal.core.data.dto.PayPalVaultTokenRequest
import com.paydock.feature.paypal.core.data.dto.PayPalVaultTokenResponse
import com.paydock.feature.paypal.core.data.dto.SetupTokenResponse
import com.paydock.feature.paypal.core.data.mapper.asEntity
import com.paydock.feature.paypal.core.data.repository.PayPalRepositoryImpl
import com.paydock.feature.paypal.core.domain.repository.PayPalRepository
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

class PayPalRepositoryTest : BaseKoinUnitTest() {

    private val httpMockClient: HttpClient by inject()

    private val testScope: TestScope by inject()
    private lateinit var repository: PayPalRepository

    @Test
    fun `GIVEN valid paypal vault request WHEN tokenising paypal vault card THEN should succeed with payment token response resource`() =
        testScope.runTest {
            // GIVEN
            repository = PayPalRepositoryImpl(get(), httpMockClient)

            val request =
                readResourceFile("paypal/valid_create_payment_token_request.json")
                    .convertToDataClass<PayPalVaultTokenRequest>()
            val response =
                readResourceFile("paypal/success_payment_token_response.json")
                    .convertToDataClass<PayPalVaultTokenResponse>()
            val entity = response.asEntity()
            // WHEN - Call the method to be tested
            val result = repository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.PayPalVault.MOCK_SETUP_TOKEN,
                request
            )
            // THEN - Verify the result
            assertNotNull(response)
            assertEquals(entity, result)
        }

    @Test(expected = ClientRequestException::class)
    fun `GIVEN invalid paypal payment token request WHEN tokenising paypal vault card THEN should fail with error response resource`() =
        testScope.runTest {
            unloadKoinModules(mockSuccessNetworkModule)
            loadKoinModules(mockFailureNetworkModule)
            // GIVEN
            repository = PayPalRepositoryImpl(get(), httpMockClient)
            val request =
                readResourceFile("paypal/invalid_payment_token_request.json")
                    .convertToDataClass<PayPalVaultTokenRequest>()
            // WHEN - Call the method to be tested
            val result = repository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.PayPalVault.MOCK_SETUP_TOKEN,
                request
            )
            // THEN - It should throw an exception
            assertNotNull(result)
        }

    @Test
    fun `GIVEN valid setup token request WHEN creating paypal setup token THEN should succeed with setup token response resource`() =
        testScope.runTest {
            // GIVEN
            repository = PayPalRepositoryImpl(get(), httpMockClient)

            val request =
                readResourceFile("paypal/valid_create_setup_token_request.json")
                    .convertToDataClass<CreateSetupTokenRequest>()
            val response =
                readResourceFile("paypal/success_setup_token_response.json")
                    .convertToDataClass<SetupTokenResponse>()
            val setupToken = response.resource.data?.setupToken
            // WHEN - Call the method to be tested
            val result = repository.createSetupToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )
            // THEN - Verify the result
            assertNotNull(response)
            assertEquals(setupToken, result)
        }

    @Test(expected = ClientRequestException::class)
    fun `GIVEN invalid setup token request WHEN creating paypal setup token THEN should fail with error response resource`() =
        testScope.runTest {
            unloadKoinModules(mockSuccessNetworkModule)
            loadKoinModules(mockFailureNetworkModule)
            // GIVEN
            repository = PayPalRepositoryImpl(get(), httpMockClient)
            val request =
                readResourceFile("paypal/invalid_create_setup_token_request.json")
                    .convertToDataClass<CreateSetupTokenRequest>()
            // WHEN - Call the method to be tested
            val result = repository.createSetupToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )
            // THEN - It should throw an exception
            assertNotNull(result)
        }

}