package com.paydock.feature.wallet.domain.usecase

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.wallet.data.dto.CaptureChargeResponse
import com.paydock.feature.wallet.data.dto.CaptureWalletChargeRequest
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

internal class CaptureWalletUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: WalletRepository
    private lateinit var captureWalletChargeUseCase: CaptureWalletChargeUseCase

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        captureWalletChargeUseCase = CaptureWalletChargeUseCase(mockRepository)
    }

    @Test
    fun `test valid PayPal capture wallet request returns expected charge response`() = runTest {
        // GIVEN
        val validAccessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        val request =
            readResourceFile("wallet/valid_paypal_capture_wallet_charge_request.json").convertToDataClass<CaptureWalletChargeRequest>()
        val response =
            readResourceFile("wallet/success_capture_wallet_response.json").convertToDataClass<CaptureChargeResponse>()
        val expectedResult = response.asEntity()
        coEvery {
            mockRepository.captureWalletTransaction(
                validAccessToken,
                request
            )
        } returns expectedResult
        // WHEN
        val actualResult = captureWalletChargeUseCase(validAccessToken, request)
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) { mockRepository.captureWalletTransaction(validAccessToken, request) }
    }

    @Test
    fun `test valid GooglePay capture wallet request returns expected charge response`() = runTest {
        // GIVEN
        val validAccessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        val request =
            readResourceFile("wallet/valid_googlepay_capture_wallet_charge_request.json").convertToDataClass<CaptureWalletChargeRequest>()
        val response =
            readResourceFile("wallet/success_capture_wallet_response.json").convertToDataClass<CaptureChargeResponse>()
        val expectedResult = response.asEntity()
        coEvery {
            mockRepository.captureWalletTransaction(
                validAccessToken,
                request
            )
        } returns expectedResult
        // WHEN
        val actualResult = captureWalletChargeUseCase(validAccessToken, request)
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) { mockRepository.captureWalletTransaction(validAccessToken, request) }
    }

    @Test
    fun `test valid Afterpay capture wallet request returns expected charge response`() = runTest {
        // GIVEN
        val validAccessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        val request =
            readResourceFile("wallet/valid_afterpay_capture_wallet_charge_request.json").convertToDataClass<CaptureWalletChargeRequest>()
        val response =
            readResourceFile("wallet/success_capture_wallet_response.json").convertToDataClass<CaptureChargeResponse>()
        val expectedResult = response.asEntity()
        coEvery {
            mockRepository.captureWalletTransaction(
                validAccessToken,
                request
            )
        } returns expectedResult
        // WHEN
        val actualResult = captureWalletChargeUseCase(validAccessToken, request)
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) { mockRepository.captureWalletTransaction(validAccessToken, request) }
    }

    @Test
    fun `test invalid access token returns expected error resource`() = runTest {
        // GIVEN
        val invalidAccessToken = MobileSDKTestConstants.Wallet.MOCK_INVALID_WALLET_TOKEN

        val request =
            readResourceFile("wallet/valid_paypal_capture_wallet_charge_request.json").convertToDataClass<CaptureWalletChargeRequest>()
        val expectedResult =
            ApiException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.InternalServerError.value,
                    summary = ErrorSummary(
                        code = "unexpected_error",
                        message = "Unexpected error during process",
                    )
                )
            )
        coEvery {
            mockRepository.captureWalletTransaction(
                invalidAccessToken,
                request
            )
        } throws expectedResult
        // WHEN
        val actualResult = captureWalletChargeUseCase(invalidAccessToken, request)
        // THEN
        assertTrue(actualResult.isFailure)
        assertEquals(expectedResult, actualResult.exceptionOrNull())
        coVerify(exactly = 1) {
            mockRepository.captureWalletTransaction(
                invalidAccessToken,
                request
            )
        }
    }

}