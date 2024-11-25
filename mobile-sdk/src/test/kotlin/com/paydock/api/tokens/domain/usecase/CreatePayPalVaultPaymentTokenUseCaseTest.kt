package com.paydock.api.tokens.domain.usecase

import com.paydock.api.tokens.data.dto.CreatePaymentTokenRequest
import com.paydock.api.tokens.domain.model.PayPalPaymentTokenDetails
import com.paydock.api.tokens.domain.repository.TokenRepository
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.extensions.convertToDataClass
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreatePayPalVaultPaymentTokenUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: TokenRepository
    private lateinit var createPayPalVaultPaymentTokenUseCase: CreatePayPalVaultPaymentTokenUseCase

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        createPayPalVaultPaymentTokenUseCase = CreatePayPalVaultPaymentTokenUseCase(mockRepository)
    }

    @Test
    fun `test valid payment token request returns expected ott payment token resource`() = runTest {
        // GIVEN
        val request =
            readResourceFile("token/valid_create_payment_token_request.json")
                .convertToDataClass<CreatePaymentTokenRequest.PayPalVaultRequest>()
        val mockToken = MobileSDKTestConstants.PayPalVault.MOCK_PAYMENT_TOKEN
        val mockEmail = MobileSDKTestConstants.PayPalVault.MOCK_EMAIL
        val expectedResult =
            PayPalPaymentTokenDetails(token = mockToken, email = mockEmail)
        coEvery {
            mockRepository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.PayPalVault.MOCK_SETUP_TOKEN,
                request
            )
        } returns expectedResult
        // WHEN
        val actualResult =
            createPayPalVaultPaymentTokenUseCase(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.PayPalVault.MOCK_SETUP_TOKEN,
                request
            )
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) {
            mockRepository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                any(),
                request
            )
        }
    }

    @Test
    fun `test invalid payment token request returns expected error resource`() = runTest {
        // GIVEN
        val request =
            readResourceFile("token/invalid_payment_token_request.json").convertToDataClass<CreatePaymentTokenRequest.PayPalVaultRequest>()
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
            mockRepository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.PayPalVault.MOCK_SETUP_TOKEN,
                request
            )
        } throws expectedResult
        // WHEN
        val actualResult =
            createPayPalVaultPaymentTokenUseCase(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.PayPalVault.MOCK_SETUP_TOKEN,
                request
            )
        // THEN
        assertTrue(actualResult.isFailure)
        assertIs<PayPalVaultException.CreatePaymentTokenException>(actualResult.exceptionOrNull())
        coVerify(exactly = 1) {
            mockRepository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.PayPalVault.MOCK_SETUP_TOKEN,
                request
            )
        }
    }

}