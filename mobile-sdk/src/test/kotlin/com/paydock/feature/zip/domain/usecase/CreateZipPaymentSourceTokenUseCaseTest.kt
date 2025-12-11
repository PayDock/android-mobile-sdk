package com.paydock.feature.zip.domain.usecase

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.domain.error.exceptions.ZipException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.feature.zip.domain.repository.ZipRepository
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

internal class CreateZipPaymentSourceTokenUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: ZipRepository
    private lateinit var createZipPaymentSourceTokenUseCase: CreateZipPaymentSourceTokenUseCase

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        createZipPaymentSourceTokenUseCase = CreateZipPaymentSourceTokenUseCase(mockRepository)
    }

    @Test
    fun `test valid create payment source token request returns expected token`() = runTest {
        // GIVEN
        val expectedToken = MobileSDKTestConstants.Zip.MOCK_PAYMENT_SOURCE_TOKEN

        coEvery {
            mockRepository.createPaymentSourceToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.Zip.MOCK_CHECKOUT_TOKEN,
                MobileSDKTestConstants.General.MOCK_GATEWAY_ID
            )
        } returns expectedToken

        // WHEN
        val actualResult = createZipPaymentSourceTokenUseCase(
            MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
            MobileSDKTestConstants.Zip.MOCK_CHECKOUT_TOKEN,
            MobileSDKTestConstants.General.MOCK_GATEWAY_ID
        )

        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedToken, actualResult.getOrNull())
        coVerify(exactly = 1) {
            mockRepository.createPaymentSourceToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.Zip.MOCK_CHECKOUT_TOKEN,
                MobileSDKTestConstants.General.MOCK_GATEWAY_ID
            )
        }
    }

    @Test
    fun `test create payment source token with invalid checkout token returns expected error`() = runTest {
        // GIVEN
        val expectedException = ApiException(
            error = ApiErrorResponse(
                status = HttpStatusCode.BadRequest.value,
                summary = ErrorSummary(
                    code = "invalid_checkout_token",
                    message = "Invalid checkout token"
                )
            )
        )

        coEvery {
            mockRepository.createPaymentSourceToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                "invalid_token",
                MobileSDKTestConstants.General.MOCK_GATEWAY_ID
            )
        } throws expectedException

        // WHEN
        val actualResult = createZipPaymentSourceTokenUseCase(
            MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
            "invalid_token",
            MobileSDKTestConstants.General.MOCK_GATEWAY_ID
        )

        // THEN
        assertTrue(actualResult.isFailure)
        assertIs<ZipException.CreatingPaymentSourceTokenException>(actualResult.exceptionOrNull())
        coVerify(exactly = 1) {
            mockRepository.createPaymentSourceToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                "invalid_token",
                MobileSDKTestConstants.General.MOCK_GATEWAY_ID
            )
        }
    }

    @Test
    fun `test create payment source token with server error returns expected error`() = runTest {
        // GIVEN
        val expectedException = ApiException(
            error = ApiErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                summary = ErrorSummary(
                    code = "token_creation_failed",
                    message = "Failed to create payment source token"
                )
            )
        )

        coEvery {
            mockRepository.createPaymentSourceToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.Zip.MOCK_CHECKOUT_TOKEN,
                MobileSDKTestConstants.General.MOCK_GATEWAY_ID
            )
        } throws expectedException

        // WHEN
        val actualResult = createZipPaymentSourceTokenUseCase(
            MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
            MobileSDKTestConstants.Zip.MOCK_CHECKOUT_TOKEN,
            MobileSDKTestConstants.General.MOCK_GATEWAY_ID
        )

        // THEN
        assertTrue(actualResult.isFailure)
        assertIs<ZipException.CreatingPaymentSourceTokenException>(actualResult.exceptionOrNull())
    }
}
