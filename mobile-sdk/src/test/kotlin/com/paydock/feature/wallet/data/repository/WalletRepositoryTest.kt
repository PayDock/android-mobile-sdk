package com.paydock.feature.wallet.data.repository

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.injection.modules.mockFailureNetworkModule
import com.paydock.core.data.injection.modules.mockSuccessNetworkModule
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.wallet.data.dto.CaptureChargeResponse
import com.paydock.feature.wallet.data.dto.CaptureWalletChargeRequest
import com.paydock.feature.wallet.data.dto.ChargeDeclineResponse
import com.paydock.feature.wallet.data.dto.WalletCallbackRequest
import com.paydock.feature.wallet.data.dto.WalletCallbackResponse
import com.paydock.feature.wallet.data.dto.WalletConfigResponse
import com.paydock.feature.wallet.data.mapper.asEntity
import com.paydock.feature.wallet.domain.repository.WalletRepository
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

internal class WalletRepositoryTest : BaseKoinUnitTest() {

    private val httpMockClient: HttpClient by inject()

    private val testScope: TestScope by inject()
    private lateinit var repository: WalletRepository

    @Test
    fun `GIVEN valid GooglePay wallet request WHEN capturing charge THEN should succeed with wallet capture response resource`() =
        testScope.runTest {
            // GIVEN
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            repository = WalletRepositoryImpl(get(), httpMockClient)

            val request =
                readResourceFile("charges/valid_googlepay_capture_wallet_charge_request.json")
                    .convertToDataClass<CaptureWalletChargeRequest>()
            val response =
                readResourceFile("charges/success_capture_wallet_response.json")
                    .convertToDataClass<CaptureChargeResponse>()
            val entity = response.asEntity()
            // WHEN - Call the method to be tested
            val result = repository.captureWalletTransaction(accessToken, request)
            // THEN - Verify the result
            assertNotNull(response)
            assertEquals(entity, result)
        }

    @Test
    fun `GIVEN valid PayPal wallet request WHEN capturing charge THEN should succeed with wallet capture response resource`() =
        testScope.runTest {
            // GIVEN
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            repository = WalletRepositoryImpl(get(), httpMockClient)

            val request =
                readResourceFile("charges/valid_paypal_capture_wallet_charge_request.json")
                    .convertToDataClass<CaptureWalletChargeRequest>()
            val response =
                readResourceFile("charges/success_capture_wallet_response.json")
                    .convertToDataClass<CaptureChargeResponse>()
            val entity = response.asEntity()
            // WHEN - Call the method to be tested
            val result = repository.captureWalletTransaction(accessToken, request)
            // THEN - Verify the result
            assertNotNull(response)
            assertEquals(entity, result)
        }

    @Test
    fun `GIVEN valid Afterpay wallet request WHEN capturing charge THEN should succeed with wallet capture response resource`() =
        testScope.runTest {
            // GIVEN
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            repository = WalletRepositoryImpl(get(), httpMockClient)

            val request =
                readResourceFile("charges/valid_afterpay_capture_wallet_charge_request.json")
                    .convertToDataClass<CaptureWalletChargeRequest>()
            val response =
                readResourceFile("charges/success_capture_wallet_response.json")
                    .convertToDataClass<CaptureChargeResponse>()
            val entity = response.asEntity()
            // WHEN - Call the method to be tested
            val result = repository.captureWalletTransaction(accessToken, request)
            // THEN - Verify the result
            assertNotNull(response)
            assertEquals(entity, result)
        }

    @Test(expected = ClientRequestException::class)
    fun `GIVEN invalid access token WHEN capturing Google Pay charge THEN should fail with error response resource`() =
        testScope.runTest {
            val invalidAccessToken = MobileSDKTestConstants.Wallet.MOCK_INVALID_WALLET_TOKEN

            unloadKoinModules(mockSuccessNetworkModule)
            loadKoinModules(mockFailureNetworkModule)
            // GIVEN
            repository = WalletRepositoryImpl(get(), httpMockClient)
            val request =
                readResourceFile("charges/valid_googlepay_capture_wallet_charge_request.json")
                    .convertToDataClass<CaptureWalletChargeRequest>()
            // WHEN - Call the method to be tested
            val result = repository.captureWalletTransaction(invalidAccessToken, request)
            // THEN - It should throw an exception
            assertNotNull(result)
        }

    @Suppress("MaxLineLength")
    @Test
    fun `GIVEN valid PayPal wallet callback request WHEN fetching wallet callback THEN should succeed with wallet callback response resource`() =
        testScope.runTest {
            // GIVEN
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            repository = WalletRepositoryImpl(get(), httpMockClient)

            val request =
                readResourceFile("charges/valid_paypal_wallet_callback_request.json")
                    .convertToDataClass<WalletCallbackRequest>()
            val response =
                readResourceFile("charges/success_paypal_wallet_callback_response.json")
                    .convertToDataClass<WalletCallbackResponse>()
            val entity = response.asEntity()
            // WHEN - Call the method to be tested
            val result = repository.getWalletCallback(accessToken, request)
            // THEN - Verify the result
            assertNotNull(response)
            assertEquals(entity, result)
        }

    @Suppress("MaxLineLength")
    @Test
    fun `GIVEN valid Afterpay wallet callback request WHEN fetching wallet callback THEN should succeed with wallet callback response resource`() =
        testScope.runTest {
            // GIVEN
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            repository = WalletRepositoryImpl(get(), httpMockClient)

            val request =
                readResourceFile("charges/valid_afterpay_wallet_callback_request.json")
                    .convertToDataClass<WalletCallbackRequest>()
            val response =
                readResourceFile("charges/success_afterpay_wallet_callback_response.json")
                    .convertToDataClass<WalletCallbackResponse>()

            val entity = response.asEntity()
            // WHEN - Call the method to be tested
            val result = repository.getWalletCallback(accessToken, request)
            // THEN - Verify the result

            assertNotNull(response)
            assertEquals(entity, result)
        }

    @Suppress("MaxLineLength")
    @Test
    fun `GIVEN valid FlyPay wallet callback request WHEN fetching wallet callback THEN should succeed with wallet callback response resource`() =
        testScope.runTest {
            // GIVEN
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            repository = WalletRepositoryImpl(get(), httpMockClient)

            val request =
                readResourceFile("charges/valid_flypay_wallet_callback_request.json")
                    .convertToDataClass<WalletCallbackRequest>()
            val response =
                readResourceFile("charges/success_flypay_wallet_callback_response.json")
                    .convertToDataClass<WalletCallbackResponse>()
            val entity = response.asEntity()
            // WHEN - Call the method to be tested
            val result = repository.getWalletCallback(accessToken, request)
            // THEN - Verify the result
            assertNotNull(response)
            assertEquals(entity, result)
        }

    @Test(expected = ClientRequestException::class)
    fun `GIVEN wallet type WHEN fetching wallet callback THEN should fail with error response resource`() =
        testScope.runTest {
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN

            unloadKoinModules(mockSuccessNetworkModule)
            loadKoinModules(mockFailureNetworkModule)
            // GIVEN
            repository = WalletRepositoryImpl(get(), httpMockClient)
            val request =
                readResourceFile("charges/invalid_wallet_callback_request.json")
                    .convertToDataClass<WalletCallbackRequest>()
            // WHEN - Call the method to be tested
            val result = repository.getWalletCallback(accessToken, request)
            // THEN - It should throw an exception
            assertNotNull(result)
        }

    @Suppress("MaxLineLength")
    @Test
    fun `GIVEN valid Afterpay wallet decline request WHEN declining wallet charge THEN should succeed with wallet decline response resource`() =
        testScope.runTest {
            // GIVEN
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val chargeId = MobileSDKTestConstants.Charge.MOCK_CHARGE_ID
            repository = WalletRepositoryImpl(get(), httpMockClient)

            val response =
                readResourceFile("charges/success_afterpay_decline_wallet_charge_response.json")
                    .convertToDataClass<ChargeDeclineResponse>()

            val entity = response.asEntity()
            // WHEN - Call the method to be tested
            val result = repository.declineWalletCharge(accessToken, chargeId)
            // THEN - Verify the result

            assertNotNull(response)
            assertEquals(entity, result)
        }

    @Test(expected = ClientRequestException::class)
    fun `GIVEN wallet type WHEN declining wallet charge THEN should fail with error response resource`() =
        testScope.runTest {
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val chargeId = MobileSDKTestConstants.Charge.MOCK_INVALID_CHARGE_ID

            unloadKoinModules(mockSuccessNetworkModule)
            loadKoinModules(mockFailureNetworkModule)
            // GIVEN
            repository = WalletRepositoryImpl(get(), httpMockClient)
            // WHEN - Call the method to be tested
            val result = repository.declineWalletCharge(accessToken, chargeId)
            // THEN - It should throw an exception
            assertNotNull(result)
        }

    @Test
    fun `GIVEN paypal gatewayId WHEN fetching wallet config THEN should succeed with paypal clientId`() =
        testScope.runTest {
            // GIVEN
            repository = WalletRepositoryImpl(get(), httpMockClient)

            val response =
                readResourceFile("gateway/success_wallet_config_response.json")
                    .convertToDataClass<WalletConfigResponse>()
            val setupToken = response.resource.data?.credentials?.clientAuth
            // WHEN - Call the method to be tested
            val result = repository.getWalletGatewayClientId(
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
            repository = WalletRepositoryImpl(get(), httpMockClient)
            // WHEN - Call the method to be tested
            val result = repository.getWalletGatewayClientId(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.General.MOCK_INVALID_GATEWAY_ID
            )
            // THEN - It should throw an exception
            assertNotNull(result)
        }

}