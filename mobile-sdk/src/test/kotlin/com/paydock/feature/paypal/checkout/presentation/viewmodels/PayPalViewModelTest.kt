package com.paydock.feature.paypal.checkout.presentation.viewmodels

import android.net.Uri
import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKConstants
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.PayPalException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.paypal.checkout.presentation.state.PayPalCheckoutUIState
import com.paydock.feature.wallet.data.dto.CaptureChargeResponse
import com.paydock.feature.wallet.data.dto.WalletCallbackResponse
import com.paydock.feature.wallet.data.mapper.asEntity
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.ui.WalletCallback
import com.paydock.feature.wallet.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertIs

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
internal class PayPalViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: PayPalViewModel
    private lateinit var captureWalletChargeUseCase: CaptureWalletChargeUseCase
    private lateinit var declineWalletChargeUseCase: DeclineWalletChargeUseCase
    private lateinit var getWalletCallbackUseCase: GetWalletCallbackUseCase

    @Before
    fun setup() {
        captureWalletChargeUseCase = mockk()
        declineWalletChargeUseCase = mockk()
        getWalletCallbackUseCase = mockk()
        viewModel = PayPalViewModel(
            captureWalletChargeUseCase,
            declineWalletChargeUseCase,
            getWalletCallbackUseCase,
            dispatchersProvider
        )
        val mockToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        viewModel.setWalletToken(mockToken)
    }

    @Test
    fun `parsePayPalUrl should extract PayPal data and populate UI state`() = runTest {
        val mockToken = MobileSDKTestConstants.PayPal.MOCK_TOKEN
        val mockPayerId = MobileSDKTestConstants.PayPal.MOCK_PAYER_ID
        val mockSuccessUrl =
            "https://paydock-mobile.sdk/paypal/success?token=2V6045660E724300D&PayerID=H2G7GULMXJZU6&intent=sale&opType=payment&return_uri="

        val mockUri = mockk<Uri>()
        every { Uri.parse(any()) } returns mockUri
        every { mockUri.getQueryParameter("token") } returns mockToken
        every { mockUri.getQueryParameter(MobileSDKConstants.PayPalConfig.PAYER_ID_KEY) } returns mockPayerId

        viewModel.uiState.test {
            // ACTION
            viewModel.parsePayPalUrl(mockSuccessUrl)
            // Initial state
            assertIs<PayPalCheckoutUIState.Idle>(awaitItem())
            // Result state - success
            awaitItem().let { state ->
                assertIs<PayPalCheckoutUIState.Capture>(state)
                assertEquals(mockToken, state.payPalToken)
                assertEquals(mockPayerId, state.payerId)
            }
        }
    }

    @Test
    fun `capture PayPal wallet charge should update isLoading, call useCase, and update state on success`() =
        runTest {
            val paymentMethodId = MobileSDKTestConstants.PayPal.MOCK_PAYMENT_METHOD_ID
            val payerId = MobileSDKTestConstants.PayPal.MOCK_PAYER_ID
            val response =
                readResourceFile("wallet/success_capture_wallet_response.json").convertToDataClass<CaptureChargeResponse>()
            val mockResult = Result.success(response.asEntity())
            coEvery { captureWalletChargeUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.uiState.test {
                // ACTION
                viewModel.captureWalletTransaction(paymentMethodId, payerId)
                // CHECK
                // Initial state
                assertIs<PayPalCheckoutUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<PayPalCheckoutUIState.Loading>(awaitItem())
                coVerify { captureWalletChargeUseCase(any(), any()) }
                // Result state - success
                awaitItem().let { state ->
                    assertIs<PayPalCheckoutUIState.Success>(state)
                    assertEquals(mockResult.getOrNull(), state.chargeData)
                }
            }
        }

    @Test
    fun `capture PayPal wallet charge should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val invalidAccessToken = MobileSDKTestConstants.Wallet.MOCK_INVALID_WALLET_TOKEN
            viewModel.setWalletToken(invalidAccessToken)
            val paymentMethodId = MobileSDKTestConstants.PayPal.MOCK_PAYMENT_METHOD_ID
            val payerId = MobileSDKTestConstants.PayPal.MOCK_PAYER_ID
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
            coEvery { captureWalletChargeUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.uiState.test {
                // ACTION
                viewModel.captureWalletTransaction(paymentMethodId, payerId)
                // CHECK
                // 4.
                // Initial state
                assertIs<PayPalCheckoutUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<PayPalCheckoutUIState.Loading>(awaitItem())
                coVerify { captureWalletChargeUseCase(any(), any()) }
                // Resul state - failure
                awaitItem().let { state ->
                    assertIs<PayPalCheckoutUIState.Error>(state)
                    assertIs<PayPalException.CapturingChargeException>(state.exception)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_GENERAL_ERROR,
                        state.exception.message
                    )
                }
            }
        }

    @Test
    fun `get PayPal wallet callback should update isLoading, call useCase, and update state to launch intent`() =
        runTest {
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val mockCallbackUrl = MobileSDKTestConstants.PayPal.MOCK_CALLBACK_URL

            val response = readResourceFile("wallet/success_paypal_wallet_callback_response.json")
                .convertToDataClass<WalletCallbackResponse>()
            val mockResult = Result.success(response.asEntity())
            coEvery { getWalletCallbackUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.uiState.test {
                // ACTION
                viewModel.getWalletCallback(walletToken = accessToken, requestShipping = true)
                // CHECK
                // Initial state
                assertIs<PayPalCheckoutUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<PayPalCheckoutUIState.Loading>(awaitItem())
                coVerify { getWalletCallbackUseCase(any(), any()) }
                // Result state - success
                awaitItem().let { state ->
                    assertIs<PayPalCheckoutUIState.LaunchIntent>(state)
                    assertNotNull(state.callbackData.callbackUrl)
                    assertEquals(mockCallbackUrl, state.callbackData.callbackUrl)
                }
            }
        }

    @Test
    fun `get PayPal wallet callback should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val mockError = ApiException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.InternalServerError.value,
                    summary = ErrorSummary(
                        code = "unexpected_error",
                        message = MobileSDKTestConstants.Errors.MOCK_WALLET_TYPE_ERROR
                    )
                )
            )
            val mockResult = Result.failure<WalletCallback>(mockError)
            coEvery { getWalletCallbackUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.uiState.test {
                // ACTION
                viewModel.setWalletToken(accessToken)
                viewModel.getWalletCallback(walletToken = accessToken, requestShipping = true)
                // CHECK
                // Initial state
                assertIs<PayPalCheckoutUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<PayPalCheckoutUIState.Loading>(awaitItem())
                coVerify { getWalletCallbackUseCase(any(), any()) }
                // Result state - failure
                awaitItem().let { state ->
                    assertIs<PayPalCheckoutUIState.Error>(state)
                    assertIs<PayPalException.FetchingUrlException>(state.exception)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_WALLET_TYPE_ERROR,
                        state.exception.message
                    )
                }
            }
        }

    @Test
    fun `resetResultState should reset UI state`() = runTest {
        val paymentMethodId = MobileSDKTestConstants.PayPal.MOCK_PAYMENT_METHOD_ID
        val payerId = MobileSDKTestConstants.PayPal.MOCK_PAYER_ID
        val response =
            readResourceFile("wallet/success_capture_wallet_response.json").convertToDataClass<CaptureChargeResponse>()
        val mockResult = Result.success(response.asEntity())
        coEvery { captureWalletChargeUseCase(any(), any()) } returns mockResult
        viewModel.captureWalletTransaction(paymentMethodId, payerId)
        // Allows for testing flow state
        viewModel.uiState.test {
            // ACTION
            viewModel.resetResultState()
            assertIs<PayPalCheckoutUIState.Idle>(awaitItem())
        }
    }
}
