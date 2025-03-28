package com.paydock.feature.card.domain.usecase

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.domain.error.exceptions.GiftCardException
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
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreateGiftCardPaymentTokenUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: CardRepository
    private lateinit var tokeniseCreditCardUseCase: CreateGiftCardPaymentTokenUseCase

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        tokeniseCreditCardUseCase = CreateGiftCardPaymentTokenUseCase(mockRepository)
    }

    @Test
    fun `test valid tokenise credit card request returns expected card resource`() = runTest {
        // GIVEN
        val request =
            readResourceFile("card/valid_tokenise_gift_card_request.json")
                .convertToDataClass<CreateCardPaymentTokenRequest.TokeniseCardRequest.GiftCard>()
        val expectedResult =
            TokenDetails(
                type = "token",
                token = MobileSDKTestConstants.Card.MOCK_CARD_TOKEN
            )
        coEvery {
            mockRepository.createPaymentToken(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
        } returns expectedResult
        // WHEN
        val actualResult = tokeniseCreditCardUseCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) { mockRepository.createPaymentToken(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request) }
    }

    @Test
    fun `test invalid tokenise credit card request returns expected error resource`() = runTest {
        // GIVEN
        val request =
            readResourceFile("card/invalid_tokenise_gift_card_request.json")
                .convertToDataClass<CreateCardPaymentTokenRequest.TokeniseCardRequest.GiftCard>()
        val expectedResult =
            ApiException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    summary = ErrorSummary(
                        code = "bad_request",
                        message = MobileSDKTestConstants.Errors.MOCK_INVALID_GIFT_CARD_DETAILS_ERROR
                    )
                )
            )
        coEvery {
            mockRepository.createPaymentToken(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
        } throws expectedResult
        // WHEN
        val actualResult = tokeniseCreditCardUseCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
        // THEN
        assertTrue(actualResult.isFailure)
        assertIs<GiftCardException.TokenisingCardException>(actualResult.exceptionOrNull())
        coVerify(exactly = 1) { mockRepository.createPaymentToken(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request) }
    }

}