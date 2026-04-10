package com.paydock.feature.googlepay.domain.usecase

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.domain.error.exceptions.GooglePayException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.feature.card.domain.model.ui.TokenDetails
import com.paydock.feature.googlepay.data.dto.CreateGooglePayTokenRequest
import com.paydock.feature.googlepay.domain.repository.GooglePayRepository
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

internal class CreateGooglePayTokenUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: GooglePayRepository
    private lateinit var createGooglePayTokenUseCase: CreateGooglePayTokenUseCase

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        createGooglePayTokenUseCase = CreateGooglePayTokenUseCase(mockRepository)
    }

    @Test
    fun `test valid Google Pay token request returns expected token details`() = runTest {
        // GIVEN
        val request = CreateGooglePayTokenRequest(
            serviceId = "test_service_id",
            serviceType = "GooglePay",
            serviceGroup = "wallet",
            payload = "base64EncodedPayload",
            payloadFormat = "encrypted_string"
        )
        val expectedResult = TokenDetails(
            token = MobileSDKTestConstants.Card.MOCK_CARD_TOKEN,
            type = "card_scheme_token"
        )
        coEvery {
            mockRepository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )
        } returns expectedResult
        // WHEN
        val actualResult = createGooglePayTokenUseCase(
            MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
            request
        )
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) {
            mockRepository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )
        }
    }

    @Test
    fun `test invalid Google Pay token request returns expected error`() = runTest {
        // GIVEN
        val request = CreateGooglePayTokenRequest(
            serviceId = "test_service_id",
            serviceType = "GooglePay",
            serviceGroup = "wallet",
            payload = "invalidPayload",
            payloadFormat = "encrypted_string"
        )
        val expectedException = ApiException(
            error = ApiErrorResponse(
                status = HttpStatusCode.BadRequest.value,
                summary = ErrorSummary(
                    code = "bad_request",
                    message = MobileSDKTestConstants.Errors.MOCK_GENERAL_ERROR
                )
            )
        )
        coEvery {
            mockRepository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )
        } throws expectedException
        // WHEN
        val actualResult = createGooglePayTokenUseCase(
            MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
            request
        )
        // THEN
        assertTrue(actualResult.isFailure)
        assertIs<GooglePayException.TokenisingGooglePayException>(actualResult.exceptionOrNull())
        coVerify(exactly = 1) {
            mockRepository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )
        }
    }

    @Test
    fun `test network error returns expected error`() = runTest {
        // GIVEN
        val request = CreateGooglePayTokenRequest(
            serviceId = "test_service_id",
            serviceType = "GooglePay",
            serviceGroup = "wallet",
            payload = "base64EncodedPayload",
            payloadFormat = "encrypted_string"
        )
        val expectedException = ApiException(
            error = ApiErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                summary = ErrorSummary(
                    code = "internal_server_error",
                    message = MobileSDKTestConstants.Errors.MOCK_GENERAL_ERROR
                )
            )
        )
        coEvery {
            mockRepository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )
        } throws expectedException
        // WHEN
        val actualResult = createGooglePayTokenUseCase(
            MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
            request
        )
        // THEN
        assertTrue(actualResult.isFailure)
        assertIs<GooglePayException.TokenisingGooglePayException>(actualResult.exceptionOrNull())
        assertEquals(
            MobileSDKTestConstants.Errors.MOCK_GENERAL_ERROR,
            actualResult.exceptionOrNull()?.message
        )
        coVerify(exactly = 1) {
            mockRepository.createPaymentToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )
        }
    }
}
