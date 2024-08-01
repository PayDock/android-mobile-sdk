package com.paydock.feature.googlepay.presentation.viewmodels

import app.cash.turbine.test
import com.google.android.gms.wallet.PaymentsClient
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.GooglePayException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.wallet.domain.usecase.CaptureWalletTransactionUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletTransactionUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal
import kotlin.test.assertIs

@Suppress("MaxLineLength")
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class GooglePayViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: GooglePayViewModel
    private lateinit var paymentsClient: PaymentsClient
    private lateinit var captureWalletTransactionUseCase: CaptureWalletTransactionUseCase
    private lateinit var declineWalletTransactionUseCase: DeclineWalletTransactionUseCase
    private lateinit var getWalletCallbackUseCase: GetWalletCallbackUseCase

    @Before
    fun setup() {
        captureWalletTransactionUseCase = mockk()
        declineWalletTransactionUseCase = mockk()
        getWalletCallbackUseCase = mockk()
        paymentsClient = mockk()
        viewModel = GooglePayViewModel(
            paymentsClient,
            captureWalletTransactionUseCase,
            declineWalletTransactionUseCase,
            getWalletCallbackUseCase,
            dispatchersProvider
        )
    }

    @Test
    fun `setWalletToken should update token`() = runTest {
        val walletToken = "testToken"
        // ACTION
        viewModel.setWalletToken(walletToken)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertEquals(walletToken, state.token)
    }

    @Test
    fun `capture GooglePay wallet charge should update isLoading, call useCase, and update state on success`() =
        runTest {
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val validRefToken = MobileSDKTestConstants.GooglePay.REF_TOKEN
            val mockResult = Result.success(
                ChargeResponse(
                    status = 200,
                    resource = ChargeResponse.ChargeResource(
                        type = "charge",
                        data = ChargeResponse.ChargeData(
                            status = "complete",
                            id = "653784a37cb913091e1be3e5",
                            amount = BigDecimal(10),
                            currency = "AUD"
                        )
                    )
                )
            )
            coEvery { captureWalletTransactionUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.captureWalletTransaction(accessToken, validRefToken)
                // CHECK
                // 4.
                // Initial state
                assertFalse(awaitItem().isLoading)
                // Loading state - before execution
                assertTrue(awaitItem().isLoading)
                coVerify { captureWalletTransactionUseCase(any(), any()) }
                // Resul state - success
                awaitItem().let { state ->
                    assertFalse(state.isLoading)
                    assertEquals(mockResult.getOrNull(), state.chargeData)
                    assertNull(state.error)
                }
            }
        }

    @Test
    fun `capture GooglePay wallet charge should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val invalidAccessToken = MobileSDKTestConstants.Wallet.MOCK_INVALID_WALLET_TOKEN
            val validRefToken = MobileSDKTestConstants.GooglePay.REF_TOKEN
            val mockError = ApiException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.InternalServerError.value,
                    summary = ErrorSummary(
                        code = "unexpected_error",
                        message = MobileSDKTestConstants.Errors.MOCK_GENERAL_ERROR
                    )
                )
            )
            val mockResult = Result.failure<ChargeResponse>(mockError)
            coEvery { captureWalletTransactionUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.captureWalletTransaction(invalidAccessToken, validRefToken)
                // CHECK
                // 4.
                // Initial state
                assertFalse(awaitItem().isLoading)
                // Loading state - before execution
                assertTrue(awaitItem().isLoading)
                coVerify { captureWalletTransactionUseCase(any(), any()) }
                // Resul state - failure
                awaitItem().let { state ->
                    assertFalse(state.isLoading)
                    assertNull(state.chargeData)
                    assertNotNull(state.error)
                    assertIs<GooglePayException.CapturingChargeException>(state.error)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_GENERAL_ERROR,
                        state.error.message
                    )
                }
            }
        }

    @Test
    fun `resetResultState should reset UI state`() = runTest {
        val walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        viewModel.stateFlow.test {
            // ACTION
            viewModel.setWalletToken(walletToken)
            viewModel.resetResultState()
            // Initial state
            assertNull(awaitItem().token)
            assertEquals(walletToken, awaitItem().token)
            // Result state - success
            awaitItem().let { state ->
                assertFalse(state.isLoading)
                assertNull(state.token)
                assertFalse(state.googlePayAvailable)
                assertNull(state.chargeData)
                assertNull(state.error)
            }
        }
    }
}
