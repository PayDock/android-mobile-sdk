package com.paydock.feature.paypal.domain.usecase

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.paypal.core.data.dto.CreateSetupTokenRequest
import com.paydock.feature.paypal.core.domain.repository.PayPalRepository
import com.paydock.feature.paypal.core.domain.usecase.CreateSetupTokenUseCase
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

internal class CreateSetupTokenUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: PayPalRepository
    private lateinit var createSetupTokenUseCase: CreateSetupTokenUseCase

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        createSetupTokenUseCase =
            CreateSetupTokenUseCase(mockRepository)
    }

    @Test
    fun `test valid create setup token request returns expected paypal setup token result`() =
        runTest {
            // GIVEN
            val request =
                readResourceFile("paypal/valid_create_setup_token_request.json").convertToDataClass<CreateSetupTokenRequest>()
            val expectedResult = MobileSDKTestConstants.PayPalVault.MOCK_SETUP_TOKEN
            coEvery {
                mockRepository.createSetupToken(
                    MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                    request
                )
            } returns expectedResult
            // WHEN
            val actualResult =
                createSetupTokenUseCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
            // THEN
            assertTrue(actualResult.isSuccess)
            assertEquals(expectedResult, actualResult.getOrNull())
            coVerify(exactly = 1) {
                mockRepository.createSetupToken(
                    MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                    request
                )
            }
        }

    @Test
    fun `test invalid create session request returns expected error result`() = runTest {
        // GIVEN
        val request =
            readResourceFile("paypal/invalid_create_setup_token_request.json")
                .convertToDataClass<CreateSetupTokenRequest>()
        val expectedResult =
            ApiException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    summary = ErrorSummary(
                        code = "bad_request",
                        message = MobileSDKTestConstants.Errors.MOCK_INVALID_OAUTH_TOKEN_ERROR
                    )
                )
            )
        coEvery {
            mockRepository.createSetupToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )
        } throws expectedResult
        // WHEN
        val actualResult =
            createSetupTokenUseCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
        // THEN
        assertTrue(actualResult.isFailure)
        assertIs<PayPalVaultException.CreateSetupTokenException>(actualResult.exceptionOrNull())
        coVerify(exactly = 1) {
            mockRepository.createSetupToken(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                request
            )
        }
    }

}