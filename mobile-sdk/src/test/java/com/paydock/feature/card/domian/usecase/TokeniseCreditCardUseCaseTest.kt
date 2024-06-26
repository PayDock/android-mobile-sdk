package com.paydock.feature.card.domian.usecase

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.network.error.ApiErrorResponse
import com.paydock.core.data.network.error.ErrorSummary
import com.paydock.core.domain.error.exceptions.ApiException
import com.paydock.core.extensions.convertToDataClass
import com.paydock.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.feature.card.domain.model.TokenisedCardDetails
import com.paydock.feature.card.domain.repository.CardDetailsRepository
import com.paydock.feature.card.domain.usecase.TokeniseCreditCardUseCase
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TokeniseCreditCardUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: CardDetailsRepository
    private lateinit var tokeniseCreditCardUseCase: TokeniseCreditCardUseCase

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        tokeniseCreditCardUseCase = TokeniseCreditCardUseCase(mockRepository)
    }

    @Test
    fun `test valid tokenise credit card request returns expected card resource`() = runTest {
        // GIVEN
        val request =
            readResourceFile("card/valid_tokenise_credit_card_request.json").convertToDataClass<TokeniseCardRequest.CreditCard>()
        val expectedResult =
            TokenisedCardDetails(
                type = "token",
                token = MobileSDKTestConstants.Card.MOCK_CARD_TOKEN
            )
        coEvery { mockRepository.tokeniseCardDetails(request) } returns expectedResult
        // WHEN
        val actualResult = tokeniseCreditCardUseCase(request)
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) { mockRepository.tokeniseCardDetails(request) }
    }

    @Test
    fun `test invalid tokenise credit card request returns expected error resource`() = runTest {
        // GIVEN
        val request =
            readResourceFile("card/invalid_tokenise_credit_card_request.json").convertToDataClass<TokeniseCardRequest.CreditCard>()
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
        coEvery { mockRepository.tokeniseCardDetails(request) } throws expectedResult
        // WHEN
        val actualResult = tokeniseCreditCardUseCase(request)
        // THEN
        assertTrue(actualResult.isFailure)
        assertEquals(expectedResult, actualResult.exceptionOrNull())
        coVerify(exactly = 1) { mockRepository.tokeniseCardDetails(request) }
    }

}