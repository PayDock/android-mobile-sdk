package com.paydock.api.charges.domain.usecase

import com.paydock.api.charges.data.dto.WalletCallbackRequest
import com.paydock.api.charges.data.dto.WalletCallbackResponse
import com.paydock.api.charges.data.mapper.asEntity
import com.paydock.api.charges.domain.repository.ChargeRepository
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
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
import kotlin.test.assertTrue

internal class GetWalletCallbackUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: ChargeRepository
    private lateinit var useCase: GetWalletCallbackUseCase

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        useCase = GetWalletCallbackUseCase(mockRepository)
    }

    @Test
    fun `test valid PayPal wallet callback request returns expected charge response`() = runTest {
        // GIVEN
        val validAccessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        val request =
            readResourceFile("charges/valid_paypal_wallet_callback_request.json").convertToDataClass<WalletCallbackRequest>()
        val response =
            readResourceFile("charges/success_paypal_wallet_callback_response.json").convertToDataClass<WalletCallbackResponse>()
        val expectedResult = response.asEntity()
        coEvery {
            mockRepository.getWalletCallback(
                validAccessToken,
                request
            )
        } returns expectedResult
        // WHEN
        val actualResult = useCase(validAccessToken, request)
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) { mockRepository.getWalletCallback(validAccessToken, request) }
    }

    @Test
    fun `test valid Afterpay wallet callback request returns expected charge response`() = runTest {
        // GIVEN
        val validAccessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        val request =
            readResourceFile("charges/valid_afterpay_wallet_callback_request.json").convertToDataClass<WalletCallbackRequest>()
        val response =
            readResourceFile("charges/success_afterpay_wallet_callback_response.json").convertToDataClass<WalletCallbackResponse>()
        val expectedResult = response.asEntity()
        coEvery {
            mockRepository.getWalletCallback(
                validAccessToken,
                request
            )
        } returns expectedResult
        // WHEN
        val actualResult = useCase(validAccessToken, request)
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) { mockRepository.getWalletCallback(validAccessToken, request) }
    }

    @Test
    fun `test valid FlyPay wallet callback request returns expected charge response`() = runTest {
        // GIVEN
        val validAccessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        val request =
            readResourceFile("charges/valid_flypay_wallet_callback_request.json").convertToDataClass<WalletCallbackRequest>()
        val response =
            readResourceFile("charges/success_flypay_wallet_callback_response.json").convertToDataClass<WalletCallbackResponse>()
        val expectedResult = response.asEntity()
        coEvery {
            mockRepository.getWalletCallback(
                validAccessToken,
                request
            )
        } returns expectedResult
        // WHEN
        val actualResult = useCase(validAccessToken, request)
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) { mockRepository.getWalletCallback(validAccessToken, request) }
    }

    @Test
    fun `test invalid wallet type request returns expected error resource`() = runTest {
        // GIVEN
        val validAccessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN

        val request =
            readResourceFile("charges/invalid_wallet_callback_request.json").convertToDataClass<WalletCallbackRequest>()
        val expectedResult =
            ApiException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    summary = ErrorSummary(
                        code = "invalid_details",
                        message = "wallet_type must be a valid enum value",
                    )
                )
            )
        coEvery {
            mockRepository.getWalletCallback(
                validAccessToken,
                request
            )
        } throws expectedResult
        // WHEN
        val actualResult = useCase(validAccessToken, request)
        // THEN
        assertTrue(actualResult.isFailure)
        assertEquals(expectedResult, actualResult.exceptionOrNull())
        coVerify(exactly = 1) { mockRepository.getWalletCallback(validAccessToken, request) }
    }

}