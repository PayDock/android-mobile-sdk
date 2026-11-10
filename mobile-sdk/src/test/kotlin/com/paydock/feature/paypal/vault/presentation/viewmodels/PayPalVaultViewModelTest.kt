package com.paydock.feature.paypal.vault.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.telemetry.RecordingTelemetry
import com.paydock.core.telemetry.SdkWidget
import com.paydock.core.telemetry.WidgetSourceScan
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.paypal.core.domain.model.ui.PayPalPaymentTokenDetails
import com.paydock.feature.paypal.core.domain.usecase.CreatePayPalVaultPaymentTokenUseCase
import com.paydock.feature.paypal.core.domain.usecase.CreateSetupTokenUseCase
import com.paydock.feature.paypal.core.domain.usecase.GetPayPalClientIdUseCase
import com.paydock.feature.paypal.vault.domain.model.integration.PayPalVaultConfig
import com.paydock.feature.paypal.vault.presentation.state.PayPalVaultUIState
import com.paydock.feature.paypal.vault.presentation.viewmodel.PayPalVaultViewModel
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertSame
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
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
internal class PayPalVaultViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()
    private lateinit var createSetupTokenUseCase: CreateSetupTokenUseCase
    private lateinit var getPayPalClientIdUseCase: GetPayPalClientIdUseCase
    private lateinit var createPayPalVaultPaymentTokenUseCase: CreatePayPalVaultPaymentTokenUseCase

    private lateinit var viewModel: PayPalVaultViewModel
    private lateinit var config: PayPalVaultConfig
    private lateinit var telemetry: RecordingTelemetry

    companion object {
        const val MOCK_ACCESS_TOKEN = MobileSDKTestConstants.PayPalVault.MOCK_ACCESS_TOKEN
        const val MOCK_SETUP_TOKEN = MobileSDKTestConstants.PayPalVault.MOCK_SETUP_TOKEN
        const val MOCK_CLIENT_ID = MobileSDKTestConstants.PayPalVault.MOCK_CLIENT_ID
    }

    @Before
    fun setup() {
        config = PayPalVaultConfig(
            accessToken = MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
            gatewayId = MobileSDKTestConstants.General.MOCK_GATEWAY_ID
        )
        createSetupTokenUseCase = mockk()
        getPayPalClientIdUseCase = mockk()
        createPayPalVaultPaymentTokenUseCase = mockk()
        telemetry = RecordingTelemetry()
        viewModel = PayPalVaultViewModel(
            config,
            SavedStateHandle(),
            createSetupTokenUseCase,
            getPayPalClientIdUseCase,
            createPayPalVaultPaymentTokenUseCase,
            dispatchersProvider,
            telemetry
        )
    }

    private fun preparePayPalCompleteSuccessFlow() {
        prepareCreateSetupTokenSuccess()
        prepareGetClientIdSuccess()
    }

    private fun prepareCreateSetupTokenFailure() {
        val mockError = PayPalVaultException.CreateSetupTokenException(
            error = ApiErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                summary = ErrorSummary(
                    code = "setup_token",
                    message = MobileSDKTestConstants.Errors.MOCK_SETUP_TOKEN_ERROR
                )
            )
        )
        val mockResult = Result.failure<String>(mockError)
        coEvery {
            createSetupTokenUseCase(
                any(),
                any()
            )
        } returns mockResult
    }

    private fun prepareCreateSetupTokenSuccess() {
        // Prepare Setup Token
        val mockSetupTokenResult = Result.success(MOCK_SETUP_TOKEN)
        coEvery {
            createSetupTokenUseCase(
                any(),
                any()
            )
        } returns mockSetupTokenResult
    }

    private fun prepareGetClientIdSuccess() {
        // Prepare Client ID
        val mockClientIdResult = Result.success(MOCK_CLIENT_ID)
        coEvery {
            getPayPalClientIdUseCase(
                eq(config.accessToken),
                eq(config.gatewayId)
            )
        } returns mockClientIdResult
    }

    private fun prepareGetClientIdFailure() {
        val mockError = PayPalVaultException.GetPayPalClientIdException(
            error = ApiErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                summary = ErrorSummary(
                    code = "gateway_client_id",
                    message = MobileSDKTestConstants.Errors.MOCK_CLIENT_ID_ERROR
                )
            )
        )
        val mockResult = Result.failure<String>(mockError)
        coEvery {
            getPayPalClientIdUseCase(
                any(),
                any()
            )
        } returns mockResult
    }

    @Test
    fun `paypal vault token and clientId end-to-end flow should update isLoading, call 2 useCases, and update states on success`() =
        runTest {
            preparePayPalCompleteSuccessFlow()
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.createPayPalSetupToken()
                // CHECK
                // Initial state
                assertIs<PayPalVaultUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<PayPalVaultUIState.Loading>(awaitItem())
                runCurrent() // Execute pending coroutine dispatchers
                awaitItem().let { state ->
                    assertIs<PayPalVaultUIState.LaunchIntent>(state)
                    assertEquals(MOCK_SETUP_TOKEN, state.setupToken)
                    assertEquals(MOCK_CLIENT_ID, state.clientId)
                }
                coVerify {
                    createSetupTokenUseCase(
                        any(),
                        any()
                    )
                }
                coVerify {
                    getPayPalClientIdUseCase(
                        any(),
                        any()
                    )
                }
            }
        }

    @Test
    fun `create setup token failure flow should update isLoading, call useCase, and update states on failure`() =
        runTest {
            prepareCreateSetupTokenFailure()
            // Allows for testing flow state
            viewModel.stateFlow.test {
                viewModel.createPayPalSetupToken()
                // CHECK
                // Initial state
                assertIs<PayPalVaultUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<PayPalVaultUIState.Loading>(awaitItem())
                runCurrent() // Execute pending coroutine dispatcher
                coVerify { createSetupTokenUseCase(any(), any()) }
                // Result state - failure
                awaitItem().let { state ->
                    assertIs<PayPalVaultUIState.Error>(state)
                    assertIs<PayPalVaultException.CreateSetupTokenException>(state.exception)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_SETUP_TOKEN_ERROR,
                        state.exception.message
                    )
                }
            }
        }

    @Test
    fun `get clientId failure flow should update isLoading, call useCase, and update states on failure`() =
        runTest {
            prepareCreateSetupTokenSuccess()
            prepareGetClientIdFailure()
            // Allows for testing flow state
            viewModel.stateFlow.test {
                viewModel.createPayPalSetupToken()
                // CHECK
                // Initial state
                assertIs<PayPalVaultUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<PayPalVaultUIState.Loading>(awaitItem())
                runCurrent() // Execute pending coroutine dispatcher
                coVerify { getPayPalClientIdUseCase(any(), any()) }
                // Result state - failure
                awaitItem().let { state ->
                    assertIs<PayPalVaultUIState.Error>(state)
                    assertIs<PayPalVaultException.GetPayPalClientIdException>(state.exception)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_CLIENT_ID_ERROR,
                        state.exception.message
                    )
                }
            }
        }

    @Test
    fun `create payment source should update isLoading, call useCase, and update state on success`() =
        runTest {
            preparePayPalCompleteSuccessFlow()
            viewModel.createPayPalSetupToken()
            runCurrent() // Execute pending coroutine dispatchers

            val mockToken = MobileSDKTestConstants.PayPalVault.MOCK_PAYMENT_TOKEN
            val mockEmail = MobileSDKTestConstants.PayPalVault.MOCK_EMAIL
            val mockResult = Result.success(PayPalPaymentTokenDetails(token = mockToken, email = mockEmail))
            coEvery {
                createPayPalVaultPaymentTokenUseCase(
                    any(),
                    any(),
                    any()
                )
            } returns mockResult

            viewModel.stateFlow.test {
                viewModel.createPayPalPaymentSourceToken()
                // CHECK
                // Initial state (from previous states)
                assertIs<PayPalVaultUIState.LaunchIntent>(awaitItem())
                // Loading state - before execution
                assertIs<PayPalVaultUIState.Loading>(awaitItem())
                runCurrent() // Execute pending coroutine dispatcher
                coVerify { createPayPalVaultPaymentTokenUseCase(any(), any(), any()) }
                // Result state - success
                awaitItem().let { state ->
                    assertIs<PayPalVaultUIState.Success>(state)
                    assertEquals(mockToken, state.details.token)
                    assertEquals(mockEmail, state.details.email)
                }
            }
        }

    @Test
    fun `create payment source should update isLoading, call useCase, and update state on failure`() =
        runTest {
            preparePayPalCompleteSuccessFlow()
            viewModel.createPayPalSetupToken()
            runCurrent() // Execute pending coroutine dispatchers

            val mockError = PayPalVaultException.CreatePaymentTokenException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.InternalServerError.value,
                    summary = ErrorSummary(
                        code = "payment_token_error",
                        message = MobileSDKTestConstants.Errors.MOCK_TOKENIZATION_ERROR
                    )
                )
            )
            val mockResult = Result.failure<PayPalPaymentTokenDetails>(mockError)
            coEvery {
                createPayPalVaultPaymentTokenUseCase(
                    any(),
                    any(),
                    any()
                )
            } returns mockResult

            viewModel.stateFlow.test {
                viewModel.createPayPalPaymentSourceToken()
                // CHECK
                // Initial state (from previous states)
                assertIs<PayPalVaultUIState.LaunchIntent>(awaitItem())
                // Loading state - before execution
                assertIs<PayPalVaultUIState.Loading>(awaitItem())
                runCurrent() // Execute pending coroutine dispatcher
                coVerify { createPayPalVaultPaymentTokenUseCase(any(), any(), any()) }
                // Result state - failure
                awaitItem().let { state ->
                    assertIs<PayPalVaultUIState.Error>(state)
                    assertIs<PayPalVaultException.CreatePaymentTokenException>(state.exception)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_TOKENIZATION_ERROR,
                        state.exception.message
                    )
                }
            }
        }

    @Test
    fun `resetResultState should reset UI state`() = runTest {
        preparePayPalCompleteSuccessFlow()
        viewModel.createPayPalSetupToken()
        runCurrent() // Execute pending coroutine dispatchers
        val mockToken = MobileSDKTestConstants.PayPalVault.MOCK_PAYMENT_TOKEN
        val mockEmail = MobileSDKTestConstants.PayPalVault.MOCK_EMAIL
        val mockResult = Result.success(PayPalPaymentTokenDetails(token = mockToken, email = mockEmail))
        coEvery {
            createPayPalVaultPaymentTokenUseCase(
                any(),
                any(),
                any()
            )
        } returns mockResult
        viewModel.createPayPalPaymentSourceToken()
        runCurrent()
        viewModel.stateFlow.test {
            assertIs<PayPalVaultUIState.Success>(awaitItem())
            // ACTION
            viewModel.resetResultState()
            // Result state - reset
            assertIs<PayPalVaultUIState.Idle>(awaitItem())
        }
    }

    // region Telemetry: one SdkError per failure path (PV1-PV5), merchant-visible results unchanged

    @Test
    fun `setup token SdkException failure reports it once and still sets the error (PV1)`() = runTest {
        prepareCreateSetupTokenFailure()

        viewModel.createPayPalSetupToken()
        runCurrent()

        val state = viewModel.stateFlow.value
        assertIs<PayPalVaultUIState.Error>(state)
        assertIs<PayPalVaultException.CreateSetupTokenException>(state.exception)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.PAYPAL_SAVE_PAYMENT_SOURCE, reported.widget)
        assertEquals("CreateSetupTokenException", reported.errorCode)
        assertNull(reported.error.cause)
        assertEquals("setup_token", reported.apiError?.summaryCode)
    }

    @Test
    fun `setup token failure that is no SdkException reports it once and is still dropped (PV1)`() = runTest {
        coEvery { createSetupTokenUseCase(any(), any()) } returns Result.failure(IllegalStateException())

        viewModel.createPayPalSetupToken()
        runCurrent()

        assertIs<PayPalVaultUIState.Loading>(viewModel.stateFlow.value)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.PAYPAL_SAVE_PAYMENT_SOURCE, reported.widget)
        assertNull(reported.errorCode)
        assertEquals(IllegalStateException::class.java.name, reported.error.type)
    }

    @Test
    fun `client id API failure reports the mapped exception with the raw cause and still sets it (PV2)`() = runTest {
        prepareCreateSetupTokenSuccess()
        coEvery { getPayPalClientIdUseCase(any(), any()) } returns Result.failure(
            ApiException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.InternalServerError.value,
                    summary = ErrorSummary(
                        code = "gateway_client_id",
                        message = MobileSDKTestConstants.Errors.MOCK_CLIENT_ID_ERROR
                    )
                )
            )
        )

        viewModel.createPayPalSetupToken()
        runCurrent()

        val state = viewModel.stateFlow.value
        assertIs<PayPalVaultUIState.Error>(state)
        assertIs<PayPalVaultException.GetPayPalClientIdException>(state.exception)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.PAYPAL_SAVE_PAYMENT_SOURCE, reported.widget)
        assertEquals("GetPayPalClientIdException", reported.errorCode)
        assertEquals(ApiException::class.java.name, reported.error.cause?.type)
        assertEquals(HttpStatusCode.InternalServerError.value, reported.apiError?.status)
        assertEquals("gateway_client_id", reported.apiError?.summaryCode)
    }

    @Test
    fun `client id SdkException failure reports the same exception with no cause and still sets it (PV2)`() =
        runTest {
            prepareCreateSetupTokenSuccess()
            prepareGetClientIdFailure()

            viewModel.createPayPalSetupToken()
            runCurrent()

            val state = viewModel.stateFlow.value
            assertIs<PayPalVaultUIState.Error>(state)
            assertIs<PayPalVaultException.GetPayPalClientIdException>(state.exception)
            val reported = telemetry.sdkErrors.single()
            assertEquals(SdkWidget.PAYPAL_SAVE_PAYMENT_SOURCE, reported.widget)
            assertEquals("GetPayPalClientIdException", reported.errorCode)
            assertNull(reported.error.cause)
            assertEquals("gateway_client_id", reported.apiError?.summaryCode)
        }

    @Test
    fun `payment token SdkException failure reports it once and still sets the error (PV3)`() = runTest {
        preparePayPalCompleteSuccessFlow()
        viewModel.createPayPalSetupToken()
        runCurrent()
        val mockError = PayPalVaultException.CreatePaymentTokenException(
            error = ApiErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                summary = ErrorSummary(
                    code = "payment_token_error",
                    message = MobileSDKTestConstants.Errors.MOCK_TOKENIZATION_ERROR
                )
            )
        )
        coEvery { createPayPalVaultPaymentTokenUseCase(any(), any(), any()) } returns Result.failure(mockError)

        viewModel.createPayPalPaymentSourceToken()
        runCurrent()

        val state = viewModel.stateFlow.value
        assertIs<PayPalVaultUIState.Error>(state)
        assertSame(mockError, state.exception)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.PAYPAL_SAVE_PAYMENT_SOURCE, reported.widget)
        assertEquals("CreatePaymentTokenException", reported.errorCode)
        assertNull(reported.error.cause)
        assertEquals("payment_token_error", reported.apiError?.summaryCode)
    }

    @Test
    fun `payment token failure that is no SdkException reports it once and is still dropped (PV3)`() = runTest {
        preparePayPalCompleteSuccessFlow()
        viewModel.createPayPalSetupToken()
        runCurrent()
        coEvery { createPayPalVaultPaymentTokenUseCase(any(), any(), any()) } returns
            Result.failure(IllegalStateException())

        viewModel.createPayPalPaymentSourceToken()
        runCurrent()

        assertIs<PayPalVaultUIState.Loading>(viewModel.stateFlow.value)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.PAYPAL_SAVE_PAYMENT_SOURCE, reported.widget)
        assertNull(reported.errorCode)
        assertEquals(IllegalStateException::class.java.name, reported.error.type)
    }

    @Test
    fun `reportWidgetError reports one PayPal vault SdkError and changes no state (PV4)`() = runTest {
        val before = viewModel.stateFlow.value

        viewModel.reportWidgetError(PayPalVaultException.PayPalSDKException(code = -1, description = "synthetic"))

        assertSame(before, viewModel.stateFlow.value)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.PAYPAL_SAVE_PAYMENT_SOURCE, reported.widget)
        assertEquals("PayPalSDKException", reported.errorCode)
        assertNull(reported.error.cause)
    }

    @Test
    fun `the vault result handler reports the web vault error it hands on once, and never the cancellation (PV4, PV5)`() {
        val source = WidgetSourceScan.read("feature/paypal/vault/presentation/PayPalSavePaymentSourceWidget.kt")
        val handler = WidgetSourceScan.function(source, "handlePayPalVaultResult")
        val cancellation = WidgetSourceScan.between(handler, "CancellationStatus.USER_INITIATED", "else ->")
        val webVault = WidgetSourceScan.between(handler, "else ->", "else -> Unit")

        assertEquals(1, WidgetSourceScan.count(source, "viewModel.reportWidgetError("))
        assertEquals(1, WidgetSourceScan.count(cancellation, "PayPalVaultException.CancellationException("))
        assertEquals(0, WidgetSourceScan.count(cancellation, "reportWidgetError("))
        assertEquals(1, WidgetSourceScan.count(cancellation, "completion("))
        assertEquals(1, WidgetSourceScan.count(webVault, "PayPalVaultException.PayPalSDKException("))
        assertEquals(1, WidgetSourceScan.count(webVault, "viewModel.reportWidgetError(failure)"))
        assertEquals(1, WidgetSourceScan.count(webVault, "completion(Result.failure(failure))"))
    }

    // endregion
}
