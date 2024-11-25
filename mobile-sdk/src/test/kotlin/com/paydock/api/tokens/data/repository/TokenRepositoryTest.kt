package com.paydock.api.tokens.data.repository

import com.paydock.api.tokens.data.dto.CreatePaymentTokenRequest
import com.paydock.api.tokens.data.dto.CreateSessionTokenAuthRequest
import com.paydock.api.tokens.data.dto.CreateSetupTokenRequest
import com.paydock.api.tokens.data.dto.PaymentTokenResponse
import com.paydock.api.tokens.data.dto.SessionAuthTokenResponse
import com.paydock.api.tokens.data.dto.SetupTokenResponse
import com.paydock.api.tokens.data.mapper.asEntity
import com.paydock.api.tokens.domain.repository.TokenRepository
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.injection.modules.mockFailureNetworkModule
import com.paydock.core.data.injection.modules.mockSuccessNetworkModule
import com.paydock.core.network.extensions.convertToDataClass
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.core.component.get
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.test.inject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TokenRepositoryTest : BaseKoinUnitTest() {

    private val httpMockClient: HttpClient by inject()

    private val testScope: TestScope by inject()
    private lateinit var repository: TokenRepository

    @Test
    fun `GIVEN valid credit card request WHEN tokenising card THEN should succeed with card token response resource`() =
        testScope.runTest {
            // GIVEN
            repository = TokenRepositoryImpl(get(), httpMockClient)

            val request =
                readResourceFile("token/valid_tokenise_credit_card_request.json")
                    .convertToDataClass<CreatePaymentTokenRequest.TokeniseCardRequest.CreditCard>()
            val response =
                readResourceFile("token/success_card_token_response.json")
                    .convertToDataClass<PaymentTokenResponse.CardTokenResponse>()
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
    fun `GIVEN invalid credit card request WHEN tokenising card THEN should fail with error response resource`() =
        testScope.runTest {
            unloadKoinModules(mockSuccessNetworkModule)
            loadKoinModules(mockFailureNetworkModule)
            // GIVEN
            repository = TokenRepositoryImpl(get(), httpMockClient)
            val request =
                readResourceFile("token/invalid_tokenise_credit_card_request.json")
                    .convertToDataClass<CreatePaymentTokenRequest.TokeniseCardRequest.CreditCard>()
            // WHEN - Call the method to be tested
            val result = repository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )
            // THEN - It should throw an exception
            assertNotNull(result)
        }

    @Test
    fun `GIVEN valid credit card request WHEN tokenising card THEN should emits TokenisedCardDetails successfully using flow`() =
        testScope.runTest {
            // GIVEN
            repository = TokenRepositoryImpl(get(), httpMockClient)
            val request =
                readResourceFile("token/valid_tokenise_credit_card_request.json")
                    .convertToDataClass<CreatePaymentTokenRequest.TokeniseCardRequest.CreditCard>()
            val response =
                readResourceFile("token/success_card_token_response.json")
                    .convertToDataClass<PaymentTokenResponse.CardTokenResponse>()
            val entity = response.asEntity()

            // WHEN - Call the method to be tested
            val resultFlow = repository.createPaymentTokenFlow(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )

            // THEN - Verify the result
            resultFlow.collect { result ->
                assertEquals(entity.token, result.token)
                assertEquals(entity.type, result.type)
            }
        }

    @Test(expected = ClientRequestException::class)
    fun `GIVEN invalid credit card request WHEN tokenising card THEN should emit failure with error response resource using flow`() =
        testScope.runTest {
            unloadKoinModules(mockSuccessNetworkModule)
            loadKoinModules(mockFailureNetworkModule)
            // GIVEN
            repository = TokenRepositoryImpl(get(), httpMockClient)
            val request =
                readResourceFile("token/invalid_tokenise_credit_card_request.json")
                    .convertToDataClass<CreatePaymentTokenRequest.TokeniseCardRequest.CreditCard>()
            // WHEN - Call the method to be tested
            val tokenisedCardDetailsFlow = repository.createPaymentTokenFlow(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )
            // THEN - It should throw an exception
            val result = tokenisedCardDetailsFlow.catch { throwable ->
                // Handle the exception and return a default value or throw it again if needed
                throw throwable
            }.single() // In this case, we expect only one element in the flow
            assertNotNull(result)
        }

    @Test
    fun `GIVEN valid gift card request WHEN tokenising card THEN should succeed with card token response resource`() =
        testScope.runTest {
            // GIVEN
            repository = TokenRepositoryImpl(get(), httpMockClient)

            val request =
                readResourceFile("token/valid_tokenise_gift_card_request.json")
                    .convertToDataClass<CreatePaymentTokenRequest.TokeniseCardRequest.GiftCard>()
            val response =
                readResourceFile("token/success_card_token_response.json")
                    .convertToDataClass<PaymentTokenResponse.CardTokenResponse>()
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
    fun `GIVEN invalid gift card request WHEN tokenising card THEN should fail with error response resource`() =
        testScope.runTest {
            unloadKoinModules(mockSuccessNetworkModule)
            loadKoinModules(mockFailureNetworkModule)
            // GIVEN
            repository = TokenRepositoryImpl(get(), httpMockClient)
            val request =
                readResourceFile("token/invalid_tokenise_gift_card_request.json")
                    .convertToDataClass<CreatePaymentTokenRequest.TokeniseCardRequest.GiftCard>()
            // WHEN - Call the method to be tested
            val result = repository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )
            // THEN - It should throw an exception
            assertNotNull(result)
        }

    @Test
    fun `GIVEN valid paypal vault request WHEN tokenising paypal vault card THEN should succeed with payment token response resource`() =
        testScope.runTest {
            // GIVEN
            repository = TokenRepositoryImpl(get(), httpMockClient)

            val request =
                readResourceFile("token/valid_create_payment_token_request.json")
                    .convertToDataClass<CreatePaymentTokenRequest.PayPalVaultRequest>()
            val response =
                readResourceFile("token/success_payment_token_response.json")
                    .convertToDataClass<PaymentTokenResponse.PayPalVaultTokenResponse>()
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
            repository = TokenRepositoryImpl(get(), httpMockClient)
            val request =
                readResourceFile("token/invalid_payment_token_request.json")
                    .convertToDataClass<CreatePaymentTokenRequest.PayPalVaultRequest>()
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
    fun `GIVEN valid session auth request WHEN creating paypal oauth token THEN should succeed with session auth response resource`() =
        testScope.runTest {
            // GIVEN
            repository = TokenRepositoryImpl(get(), httpMockClient)

            val request =
                readResourceFile("token/valid_create_session_auth_request.json")
                    .convertToDataClass<CreateSessionTokenAuthRequest>()
            val response =
                readResourceFile("token/success_session_auth_response.json")
                    .convertToDataClass<SessionAuthTokenResponse>()
            val entity = response.asEntity()
            // WHEN - Call the method to be tested
            val result = repository.createSessionAuthToken(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
            // THEN - Verify the result
            assertNotNull(response)
            assertEquals(entity, result)
        }

    @Test(expected = ClientRequestException::class)
    fun `GIVEN invalid session auth request WHEN creating paypal oauth token THEN should fail with error response resource`() =
        testScope.runTest {
            unloadKoinModules(mockSuccessNetworkModule)
            loadKoinModules(mockFailureNetworkModule)
            // GIVEN
            repository = TokenRepositoryImpl(get(), httpMockClient)
            val request =
                readResourceFile("token/invalid_create_session_auth_request.json")
                    .convertToDataClass<CreateSessionTokenAuthRequest>()
            // WHEN - Call the method to be tested
            val result = repository.createSessionAuthToken(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
            // THEN - It should throw an exception
            assertNotNull(result)
        }

    @Test
    fun `GIVEN valid setup token request WHEN creating paypal setup token THEN should succeed with setup token response resource`() =
        testScope.runTest {
            // GIVEN
            repository = TokenRepositoryImpl(get(), httpMockClient)

            val request =
                readResourceFile("token/valid_create_setup_token_request.json")
                    .convertToDataClass<CreateSetupTokenRequest>()
            val response =
                readResourceFile("token/success_setup_token_response.json")
                    .convertToDataClass<SetupTokenResponse>()
            val setupToken = response.resource.data?.setupToken
            // WHEN - Call the method to be tested
            val result = repository.createSetupToken(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
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
            repository = TokenRepositoryImpl(get(), httpMockClient)
            val request =
                readResourceFile("token/invalid_create_setup_token_request.json")
                    .convertToDataClass<CreateSetupTokenRequest>()
            // WHEN - Call the method to be tested
            val result = repository.createSetupToken(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
            // THEN - It should throw an exception
            assertNotNull(result)
        }

}