package com.paydock.feature.paypal.presentation.viewmodels

import android.net.Uri
import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKConstants
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.network.error.ApiErrorResponse
import com.paydock.core.data.network.error.ErrorSummary
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.ApiException
import com.paydock.core.domain.error.exceptions.PayPalException
import com.paydock.core.extensions.convertToDataClass
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.wallet.data.api.dto.WalletCallbackResponse
import com.paydock.feature.wallet.data.api.dto.WalletCaptureResponse
import com.paydock.feature.wallet.data.mapper.asEntity
import com.paydock.feature.wallet.domain.model.WalletCallback
import com.paydock.feature.wallet.domain.usecase.CaptureWalletTransactionUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletTransactionUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
import kotlin.test.assertIs

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class PayPalViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: PayPalViewModel
    private lateinit var captureWalletTransactionUseCase: CaptureWalletTransactionUseCase
    private lateinit var declineWalletTransactionUseCase: DeclineWalletTransactionUseCase
    private lateinit var getWalletCallbackUseCase: GetWalletCallbackUseCase

    @Before
    fun setup() {
        captureWalletTransactionUseCase = mockk()
        declineWalletTransactionUseCase = mockk()
        getWalletCallbackUseCase = mockk()
        viewModel = PayPalViewModel(
            captureWalletTransactionUseCase,
            declineWalletTransactionUseCase,
            getWalletCallbackUseCase,
            dispatchersProvider
        )
    }

    @Test
    fun `setWalletToken should update token`() = runTest {
        val walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        // ACTION
        viewModel.setWalletToken(walletToken)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertEquals(walletToken, state.token)
    }

    //

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
                assertNull(state.callbackData)
                assertNull(state.paymentData)
                assertNull(state.error)
            }
        }
    }

    @Test
    fun `createPayPalUrl should return PayPal callback url with redirect url`() = runTest {
        val callbackUrl = MobileSDKTestConstants.PayPal.MOCK_CALLBACK_URL
        val resultUrl = viewModel.createPayPalUrl(callbackUrl)
        assertEquals(
            "$callbackUrl&${MobileSDKConstants.PayPalConfig.REDIRECT_PARAM_NAME}" +
                "=${MobileSDKConstants.PayPalConfig.PAY_PAL_REDIRECT_PARAM_VALUE}",
            resultUrl
        )
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
        every { mockUri.getQueryParameter("PayerID") } returns mockPayerId

        viewModel.stateFlow.test {
            // ACTION
            viewModel.parsePayPalUrl(mockSuccessUrl)
            // Initial state
            assertNull(awaitItem().paymentData)
            // Result state - success
            awaitItem().let { state ->
                assertNotNull(state.paymentData)
                assertEquals(mockToken, state.paymentData?.payPalToken)
                assertEquals(mockPayerId, state.paymentData?.payerId)
                assertFalse(state.isLoading)
            }
        }
    }

    @Test
    fun `capture PayPal wallet charge should update isLoading, call useCase, and update state on success`() =
        runTest {
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val paymentMethodId = MobileSDKTestConstants.PayPal.MOCK_PAYMENT_METHOD_ID
            val payerId = MobileSDKTestConstants.PayPal.MOCK_PAYER_ID
            val response =
                readResourceFile("wallet/success_capture_wallet_response.json").convertToDataClass<WalletCaptureResponse>()
            val mockResult = Result.success(response.asEntity())
            coEvery { captureWalletTransactionUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.captureWalletTransaction(accessToken, paymentMethodId, payerId)
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
    fun `capture PayPal wallet charge should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val invalidAccessToken = MobileSDKTestConstants.Wallet.MOCK_INVALID_WALLET_TOKEN
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
            coEvery { captureWalletTransactionUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.captureWalletTransaction(invalidAccessToken, paymentMethodId, payerId)
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
                    assertIs<PayPalException.CapturingChargeException>(state.error)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_GENERAL_ERROR,
                        state.error.message
                    )
                }
            }
        }

    @Test
    fun `get PayPal wallet callback should update isLoading, call useCase, and update state on success`() =
        runTest {
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val mockCallbackUrl = MobileSDKTestConstants.PayPal.MOCK_CALLBACK_URL

            val response = readResourceFile("wallet/success_paypal_wallet_callback_response.json")
                .convertToDataClass<WalletCallbackResponse>()
            val mockResult = Result.success(response.asEntity())
            coEvery { getWalletCallbackUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.setWalletToken(accessToken)
                viewModel.getWalletCallback(walletToken = accessToken, requestShipping = true)
                // CHECK
                // 4.
                // Initial state
                assertFalse(awaitItem().isLoading)
                // wallet token is added to state
                assertNotNull(awaitItem().token)
                // Loading state - before execution
                assertTrue(awaitItem().isLoading)
                coVerify { getWalletCallbackUseCase(any(), any()) }
                // Resul state - success
                awaitItem().let { state ->
                    assertFalse(state.isLoading)
                    assertNotNull(state.callbackData)
                    assertNotNull(state.callbackData?.callbackUrl)
                    assertEquals(mockCallbackUrl, state.callbackData?.callbackUrl)
                    assertNull(state.error)
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
            viewModel.stateFlow.test {
                // ACTION
                viewModel.setWalletToken(accessToken)
                viewModel.getWalletCallback(walletToken = accessToken, requestShipping = true)
                // CHECK
                // 4.
                // Initial state
                assertFalse(awaitItem().isLoading)
                // wallet token is added to state
                assertNotNull(awaitItem().token)
                // Loading state - before execution
                assertTrue(awaitItem().isLoading)
                coVerify { getWalletCallbackUseCase(any(), any()) }
                // Resul state - failure
                awaitItem().let { state ->
                    assertFalse(state.isLoading)
                    assertNull(state.callbackData)
                    assertNotNull(state.error)
                    assertIs<PayPalException.FetchingUrlException>(state.error)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_WALLET_TYPE_ERROR,
                        state.error.message
                    )
                }
            }
        }

}
