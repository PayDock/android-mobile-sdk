package com.paydock.feature.card.data.repository

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.injection.modules.mockFailureNetworkModule
import com.paydock.core.data.injection.modules.mockSuccessNetworkModule
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.utils.reader.LocalFileReader
import com.paydock.feature.card.data.dto.CardSchemasResponse
import com.paydock.feature.card.data.dto.CardTokenResponse
import com.paydock.feature.card.data.dto.CreateCardPaymentTokenRequest
import com.paydock.feature.card.data.mapper.asEntity
import com.paydock.feature.card.domain.repository.CardRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.mockk.coEvery
import io.mockk.mockk
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

class CardRepositoryTest : BaseKoinUnitTest() {

    private val httpMockClient: HttpClient by inject()

    private val testScope: TestScope by inject()
    private lateinit var repository: CardRepository
    private val fileReader: LocalFileReader = mockk()

    @Test
    fun `GIVEN valid credit card request WHEN tokenising card THEN should succeed with card token response resource`() =
        testScope.runTest {
            // GIVEN
            repository = CardRepositoryImpl(get(), httpMockClient, fileReader)

            val request =
                readResourceFile("card/valid_tokenise_credit_card_request.json")
                    .convertToDataClass<CreateCardPaymentTokenRequest.TokeniseCardRequest.CreditCard>()
            val response =
                readResourceFile("card/success_card_token_response.json")
                    .convertToDataClass<CardTokenResponse>()
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
            repository = CardRepositoryImpl(get(), httpMockClient, fileReader)
            val request =
                readResourceFile("card/invalid_tokenise_credit_card_request.json")
                    .convertToDataClass<CreateCardPaymentTokenRequest.TokeniseCardRequest.CreditCard>()
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
            repository = CardRepositoryImpl(get(), httpMockClient, fileReader)
            val request =
                readResourceFile("card/valid_tokenise_credit_card_request.json")
                    .convertToDataClass<CreateCardPaymentTokenRequest.TokeniseCardRequest.CreditCard>()
            val response =
                readResourceFile("card/success_card_token_response.json")
                    .convertToDataClass<CardTokenResponse>()
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
            repository = CardRepositoryImpl(get(), httpMockClient, fileReader)
            val request =
                readResourceFile("card/invalid_tokenise_credit_card_request.json")
                    .convertToDataClass<CreateCardPaymentTokenRequest.TokeniseCardRequest.CreditCard>()
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
            repository = CardRepositoryImpl(get(), httpMockClient, fileReader)

            val request =
                readResourceFile("card/valid_tokenise_gift_card_request.json")
                    .convertToDataClass<CreateCardPaymentTokenRequest.TokeniseCardRequest.GiftCard>()
            val response =
                readResourceFile("card/success_card_token_response.json")
                    .convertToDataClass<CardTokenResponse>()
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
            repository = CardRepositoryImpl(get(), httpMockClient, fileReader)
            val request =
                readResourceFile("card/invalid_tokenise_gift_card_request.json")
                    .convertToDataClass<CreateCardPaymentTokenRequest.TokeniseCardRequest.GiftCard>()
            // WHEN - Call the method to be tested
            val result = repository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )
            // THEN - It should throw an exception
            assertNotNull(result)
        }

    @Test
    fun `GIVEN valid access token WHEN fetching card schemas THEN should succeed with card schemas response list`() =
        testScope.runTest {
            // GIVEN
            val mockResource = readResourceFile("card/success_get_card_schemas_response.json")
            coEvery {
                fileReader.readFileFromAssets(any())
            } returns mockResource

            repository = CardRepositoryImpl(
                get(),
                httpMockClient,
                fileReader
            )
            val expectedResult = mockResource.convertToDataClass<CardSchemasResponse>().asEntity()
            // WHEN - Call the method to be tested
            val result = repository.getCardSchemas()
            // THEN - Verify the result
            assertNotNull(result)
            assertEquals(expectedResult, result)
        }

    @Test(expected = IllegalArgumentException::class)
    fun `GIVEN invalid access token WHEN fetching card schemas THEN should fail with error response resource`() =
        testScope.runTest {
            unloadKoinModules(mockSuccessNetworkModule)
            loadKoinModules(mockFailureNetworkModule)
            // GIVEN
            val expectedResult = IllegalArgumentException("Unable to parse json file.")
            coEvery {
                fileReader.readFileFromAssets(any())
            } throws expectedResult

            repository = CardRepositoryImpl(
                get(),
                httpMockClient,
                fileReader
            )

            // WHEN - Call the method to be tested
            val result = repository.getCardSchemas()
            // THEN - It should throw an exception
            assertNotNull(result)
        }

}