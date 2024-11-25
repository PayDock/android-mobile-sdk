package com.paydock.api.tokens.domain.usecase

import com.paydock.api.tokens.data.dto.CreateSessionTokenAuthRequest
import com.paydock.api.tokens.domain.model.SessionAuthToken
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
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreateSessionAuthUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: TokenRepository
    private lateinit var createSessionAuthTokenUseCase: CreateSessionAuthTokenUseCase

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        createSessionAuthTokenUseCase = CreateSessionAuthTokenUseCase(mockRepository)
    }

    @Test
    fun `test valid create session request returns expected paypal session auth result`() = runTest {
        // GIVEN
        val request =
            readResourceFile("token/valid_create_session_auth_request.json").convertToDataClass<CreateSessionTokenAuthRequest>()
        val expectedResult =
            SessionAuthToken(
                accessToken = MobileSDKTestConstants.PayPalVault.MOCK_ACCESS_TOKEN,
                idToken = MobileSDKTestConstants.PayPalVault.MOCK_ID_TOKEN
            )
        coEvery {
            mockRepository.createSessionAuthToken(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
        } returns expectedResult
        // WHEN
        val actualResult = createSessionAuthTokenUseCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) {
            mockRepository.createSessionAuthToken(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
        }
    }

    @Test
    fun `test invalid create session request returns expected error result`() = runTest {
        // GIVEN
        val request =
            readResourceFile("token/invalid_create_session_auth_request.json")
                .convertToDataClass<CreateSessionTokenAuthRequest>()
        val expectedResult =
            ApiException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    summary = ErrorSummary(
                        code = "bad_request",
                        message = MobileSDKTestConstants.Errors.MOCK_INVALID_GATEWAY_ID_ERROR
                    )
                )
            )
        coEvery {
            mockRepository.createSessionAuthToken(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
        } throws expectedResult
        // WHEN
        val actualResult = createSessionAuthTokenUseCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
        // THEN
        assertTrue(actualResult.isFailure)
        assertIs<PayPalVaultException.CreateSessionAuthTokenException>(actualResult.exceptionOrNull())
        coVerify(exactly = 1) {
            mockRepository.createSessionAuthToken(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, request)
        }
    }

}