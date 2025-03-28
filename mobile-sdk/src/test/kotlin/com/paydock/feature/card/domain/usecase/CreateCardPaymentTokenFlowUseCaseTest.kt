package com.paydock.feature.card.domain.usecase

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.card.data.dto.CreateCardPaymentTokenRequest
import com.paydock.feature.card.domain.model.ui.TokenDetails
import com.paydock.feature.card.domain.repository.CardRepository
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CreateCardPaymentTokenFlowUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: CardRepository
    private lateinit var tokeniseCreditCardUseCase: CreateCardPaymentTokenFlowUseCase

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        tokeniseCreditCardUseCase = CreateCardPaymentTokenFlowUseCase(mockRepository)
    }

    @Test
    fun `test valid tokenise credit card request returns expected card resource`() = runTest {
        // GIVEN
        val request =
            readResourceFile("card/valid_tokenise_credit_card_request.json")
                .convertToDataClass<CreateCardPaymentTokenRequest.TokeniseCardRequest.CreditCard>()
        val expectedResult =
            TokenDetails(
                type = "token",
                token = MobileSDKTestConstants.Card.MOCK_CARD_TOKEN
            )
        coEvery {
            mockRepository.createPaymentTokenFlow(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
        } returns flowOf(expectedResult)
        // WHEN
        val actualFlowResult = tokeniseCreditCardUseCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
        // THEN
        // Assert the result is success and contains the expected data
        actualFlowResult.collect { result ->
            assertTrue(result.isSuccess)
            assertEquals(expectedResult, result.getOrNull())
            coVerify(exactly = 1) {
                mockRepository.createPaymentTokenFlow(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
            }
        }
    }

    @Test
    fun `test invalid tokenise credit card request returns expected error resource`() = runTest {
        // GIVEN
        val request =
            readResourceFile("card/invalid_tokenise_credit_card_request.json")
                .convertToDataClass<CreateCardPaymentTokenRequest.TokeniseCardRequest.CreditCard>()
        val expectedResult =
            ApiException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    summary = ErrorSummary(
                        code = "bad_request",
                        message = MobileSDKTestConstants.Errors.MOCK_INVALID_CARD_DETAILS_ERROR
                    )
                )
            )
        coEvery {
            mockRepository.createPaymentTokenFlow(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
        } returns flow { throw expectedResult }
        // WHEN
        val actualFlowResult = tokeniseCreditCardUseCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
        // THEN
        // Assert the result is success and contains the expected data
        actualFlowResult.collect { result ->
            assertTrue(result.isFailure)
            assertEquals(expectedResult, result.exceptionOrNull())
            coVerify(exactly = 1) {
                mockRepository.createPaymentTokenFlow(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
            }
        }
    }

}