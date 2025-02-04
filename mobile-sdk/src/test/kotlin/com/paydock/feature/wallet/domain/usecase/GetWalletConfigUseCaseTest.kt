package com.paydock.feature.wallet.domain.usecase

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.feature.paypal.core.domain.usecase.GetPayPalClientIdUseCase
import com.paydock.feature.wallet.domain.repository.WalletRepository
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

internal class GetWalletConfigUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: WalletRepository
    private lateinit var getPayPalClientIdUseCase: GetPayPalClientIdUseCase

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        getPayPalClientIdUseCase = GetPayPalClientIdUseCase(mockRepository)
    }

    @Test
    fun `test valid gatewayId returns expected paypal clientId result`() = runTest {
        // GIVEN
        val expectedResult = MobileSDKTestConstants.PayPalVault.MOCK_CLIENT_ID
        coEvery {
            mockRepository.getWalletGatewayClientId(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.General.MOCK_GATEWAY_ID
            )
        } returns expectedResult
        // WHEN
        val actualResult =
            getPayPalClientIdUseCase(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.General.MOCK_GATEWAY_ID
            )
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) {
            mockRepository.getWalletGatewayClientId(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.General.MOCK_GATEWAY_ID
            )
        }
    }

    @Test
    fun `test invalid gatewayId returns expected error result`() = runTest {
        // GIVEN
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
            mockRepository.getWalletGatewayClientId(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.General.MOCK_INVALID_GATEWAY_ID
            )
        } throws expectedResult
        // WHEN
        val actualResult =
            getPayPalClientIdUseCase(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.General.MOCK_INVALID_GATEWAY_ID
            )
        // THEN
        assertTrue(actualResult.isFailure)
        assertIs<PayPalVaultException.GetPayPalClientIdException>(actualResult.exceptionOrNull())
        coVerify(exactly = 1) {
            mockRepository.getWalletGatewayClientId(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.General.MOCK_INVALID_GATEWAY_ID
            )
        }
    }

}