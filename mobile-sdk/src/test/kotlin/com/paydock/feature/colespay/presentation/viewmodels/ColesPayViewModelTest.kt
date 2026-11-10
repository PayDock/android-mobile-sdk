package com.paydock.feature.colespay.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.ColesPayException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.telemetry.RecordingTelemetry
import com.paydock.core.telemetry.SdkWidget
import com.paydock.core.telemetry.WidgetSourceScan
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.colespay.integration.ColesPayWidgetConfig
import com.paydock.feature.colespay.presentation.state.ColesPayUIState
import com.paydock.feature.wallet.data.dto.WalletCallbackResponse
import com.paydock.feature.wallet.data.mapper.asEntity
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import com.paydock.feature.wallet.domain.model.ui.WalletCallback
import com.paydock.feature.wallet.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
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
internal class ColesPayViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()
    private lateinit var viewModel: ColesPayViewModel
    private lateinit var captureWalletChargeUseCase: CaptureWalletChargeUseCase
    private lateinit var declineWalletChargeUseCase: DeclineWalletChargeUseCase
    private lateinit var getWalletCallbackUseCase: GetWalletCallbackUseCase
    private lateinit var telemetry: RecordingTelemetry

    @Before
    fun setup() {
        captureWalletChargeUseCase = mockk()
        declineWalletChargeUseCase = mockk()
        getWalletCallbackUseCase = mockk()
        telemetry = RecordingTelemetry()

        viewModel = ColesPayViewModel(
            ColesPayWidgetConfig(MobileSDKTestConstants.ColesPay.MOCK_CLIENT_ID),
            SavedStateHandle(),
            captureWalletChargeUseCase,
            declineWalletChargeUseCase,
            getWalletCallbackUseCase,
            dispatchersProvider,
            telemetry
        )
        val mockToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        viewModel.setWalletToken(mockToken)
    }

    @Test
    fun `get Coles Pay wallet callback should update isLoading, call useCase, and update state to launch intent`() =
        runTest {
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val mockColesPayOrderId = MobileSDKTestConstants.ColesPay.MOCK_ORDER_ID
            val response =
                readResourceFile("wallet/success_colespay_wallet_callback_response.json")
                    .convertToDataClass<WalletCallbackResponse>()
            val mockResult = Result.success(response.asEntity())
            coEvery { getWalletCallbackUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.uiState.test {
                // ACTION
                viewModel.getWalletCallback(walletToken = accessToken)
                // CHECK
                // Initial state
                assertIs<ColesPayUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<ColesPayUIState.Loading>(awaitItem())
                coVerify { getWalletCallbackUseCase(any(), any()) }
                // Result state - success
                awaitItem().let { state ->
                    assertIs<ColesPayUIState.LaunchIntent>(state)
                    assertNotNull(state.callbackData.callbackId)
                    assertEquals(mockColesPayOrderId, state.callbackData.callbackId)
                }
            }
        }

    @Test
    fun `get Coles Pay wallet callback should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val mockError = ApiException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    summary = ErrorSummary(
                        code = "unexpected_error",
                        message = MobileSDKTestConstants.Errors.MOCK_INVALID_GATEWAY_ID_ERROR
                    )
                )
            )
            val mockResult = Result.failure<WalletCallback>(mockError)
            coEvery { getWalletCallbackUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.uiState.test {
                // ACTION
                viewModel.getWalletCallback(walletToken = accessToken)
                // CHECK
                // Initial state
                assertIs<ColesPayUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<ColesPayUIState.Loading>(awaitItem())
                coVerify { getWalletCallbackUseCase(any(), any()) }
                // Result state - failure
                awaitItem().let { state ->
                    assertIs<ColesPayUIState.Error>(state)
                    assertIs<ColesPayException.FetchingUrlException>(state.exception)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_INVALID_GATEWAY_ID_ERROR,
                        state.exception.message
                    )
                }
            }
        }

    @Test
    fun `completeResult should update UI state to success`() = runTest {
        val mockOrderId = MobileSDKTestConstants.ColesPay.MOCK_ORDER_ID
        viewModel.uiState.test {
            // Initial state
            assertIs<ColesPayUIState.Idle>(awaitItem())
            // ACTION
            viewModel.completeResult(mockOrderId)
            // Result state - success
            awaitItem().let { state ->
                assertIs<ColesPayUIState.Success>(state)
                assertEquals(mockOrderId, state.orderId)
            }
        }
    }

    @Test
    fun `resetResultState should reset UI state`() = runTest {
        viewModel.completeResult(MobileSDKTestConstants.ColesPay.MOCK_ORDER_ID)
        viewModel.uiState.test {
            assertIs<ColesPayUIState.Success>(awaitItem())
            viewModel.resetResultState()
            // Result state - reset
            assertIs<ColesPayUIState.Idle>(awaitItem())
        }
    }

    @Test
    fun `startColesPayFlow success triggers wallet callback and emits LaunchIntent`() = runTest {
        // GIVEN token success, wallet callback success
        val tokenResult = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        val response =
            readResourceFile("wallet/success_colespay_wallet_callback_response.json")
                .convertToDataClass<WalletCallbackResponse>()
        val mockResult = Result.success(response.asEntity())
        coEvery { getWalletCallbackUseCase(any(), any()) } returns mockResult

        viewModel.uiState.test {
            // ACTION: simulate flow start providing token success via callback
            viewModel.startColesPayFlow { callback ->
                callback(Result.success(WalletTokenResult(tokenResult)))
            }
            // Initial Idle
            assertIs<ColesPayUIState.Idle>(awaitItem())
            // Loading from startColesPayFlow
            assertIs<ColesPayUIState.Loading>(awaitItem())
            // Result LaunchIntent
            awaitItem().let { state ->
                assertIs<ColesPayUIState.LaunchIntent>(state)
                assertEquals(MobileSDKTestConstants.ColesPay.MOCK_ORDER_ID, state.callbackData.callbackId)
            }
        }
    }

    @Test
    fun `startColesPayFlow failure emits InitialisationWalletTokenException`() = runTest {
        viewModel.uiState.test {
            // ACTION: simulate tokenization failure via callback
            viewModel.startColesPayFlow { callback ->
                callback(Result.failure(Exception(MobileSDKTestConstants.Errors.MOCK_TOKENIZATION_ERROR)))
            }
            // Initial Idle
            assertIs<ColesPayUIState.Idle>(awaitItem())
            // Loading
            assertIs<ColesPayUIState.Loading>(awaitItem())
            // Error
            awaitItem().let { state ->
                assertIs<ColesPayUIState.Error>(state)
                assertIs<ColesPayException.InitialisationWalletTokenException>(state.exception)
                assertEquals(MobileSDKTestConstants.Errors.MOCK_TOKENIZATION_ERROR, state.exception.message)
            }
        }
    }

    // region Telemetry: one SdkError per failure path (CP1-CP4), merchant-visible results unchanged

    @Test
    fun `callback failure reports one SdkError with the API cause and still sets FetchingUrlException (CP1)`() =
        runTest {
            val apiFailure = ApiException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    summary = ErrorSummary(
                        code = "unexpected_error",
                        message = MobileSDKTestConstants.Errors.MOCK_INVALID_GATEWAY_ID_ERROR
                    )
                )
            )
            coEvery { getWalletCallbackUseCase(any(), any()) } returns Result.failure(apiFailure)

            viewModel.getWalletCallback(walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN)
            val state = viewModel.uiState.first { it is ColesPayUIState.Error }

            assertIs<ColesPayUIState.Error>(state)
            assertIs<ColesPayException.FetchingUrlException>(state.exception)
            val reported = telemetry.sdkErrors.single()
            assertEquals(SdkWidget.COLES_PAY, reported.widget)
            assertEquals("FetchingUrlException", reported.errorCode)
            assertEquals(ApiException::class.java.name, reported.error.cause?.type)
            assertEquals(HttpStatusCode.BadRequest.value, reported.apiError?.status)
            assertEquals("unexpected_error", reported.apiError?.summaryCode)
        }

    @Test
    fun `merchant token failure reports no SdkError and still sets the error (CP2)`() =
        runTest {
            viewModel.startColesPayFlow { callback -> callback(Result.failure(IllegalStateException())) }

            val state = viewModel.uiState.value
            assertIs<ColesPayUIState.Error>(state)
            assertIs<ColesPayException.InitialisationWalletTokenException>(state.exception)
            assertTrue(telemetry.sdkErrors.isEmpty())
        }

    @Test
    fun `reportWidgetError reports one Coles Pay SdkError and changes no state (CP3, CP4)`() = runTest {
        val before = viewModel.uiState.value

        viewModel.reportWidgetError(ColesPayException.CancellationException(displayableMessage = "cancelled"))

        assertSame(before, viewModel.uiState.value)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.COLES_PAY, reported.widget)
        assertEquals("CancellationException", reported.errorCode)
        assertNull(reported.error.cause)
    }

    @Test
    fun `the web result handler reports the page-closed cancellation and the web view error it hands on, once each (CP3, CP4)`() {
        val source = WidgetSourceScan.read("feature/colespay/presentation/ColesPayWidget.kt")
        val handler = WidgetSourceScan.function(source, "handleColesPayResult")
        val cancellation = WidgetSourceScan.between(handler, "CancellationStatus.USER_INITIATED", "else ->")
        val webView = WidgetSourceScan.between(handler, "else ->", "viewModel.resetResultState()")

        assertEquals(2, WidgetSourceScan.count(source, "viewModel.reportWidgetError("))
        assertEquals(1, WidgetSourceScan.count(cancellation, "CancellationStatus.PAGE_CLOSED ->"))
        assertEquals(1, WidgetSourceScan.count(cancellation, "ColesPayException.CancellationException("))
        assertEquals(1, WidgetSourceScan.count(cancellation, "reportCancellation(cancellationStatus, failure, viewModel)"))
        assertEquals(1, WidgetSourceScan.count(cancellation, "completion(Result.failure(failure))"))
        assertEquals(1, WidgetSourceScan.count(webView, "ColesPayException.WebViewException("))
        assertEquals(1, WidgetSourceScan.count(webView, "viewModel.reportWidgetError(failure)"))
        assertEquals(1, WidgetSourceScan.count(webView, "completion(Result.failure(failure))"))
    }

    @Test
    fun `the web result handler never reports a user-initiated cancellation (CP3)`() {
        val source = WidgetSourceScan.read("feature/colespay/presentation/ColesPayWidget.kt")
        val handler = WidgetSourceScan.function(source, "handleColesPayResult")
        val cancellation = WidgetSourceScan.between(handler, "CancellationStatus.USER_INITIATED", "else ->")
        val reporter = WidgetSourceScan.function(source, "reportCancellation")

        // The cancellation branch decides on the status the activity returned, never on message text.
        assertEquals(1, WidgetSourceScan.count(handler, "when (val cancellationStatus = data.getCancellationStatusExtra())"))
        assertEquals(1, WidgetSourceScan.count(cancellation, "reportCancellation(cancellationStatus, failure, viewModel)"))
        assertEquals(
            1,
            WidgetSourceScan.count(
                reporter,
                "if (status != CancellationStatus.USER_INITIATED) viewModel.reportWidgetError(failure)"
            )
        )
    }

    // endregion
}
