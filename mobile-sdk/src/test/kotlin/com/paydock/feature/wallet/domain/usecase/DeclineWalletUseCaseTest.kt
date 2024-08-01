package com.paydock.feature.wallet.domain.usecase

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.dto.error.toApiError
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.wallet.data.api.dto.WalletDeclineResponse
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

class DeclineWalletUseCaseTest : BaseKoinUnitTest() {

    private lateinit var mockRepository: WalletRepository
    private lateinit var useCase: DeclineWalletTransactionUseCase

    @BeforeTest
    fun setUp() {
        mockRepository = mockk()
        useCase = DeclineWalletTransactionUseCase(mockRepository)
    }

    @Test
    fun `test valid Afterpay decline wallet chargeId returns expected charge response`() = runTest {
        // GIVEN
        val validAccessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        val validChargeId = MobileSDKTestConstants.Charge.MOCK_CHARGE_ID
        val response =
            readResourceFile("wallet/success_afterpay_decline_wallet_charge_response.json").convertToDataClass<WalletDeclineResponse>()
        val expectedResult = response.asEntity()
        coEvery {
            mockRepository.declineWalletTransaction(
                validAccessToken,
                validChargeId
            )
        } returns expectedResult
        // WHEN
        val actualResult = useCase(validAccessToken, validChargeId)
        // THEN
        assertTrue(actualResult.isSuccess)
        assertEquals(expectedResult, actualResult.getOrNull())
        coVerify(exactly = 1) {
            mockRepository.declineWalletTransaction(
                validAccessToken,
                validChargeId
            )
        }
    }

    @Test
    fun `test invalid Afterpay decline wallet chargeId returns expected charge response`() =
        runTest {
            // GIVEN
            val validAccessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val validChargeId = MobileSDKTestConstants.Charge.MOCK_INVALID_CHARGE_ID
            val response =
                readResourceFile("wallet/failure_decline_wallet_invalid_chargeid_response.json").convertToDataClass<ApiErrorResponse>()
            val expectedResult = response.toApiError()
            coEvery {
                mockRepository.declineWalletTransaction(
                    validAccessToken,
                    validChargeId
                )
            } throws expectedResult
            // WHEN
            val actualResult = useCase(validAccessToken, validChargeId)
            // THEN
            assertTrue(actualResult.isFailure)
            assertEquals(expectedResult, actualResult.exceptionOrNull())
            coVerify(exactly = 1) {
                mockRepository.declineWalletTransaction(
                    validAccessToken,
                    validChargeId
                )
            }
        }

    @Test
    fun `test invalid access token returns expected error resource`() = runTest {
        // GIVEN
        val invalidAccessToken = MobileSDKTestConstants.Wallet.MOCK_INVALID_WALLET_TOKEN
        val validChargeId = MobileSDKTestConstants.Charge.MOCK_INVALID_CHARGE_ID

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
            mockRepository.declineWalletTransaction(
                invalidAccessToken,
                validChargeId
            )
        } throws expectedResult
        // WHEN
        val actualResult = useCase(invalidAccessToken, validChargeId)
        // THEN
        assertTrue(actualResult.isFailure)
        assertEquals(expectedResult, actualResult.exceptionOrNull())
        coVerify(exactly = 1) {
            mockRepository.declineWalletTransaction(
                invalidAccessToken,
                validChargeId
            )
        }
    }

}