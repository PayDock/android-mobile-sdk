package com.paydock.feature.paypal.checkout.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.PayPalException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.telemetry.RecordingTelemetry
import com.paydock.core.telemetry.SdkWidget
import com.paydock.core.telemetry.WidgetSourceScan
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.paypal.checkout.domain.model.integration.PayPalWidgetConfig
import com.paydock.feature.paypal.checkout.presentation.state.PayPalCheckoutUIState
import com.paydock.feature.paypal.checkout.presentation.viewmodel.PayPalViewModel
import com.paydock.feature.paypal.core.domain.usecase.GetPayPalClientIdUseCase
import com.paydock.feature.wallet.data.dto.CaptureChargeResponse
import com.paydock.feature.wallet.data.dto.WalletCallbackResponse
import com.paydock.feature.wallet.data.mapper.asEntity
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import com.paydock.feature.wallet.domain.model.ui.WalletCallback
import com.paydock.feature.wallet.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertSame
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
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
internal class PayPalViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: PayPalViewModel
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var captureWalletChargeUseCase: CaptureWalletChargeUseCase
    private lateinit var declineWalletChargeUseCase: DeclineWalletChargeUseCase
    private lateinit var getWalletCallbackUseCase: GetWalletCallbackUseCase
    private lateinit var getPayPalClientIdUseCase: GetPayPalClientIdUseCase
    private lateinit var widgetConfig: PayPalWidgetConfig
    private lateinit var telemetry: RecordingTelemetry

    @Before
    fun setup() {
        savedStateHandle = SavedStateHandle()
        captureWalletChargeUseCase = mockk()
        declineWalletChargeUseCase = mockk()
        getWalletCallbackUseCase = mockk()
        getPayPalClientIdUseCase = mockk()
        telemetry = RecordingTelemetry()
        widgetConfig = PayPalWidgetConfig(
            accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN,
            gatewayId = "GATEWAY_ID_TEST",
            fundingSource = PayPalWidgetConfig.PayPalFundingSource.PAYPAL
        )
        viewModel = PayPalViewModel(
            widgetConfig,
            savedStateHandle,
            captureWalletChargeUseCase,
            declineWalletChargeUseCase,
            getWalletCallbackUseCase,
            dispatchersProvider,
            getPayPalClientIdUseCase,
            telemetry
        )
        val mockToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        viewModel.setWalletToken(mockToken)
    }

    @Test
    fun `handlePayPalButtonClick success triggers wallet callback and emits LaunchIntent`() = runTest {
        // GIVEN token success, wallet callback success with ORDER_ID, and clientId success
        val tokenResult = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        val walletCallbackResponse = readResourceFile("wallet/success_paypal_wallet_callback_response.json")
            .convertToDataClass<WalletCallbackResponse>()
        val walletCallbackSuccess = Result.success(walletCallbackResponse.asEntity())
        coEvery { getWalletCallbackUseCase(any(), any()) } returns walletCallbackSuccess
        coEvery {
            getPayPalClientIdUseCase(widgetConfig.accessToken, widgetConfig.gatewayId)
        } returns Result.success(MobileSDKTestConstants.PayPalVault.MOCK_CLIENT_ID)

        viewModel.uiState.test {
            // ACTION: simulate button click providing token success via callback
            viewModel.handlePayPalButtonClick(
                config = widgetConfig,
                tokenRequest = { callback -> callback(Result.success(WalletTokenResult(tokenResult))) }
            )
            // Initial Idle
            assertIs<PayPalCheckoutUIState.Idle>(awaitItem())
            // Loading from handlePayPalButtonClick
            assertIs<PayPalCheckoutUIState.Loading>(awaitItem())
            // Result LaunchIntent
            awaitItem().let { state ->
                assertIs<PayPalCheckoutUIState.LaunchIntent>(state)
                assertEquals(MobileSDKTestConstants.PayPalVault.MOCK_CLIENT_ID, state.clientId)
                assertNotNull(state.orderId)
            }
        }
    }

    @Test
    fun `handlePayPalButtonClick failure emits InitialisationWalletTokenException`() = runTest {
        viewModel.uiState.test {
            // ACTION: simulate tokenization failure via callback
            viewModel.handlePayPalButtonClick(
                config = widgetConfig,
                tokenRequest = { callback -> callback(Result.failure(Exception(MobileSDKTestConstants.Errors.MOCK_TOKENIZATION_ERROR))) }
            )
            // Initial Idle
            assertIs<PayPalCheckoutUIState.Idle>(awaitItem())
            // Loading
            assertIs<PayPalCheckoutUIState.Loading>(awaitItem())
            // Error
            awaitItem().let { state ->
                assertIs<PayPalCheckoutUIState.Error>(state)
                assertIs<PayPalException.InitialisationWalletTokenException>(state.exception)
                assertEquals(MobileSDKTestConstants.Errors.MOCK_TOKENIZATION_ERROR, state.exception.message)
            }
        }
    }

    // Removed obsolete parsePayPalUrl test since the flow now passes data via SDK results

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

            val response = readResourceFile("wallet/success_paypal_wallet_callback_response.json")
                .convertToDataClass<WalletCallbackResponse>()
            val mockResult = Result.success(response.asEntity())
            coEvery { getWalletCallbackUseCase(any(), any()) } returns mockResult
            coEvery {
                getPayPalClientIdUseCase(widgetConfig.accessToken, widgetConfig.gatewayId)
            } returns Result.success(MobileSDKTestConstants.PayPalVault.MOCK_CLIENT_ID)
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
                // Result state - success (LaunchIntent with clientId and orderId)
                awaitItem().let { state ->
                    assertIs<PayPalCheckoutUIState.LaunchIntent>(state)
                    assertEquals(MobileSDKTestConstants.PayPalVault.MOCK_CLIENT_ID, state.clientId)
                    assertNotNull(state.orderId)
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

    @Test
    fun `get PayPal wallet callback with missing ORDER_ID emits configuration error`() = runTest {
        val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        val walletCallback = WalletCallback(
            callbackId = "cb_123",
            status = "CREATED",
            callbackUrl = MobileSDKTestConstants.PayPal.MOCK_CALLBACK_URL,
            refToken = null,
            id = null // missing order id
        )
        val mockResult = Result.success(walletCallback)
        coEvery { getWalletCallbackUseCase(any(), any()) } returns mockResult

        viewModel.uiState.test {
            viewModel.getWalletCallback(walletToken = accessToken, requestShipping = true)
            assertIs<PayPalCheckoutUIState.Idle>(awaitItem())
            assertIs<PayPalCheckoutUIState.Loading>(awaitItem())
            awaitItem().let { state ->
                assertIs<PayPalCheckoutUIState.Error>(state)
                assertIs<PayPalException.ConfigurationException>(state.exception)
            }
        }
    }

    @Test
    fun `get PayPal client id failure maps to PayPalException_GetPayPalClientIdException`() = runTest {
        val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        val walletCallback = WalletCallback(
            callbackId = "cb_123",
            status = "CREATED",
            callbackUrl = MobileSDKTestConstants.PayPal.MOCK_CALLBACK_URL,
            refToken = null,
            id = MobileSDKTestConstants.PayPal.MOCK_ORDER_ID
        )
        val callbackSuccess = Result.success(walletCallback)
        val apiError = ApiException(
            error = ApiErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                summary = ErrorSummary(
                    code = "unexpected_error",
                    message = MobileSDKTestConstants.Errors.MOCK_CLIENT_ID_ERROR
                )
            )
        )
        coEvery { getWalletCallbackUseCase(any(), any()) } returns callbackSuccess
        coEvery { getPayPalClientIdUseCase(widgetConfig.accessToken, widgetConfig.gatewayId) } returns Result.failure(apiError)

        viewModel.uiState.test {
            viewModel.getWalletCallback(walletToken = accessToken, requestShipping = true)
            assertIs<PayPalCheckoutUIState.Idle>(awaitItem())
            assertIs<PayPalCheckoutUIState.Loading>(awaitItem())
            awaitItem().let { state ->
                assertIs<PayPalCheckoutUIState.Error>(state)
                assertIs<PayPalException.GetPayPalClientIdException>(state.exception)
            }
        }
    }

    @Test
    fun `get funding source maps to correct PayPal enum for PAYPAL`() = runTest {
        // Given
        widgetConfig = PayPalWidgetConfig(
            accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN,
            gatewayId = "GATEWAY_ID_TEST",
            fundingSource = PayPalWidgetConfig.PayPalFundingSource.PAYPAL
        )
        viewModel = PayPalViewModel(
            widgetConfig,
            savedStateHandle,
            captureWalletChargeUseCase,
            declineWalletChargeUseCase,
            getWalletCallbackUseCase,
            dispatchersProvider,
            getPayPalClientIdUseCase,
            telemetry
        )

        // When
        val result = viewModel.getFundingSource()

        // Then
        assertEquals(result, PayPalWebCheckoutFundingSource.PAYPAL)
    }

    @Test
    fun `get funding source maps to correct PayPal enum for PAY_LATER`() = runTest {
        // Given
        widgetConfig = PayPalWidgetConfig(
            accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN,
            gatewayId = "GATEWAY_ID_TEST",
            fundingSource = PayPalWidgetConfig.PayPalFundingSource.PAY_LATER
        )
        viewModel = PayPalViewModel(
            widgetConfig,
            savedStateHandle,
            captureWalletChargeUseCase,
            declineWalletChargeUseCase,
            getWalletCallbackUseCase,
            dispatchersProvider,
            getPayPalClientIdUseCase,
            telemetry
        )

        // When
        val result = viewModel.getFundingSource()

        // Then
        assertEquals(result, PayPalWebCheckoutFundingSource.PAY_LATER)
    }

    @Test
    fun `get funding source maps to correct PayPal enum for PAYPAL_CREDIT`() = runTest {
        // Given
        widgetConfig = PayPalWidgetConfig(
            accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN,
            gatewayId = "GATEWAY_ID_TEST",
            fundingSource = PayPalWidgetConfig.PayPalFundingSource.PAYPAL_CREDIT
        )
        viewModel = PayPalViewModel(
            widgetConfig,
            savedStateHandle,
            captureWalletChargeUseCase,
            declineWalletChargeUseCase,
            getWalletCallbackUseCase,
            dispatchersProvider,
            getPayPalClientIdUseCase,
            telemetry
        )

        // When
        val result = viewModel.getFundingSource()

        // Then
        assertEquals(result, PayPalWebCheckoutFundingSource.PAYPAL_CREDIT)
    }

    // region Telemetry: one SdkError per failure path (PP1-PP8), merchant-visible results unchanged

    private fun walletCallbackWithOrderId(orderId: String?) = WalletCallback(
        callbackId = "cb_123",
        status = "CREATED",
        callbackUrl = MobileSDKTestConstants.PayPal.MOCK_CALLBACK_URL,
        refToken = null,
        id = orderId
    )

    private fun apiFailure(status: HttpStatusCode, code: String) = ApiException(
        error = ApiErrorResponse(
            status = status.value,
            summary = ErrorSummary(code = code, message = MobileSDKTestConstants.Errors.MOCK_GENERAL_ERROR)
        )
    )

    @Test
    fun `missing order id reports one SdkError and still sets ConfigurationException (PP1)`() = runTest {
        coEvery { getWalletCallbackUseCase(any(), any()) } returns Result.success(walletCallbackWithOrderId(null))

        viewModel.getWalletCallback(walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN, requestShipping = true)
        val state = viewModel.uiState.first { it is PayPalCheckoutUIState.Error }

        assertIs<PayPalCheckoutUIState.Error>(state)
        assertIs<PayPalException.ConfigurationException>(state.exception)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.PAYPAL, reported.widget)
        assertEquals("ConfigurationException", reported.errorCode)
        assertNull(reported.error.cause)
    }

    @Test
    fun `wallet callback failure reports one SdkError with the API cause and still sets FetchingUrlException (PP2)`() =
        runTest {
            coEvery { getWalletCallbackUseCase(any(), any()) } returns
                Result.failure(apiFailure(HttpStatusCode.BadRequest, "unexpected_error"))

            viewModel.getWalletCallback(walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN, requestShipping = true)
            val state = viewModel.uiState.first { it is PayPalCheckoutUIState.Error }

            assertIs<PayPalCheckoutUIState.Error>(state)
            assertIs<PayPalException.FetchingUrlException>(state.exception)
            val reported = telemetry.sdkErrors.single()
            assertEquals(SdkWidget.PAYPAL, reported.widget)
            assertEquals("FetchingUrlException", reported.errorCode)
            assertEquals(ApiException::class.java.name, reported.error.cause?.type)
            assertEquals(HttpStatusCode.BadRequest.value, reported.apiError?.status)
            assertEquals("unexpected_error", reported.apiError?.summaryCode)
        }

    @Test
    fun `wallet callback failure in a cancelled scope reports nothing and still sets FetchingUrlException (PP2)`() =
        runTest {
            coEvery { getWalletCallbackUseCase(any(), any()) } coAnswers {
                viewModel.viewModelScope.cancel()
                Result.failure(apiFailure(HttpStatusCode.BadRequest, "unexpected_error"))
            }

            viewModel.getWalletCallback(walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN, requestShipping = true)
            val state = viewModel.uiState.first { it is PayPalCheckoutUIState.Error }

            assertIs<PayPalCheckoutUIState.Error>(state)
            assertIs<PayPalException.FetchingUrlException>(state.exception)
            assertTrue(telemetry.sdkErrors.isEmpty())
        }

    @Test
    fun `capture failure reports one SdkError with the API cause and still sets CapturingChargeException (PP3)`() =
        runTest {
            coEvery { captureWalletChargeUseCase(any(), any()) } returns
                Result.failure(apiFailure(HttpStatusCode.InternalServerError, "capture_error"))

            viewModel.captureWalletTransaction(
                MobileSDKTestConstants.PayPal.MOCK_PAYMENT_METHOD_ID,
                MobileSDKTestConstants.PayPal.MOCK_PAYER_ID
            )
            val state = viewModel.uiState.first { it is PayPalCheckoutUIState.Error }

            assertIs<PayPalCheckoutUIState.Error>(state)
            assertIs<PayPalException.CapturingChargeException>(state.exception)
            val reported = telemetry.sdkErrors.single()
            assertEquals(SdkWidget.PAYPAL, reported.widget)
            assertEquals("CapturingChargeException", reported.errorCode)
            assertEquals(ApiException::class.java.name, reported.error.cause?.type)
            assertEquals(HttpStatusCode.InternalServerError.value, reported.apiError?.status)
            assertEquals("capture_error", reported.apiError?.summaryCode)
        }

    @Test
    fun `client id API failure reports the mapped exception with the raw cause and still sets it (PP4)`() = runTest {
        coEvery { getWalletCallbackUseCase(any(), any()) } returns
            Result.success(walletCallbackWithOrderId(MobileSDKTestConstants.PayPal.MOCK_ORDER_ID))
        coEvery { getPayPalClientIdUseCase(widgetConfig.accessToken, widgetConfig.gatewayId) } returns
            Result.failure(apiFailure(HttpStatusCode.InternalServerError, "gateway_client_id"))

        viewModel.getWalletCallback(walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN, requestShipping = true)
        val state = viewModel.uiState.first { it is PayPalCheckoutUIState.Error }

        assertIs<PayPalCheckoutUIState.Error>(state)
        assertIs<PayPalException.GetPayPalClientIdException>(state.exception)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.PAYPAL, reported.widget)
        assertEquals("GetPayPalClientIdException", reported.errorCode)
        assertEquals(ApiException::class.java.name, reported.error.cause?.type)
        assertEquals("gateway_client_id", reported.apiError?.summaryCode)
    }

    @Test
    fun `client id SdkException failure reports the same exception with no cause and still sets it (PP4)`() = runTest {
        val sdkFailure = PayPalException.GetPayPalClientIdException(
            error = ApiErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                summary = ErrorSummary(
                    code = "gateway_client_id",
                    message = MobileSDKTestConstants.Errors.MOCK_CLIENT_ID_ERROR
                )
            )
        )
        coEvery { getWalletCallbackUseCase(any(), any()) } returns
            Result.success(walletCallbackWithOrderId(MobileSDKTestConstants.PayPal.MOCK_ORDER_ID))
        coEvery { getPayPalClientIdUseCase(widgetConfig.accessToken, widgetConfig.gatewayId) } returns
            Result.failure(sdkFailure)

        viewModel.getWalletCallback(walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN, requestShipping = true)
        val state = viewModel.uiState.first { it is PayPalCheckoutUIState.Error }

        assertIs<PayPalCheckoutUIState.Error>(state)
        assertSame(sdkFailure, state.exception)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.PAYPAL, reported.widget)
        assertEquals("GetPayPalClientIdException", reported.errorCode)
        assertNull(reported.error.cause)
        assertEquals("gateway_client_id", reported.apiError?.summaryCode)
    }

    @Test
    fun `lost wallet token reports one SdkError and still sets InitialisationWalletTokenException (PP5)`() = runTest {
        viewModel.resetResultState()

        viewModel.captureWalletTransaction(
            MobileSDKTestConstants.PayPal.MOCK_PAYMENT_METHOD_ID,
            MobileSDKTestConstants.PayPal.MOCK_PAYER_ID
        )

        val state = viewModel.uiState.value
        assertIs<PayPalCheckoutUIState.Error>(state)
        assertIs<PayPalException.InitialisationWalletTokenException>(state.exception)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.PAYPAL, reported.widget)
        assertEquals("InitialisationWalletTokenException", reported.errorCode)
        assertNull(reported.error.cause)
    }

    @Test
    fun `merchant token failure reports no SdkError and still sets the error (PP6)`() =
        runTest {
            viewModel.handlePayPalButtonClick(
                config = widgetConfig,
                tokenRequest = { callback -> callback(Result.failure(IllegalStateException())) }
            )

            val state = viewModel.uiState.value
            assertIs<PayPalCheckoutUIState.Error>(state)
            assertIs<PayPalException.InitialisationWalletTokenException>(state.exception)
            assertTrue(telemetry.sdkErrors.isEmpty())
        }

    @Test
    fun `reportWidgetError reports one PayPal SdkError and changes no state (PP7)`() = runTest {
        val before = viewModel.uiState.value

        viewModel.reportWidgetError(PayPalException.PayPalSDKException(code = -1, description = "synthetic"))

        assertSame(before, viewModel.uiState.value)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.PAYPAL, reported.widget)
        assertEquals("PayPalSDKException", reported.errorCode)
        assertNull(reported.error.cause)
    }

    @Test
    fun `the web result handler reports the web checkout error it hands on once, and never the cancellation (PP7, PP8)`() {
        val source = WidgetSourceScan.read("feature/paypal/checkout/presentation/PayPalWidget.kt")
        val handler = WidgetSourceScan.function(source, "handlePayPalResult")
        val cancellation = WidgetSourceScan.between(handler, "CancellationStatus.USER_INITIATED", "else ->")
        val webCheckout = WidgetSourceScan.between(handler, "else ->", "viewModel.resetResultState()")

        assertEquals(1, WidgetSourceScan.count(source, "viewModel.reportWidgetError("))
        assertEquals(1, WidgetSourceScan.count(cancellation, "PayPalException.CancellationException("))
        assertEquals(0, WidgetSourceScan.count(cancellation, "reportWidgetError("))
        assertEquals(1, WidgetSourceScan.count(cancellation, "completion("))
        assertEquals(1, WidgetSourceScan.count(webCheckout, "PayPalException.PayPalSDKException("))
        assertEquals(1, WidgetSourceScan.count(webCheckout, "viewModel.reportWidgetError(failure)"))
        assertEquals(1, WidgetSourceScan.count(webCheckout, "completion(Result.failure(failure))"))
    }

    // endregion
}
