package com.paydock.feature.wallet.domain.usecase

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.network.error.ApiErrorResponse
import com.paydock.core.data.network.error.ErrorSummary
import com.paydock.core.domain.error.exceptions.ApiException
import com.paydock.core.extensions.convertToDataClass
import com.paydock.feature.wallet.data.api.dto.WalletCallbackRequest
import com.paydock.feature.wallet.data.api.dto.WalletCallbackResponse
import com.paydock.feature.wallet.data.mapper.asEntity
import com.paydock.feature.wallet.domain.repository.WalletRepository
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetWalletCallbackUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: WalletRepository
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
            readResourceFile("wallet/valid_paypal_wallet_callback_request.json").convertToDataClass<WalletCallbackRequest>()
        val response =
            readResourceFile("wallet/success_paypal_wallet_callback_response.json").convertToDataClass<WalletCallbackResponse>()
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
            readResourceFile("wallet/valid_afterpay_wallet_callback_request.json").convertToDataClass<WalletCallbackRequest>()
        val response =
            readResourceFile("wallet/success_afterpay_wallet_callback_response.json").convertToDataClass<WalletCallbackResponse>()
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
            readResourceFile("wallet/valid_flypay_wallet_callback_request.json").convertToDataClass<WalletCallbackRequest>()
        val response =
            readResourceFile("wallet/success_flypay_wallet_callback_response.json").convertToDataClass<WalletCallbackResponse>()
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
            readResourceFile("wallet/invalid_wallet_callback_request.json").convertToDataClass<WalletCallbackRequest>()
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