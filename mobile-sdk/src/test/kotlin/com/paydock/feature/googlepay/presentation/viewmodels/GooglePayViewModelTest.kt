package com.paydock.feature.googlepay.presentation.viewmodels

import app.cash.turbine.test
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.WalletConstants
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKConstants
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.GooglePayException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.telemetry.RecordingTelemetry
import com.paydock.core.telemetry.SdkWidget
import com.paydock.core.telemetry.WidgetSourceScan
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.googlepay.domain.model.integration.GooglePayWidgetConfig
import com.paydock.feature.googlepay.domain.usecase.CreateGooglePayTokenUseCase
import com.paydock.feature.googlepay.presentation.state.GooglePayUIState
import com.paydock.feature.googlepay.util.PaymentsUtil
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertSame
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.json.JSONArray
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
internal class GooglePayViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: GooglePayViewModel
    private lateinit var paymentsClient: PaymentsClient
    private lateinit var createGooglePayTokenUseCase: CreateGooglePayTokenUseCase
    private lateinit var telemetry: RecordingTelemetry

    @Before
    fun setup() {
        createGooglePayTokenUseCase = mockk()
        paymentsClient = mockk()
        telemetry = RecordingTelemetry()

        val mockAccessToken = MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN
        val mockServiceId = MobileSDKTestConstants.General.MOCK_GATEWAY_ID
        val isReadyToPayRequestJson = PaymentsUtil.createIsReadyToPayRequest()
        val paymentRequest =
            PaymentsUtil.createGooglePayRequest(
                amount = BigDecimal(10),
                amountLabel = "Goodies",
                currencyCode = "AUD",
                countryCode = "AU",
                merchantName = "unit_test",
                merchantIdentifier = "unit_test"
            )

        // This is to ensure we are able to mock the init function
        coEvery { paymentsClient.isReadyToPay(any()) } returns Tasks.forResult(true)

        viewModel = GooglePayViewModel(
            paymentsClient,
            GooglePayWidgetConfig(
                mockAccessToken,
                mockServiceId,
                isReadyToPayRequestJson,
                paymentRequest
            ),
            createGooglePayTokenUseCase,
            dispatchersProvider,
            telemetry
        )
    }

    @Test
    fun `ViewModel initialization when isReadyToPay returns false updates googlePayAvailable`() =
        runTest {
            // Re-initialize ViewModel with specific mock behavior for this test
            coEvery { paymentsClient.isReadyToPay(any()) } returns Tasks.forResult(false)

            val mockAccessToken = MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN
            val mockServiceId = MobileSDKTestConstants.General.MOCK_GATEWAY_ID
            val isReadyToPayRequestJson = PaymentsUtil.createIsReadyToPayRequest()
            val paymentRequest =
                PaymentsUtil.createGooglePayRequest(
                    amount = BigDecimal(10),
                    amountLabel = "Goodies",
                    currencyCode = "AUD",
                    countryCode = "AU",
                    merchantName = "unit_test",
                    merchantIdentifier = "unit_test"
                )

            viewModel = GooglePayViewModel(
                paymentsClient,
                GooglePayWidgetConfig(
                    mockAccessToken,
                    mockServiceId,
                    isReadyToPayRequestJson,
                    paymentRequest
                ),
                createGooglePayTokenUseCase,
                dispatchersProvider,
                telemetry
            )

            viewModel.googlePayAvailable.test {
                assertEquals(false, awaitItem())
            }
            viewModel.uiState.test {
                // Skip initial Idle state
                assertEquals(GooglePayUIState.Idle, awaitItem())
                // Now sets specific IsReadyToPayException error state
                awaitItem().let { state ->
                    assertIs<GooglePayUIState.Error>(state)
                    assertIs<GooglePayException.IsReadyToPayException>(state.exception)
                }
            }
        }

    @Test
    fun `handleGooglePayResultErrors sets timeout state on TIMEOUT`() = runTest {
        viewModel.handleGooglePayResultErrors(CommonStatusCodes.TIMEOUT)
        viewModel.uiState.test {
            awaitItem().let { state ->
                assertIs<GooglePayUIState.Error>(state)
                val actualException = state.exception
                assertIs<GooglePayException.SDKException.Timeout>(actualException)
                assertEquals(
                    MobileSDKConstants.GooglePayConfig.Errors.TIMEOUT_ERROR,
                    actualException.message
                )
                assertEquals(
                    "TIMEOUT",
                    actualException.statusCodeString
                )

            }
        }
    }

    @Test
    fun `handleGooglePayResultErrors sets play services error state on SIGN_IN_REQUIRED`() =
        runTest {
            viewModel.handleGooglePayResultErrors(CommonStatusCodes.SIGN_IN_REQUIRED)
            viewModel.uiState.test {
                awaitItem().let { state ->
                    assertIs<GooglePayUIState.Error>(state)
                    val actualException = state.exception
                    assertIs<GooglePayException.SDKException.PlayServicesError>(actualException)
                    assertEquals(
                        MobileSDKConstants.GooglePayConfig.Errors.PLAY_SERVICES_ERROR,
                        actualException.message
                    )
                    assertEquals(
                        "SIGN_IN_REQUIRED",
                        actualException.statusCodeString
                    )
                }
            }
        }

    @Test
    fun `handleGooglePayResultErrors sets unknown SDK exception for unhandled status code`() =
        runTest {
            val unknownStatusCode = 999 // A code not explicitly handled
            val expectedStatusString = "unknown status code: $unknownStatusCode"
            val expectedError =
                "[$expectedStatusString] An unexpected error occurred while processing Google Pay. Please try again later."
            viewModel.handleGooglePayResultErrors(unknownStatusCode)
            viewModel.uiState.test {
                awaitItem().let { state ->
                    assertIs<GooglePayUIState.Error>(state)
                    val actualException = state.exception
                    assertIs<GooglePayException.SDKException.UnknownSdkException>(actualException)
                    assertEquals(expectedError, actualException.message)
                    assertEquals(expectedStatusString, actualException.statusCodeString)
                }
            }
        }

    @Test
    fun `handleGooglePayResultErrors sets result state on DEVELOPER_ERROR`() = runTest {
        viewModel.handleGooglePayResultErrors(CommonStatusCodes.DEVELOPER_ERROR)
        viewModel.uiState.test {
            awaitItem().let { state ->
                assertIs<GooglePayUIState.Error>(state)
                val actualException = state.exception
                assertIs<GooglePayException.SDKException.DeveloperError>(actualException)
                assertEquals(
                    actualException.message,
                    MobileSDKConstants.GooglePayConfig.Errors.DEV_ERROR
                )
                assertEquals(
                    "DEVELOPER_ERROR",
                    actualException.statusCodeString
                )

            }
        }
    }

    @Test
    fun `handleGooglePayResultErrors sets result state on ERROR`() = runTest {
        viewModel.handleGooglePayResultErrors(CommonStatusCodes.ERROR)
        viewModel.uiState.test {
            awaitItem().let { state ->
                assertIs<GooglePayUIState.Error>(state)
                assertIs<GooglePayException.SDKException.ServiceError>(state.exception)
                assertEquals(
                    state.exception.message,
                    MobileSDKConstants.GooglePayConfig.Errors.GOOGLE_PAY_SERVICE_ERROR
                )
            }
        }
    }

    @Test
    fun `handleGooglePayResultErrors sets cancellation state on ERROR_CODE_USER_CANCELLED`() = runTest {
        val status = Status(WalletConstants.ERROR_CODE_USER_CANCELLED, "User cancelled request")
        viewModel.handleWalletResultErrors(status)
        viewModel.uiState.test {
            awaitItem().let { state ->
                assertIs<GooglePayUIState.Error>(state)
                // Now mapped specifically to SDKException.CancelledBySdk when originating from SDK
                assertIs<GooglePayException.SDKException.CancelledBySdk>(state.exception)
                assertEquals("CANCELED", (state.exception as GooglePayException.SDKException.CancelledBySdk).statusCodeString)
            }
        }
    }

    @Test
    fun `handleGooglePayResultErrors sets result state on ERROR_CODE_DEVELOPER_ERROR`() = runTest {
        val status = Status(WalletConstants.ERROR_CODE_DEVELOPER_ERROR, "Developer error occurred")
        viewModel.handleWalletResultErrors(status)
        viewModel.uiState.test {
            awaitItem().let { state ->
                assertIs<GooglePayUIState.Error>(state)
                // Now mapped specifically to SDKException.DeveloperError
                assertIs<GooglePayException.SDKException.DeveloperError>(state.exception)
                assertEquals("DEVELOPER_ERROR", (state.exception as GooglePayException.SDKException.DeveloperError).statusCodeString)
            }
        }
    }

    @Test
    fun `handleGooglePayResultErrors sets result state on all other errors`() = runTest {
        val status = Status(WalletConstants.ERROR_CODE_INTERNAL_ERROR, "An unexpected error occurred!")
        viewModel.handleWalletResultErrors(status)
        viewModel.uiState.test {
            awaitItem().let { state ->
                assertIs<GooglePayUIState.Error>(state)
                // Now mapped specifically to SDKException.ServiceError
                assertIs<GooglePayException.SDKException.ServiceError>(state.exception)
                assertEquals("8", state.exception.statusCodeString)
            }
        }
    }

    @Test
    fun `extractAllowedPaymentMethods returns correct string on valid request`() {
        val validRequest = PaymentsUtil.createGooglePayRequest(
            amount = BigDecimal(10),
            amountLabel = "Goodies",
            currencyCode = "AUD",
            countryCode = "AU",
            merchantName = "unit_test",
            merchantIdentifier = "unit_test"
        )
        val methods = viewModel.extractAllowedPaymentMethods(validRequest)
        val json = JSONArray(methods)
        assertEquals(1, json.length())
        val cardMethod = json.getJSONObject(0)
        assertEquals(MobileSDKConstants.GooglePayConfig.CARD_PAYMENT_TYPE, cardMethod.getString("type"))

        val parameters = cardMethod.getJSONObject("parameters")
        assertEquals(true, parameters.getBoolean("billingAddressRequired"))
        val billingAddressParameters = parameters.getJSONObject("billingAddressParameters")
        assertEquals(
            MobileSDKConstants.GooglePayConfig.BILLING_ADDRESS_FORMAT,
            billingAddressParameters.getString("format")
        )

        // Default `phoneNumberRequired` in PaymentsUtil.createGooglePayRequest is false in this test.
        assertEquals(false, billingAddressParameters.getBoolean("phoneNumberRequired"))

        assertEquals(
            MobileSDKConstants.GooglePayConfig.ALLOWED_CARD_NETWORKS,
            (
                parameters.getJSONArray("allowedCardNetworks").let { arr ->
                    List(arr.length()) { i -> arr.getString(i) }
                }
                )
        )

        val tokenizationSpecification = cardMethod.getJSONObject("tokenizationSpecification")
        assertEquals(
            MobileSDKConstants.GooglePayConfig.TOKENIZATION_TYPE,
            tokenizationSpecification.getString("type")
        )
        val tokenParams = tokenizationSpecification.getJSONObject("parameters")
        assertEquals("unit_test", tokenParams.getString("gatewayMerchantId"))
        assertEquals(MobileSDKConstants.GooglePayConfig.GATEWAY, tokenParams.getString("gateway"))
    }

    @Test
    fun `extractAllowedPaymentMethods sets error state on invalid request`() = runTest {
        val validRequest = PaymentsUtil.createGooglePayRequest(
            amount = BigDecimal(10),
            amountLabel = "Goodies",
            currencyCode = "AUD",
            countryCode = "AU",
            merchantName = "unit_test",
            merchantIdentifier = "unit_test"
        )
        val invalidRequest = validRequest.copy(allowedPaymentMethods = emptyList())

        val methods = viewModel.extractAllowedPaymentMethods(invalidRequest)
        assertNull(methods)

        viewModel.uiState.test {
            awaitItem().let { state ->
                assertIs<GooglePayUIState.Error>(state)
                assertTrue(state.exception is GooglePayException.InitialisationException)
            }
        }
    }

    @Test
    fun `processGooglePayPaymentResult fails to extract google token and updates error state`() =
        runTest {
            val paymentData = mockk<PaymentData>()
            val exception = Exception("Token extraction failed")
            every { paymentData.toJson() } throws exception

            viewModel.processGooglePayPaymentResult(paymentData)
            viewModel.uiState.test {
                awaitItem().let { state ->
                    assertTrue(state is GooglePayUIState.Error)
                    assertTrue((state as GooglePayUIState.Error).exception is GooglePayException.ResultException)
                }
            }
        }

    @Test
    fun `resetResultState resets state to Idle`() = runTest {
        viewModel.handleGooglePayResultErrors(CommonStatusCodes.ERROR)
        viewModel.uiState.test {
            assertIs<GooglePayUIState.Error>(awaitItem())
            viewModel.resetResultState()
            assertIs<GooglePayUIState.Idle>(awaitItem())
        }
    }

    // region Telemetry: one SdkError per failure path (GP1-GP8), merchant-visible results unchanged

    private fun makeViewModel(reporting: RecordingTelemetry) = GooglePayViewModel(
        paymentsClient,
        GooglePayWidgetConfig(
            MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
            MobileSDKTestConstants.General.MOCK_GATEWAY_ID,
            PaymentsUtil.createIsReadyToPayRequest(),
            PaymentsUtil.createGooglePayRequest(
                amount = BigDecimal(10),
                amountLabel = "Goodies",
                currencyCode = "AUD",
                countryCode = "AU",
                merchantName = "unit_test",
                merchantIdentifier = "unit_test"
            )
        ),
        createGooglePayTokenUseCase,
        dispatchersProvider,
        reporting
    )

    @Test
    fun `not ready to pay reports no SdkError and still sets IsReadyToPayException (GP1)`() = runTest {
        coEvery { paymentsClient.isReadyToPay(any()) } returns Tasks.forResult(false)
        val reporting = RecordingTelemetry()

        val failing = makeViewModel(reporting)
        val state = failing.uiState.first { it is GooglePayUIState.Error }

        assertIs<GooglePayUIState.Error>(state)
        assertIs<GooglePayException.IsReadyToPayException>(state.exception)
        assertTrue(reporting.sdkErrors.isEmpty())
    }

    @Test
    fun `isReadyToPay ApiException reports one SdkError with the gms cause and still sets IsReadyToPayException (GP2)`() =
        runTest {
            coEvery { paymentsClient.isReadyToPay(any()) } returns
                Tasks.forException(ApiException(Status(CommonStatusCodes.NETWORK_ERROR)))
            val reporting = RecordingTelemetry()

            val failing = makeViewModel(reporting)
            val state = failing.uiState.first { it is GooglePayUIState.Error }

            assertIs<GooglePayUIState.Error>(state)
            assertIs<GooglePayException.IsReadyToPayException>(state.exception)
            val reported = reporting.sdkErrors.single()
            assertEquals(SdkWidget.GOOGLE_PAY, reported.widget)
            assertEquals("IsReadyToPayException", reported.errorCode)
            assertEquals(ApiException::class.java.name, reported.error.cause?.type)
        }

    @Test
    fun `allowed payment methods failure reports once per view model and still sets InitialisationException (GP3)`() =
        runTest {
            val invalidRequest = PaymentsUtil.createGooglePayRequest(
                amount = BigDecimal(10),
                amountLabel = "Goodies",
                currencyCode = "AUD",
                countryCode = "AU",
                merchantName = "unit_test",
                merchantIdentifier = "unit_test"
            ).copy(allowedPaymentMethods = emptyList())

            // Runs on every composition: the second call must not report again.
            assertNull(viewModel.extractAllowedPaymentMethods(invalidRequest))
            assertNull(viewModel.extractAllowedPaymentMethods(invalidRequest))

            val state = viewModel.uiState.value
            assertIs<GooglePayUIState.Error>(state)
            assertIs<GooglePayException.InitialisationException>(state.exception)
            val reported = telemetry.sdkErrors.single()
            assertEquals(SdkWidget.GOOGLE_PAY, reported.widget)
            assertEquals("InitialisationException", reported.errorCode)
            assertEquals(IllegalArgumentException::class.java.name, reported.error.cause?.type)
        }

    @Test
    fun `cancellation reports no SdkError and still sets CancellationException (GP4)`() = runTest {
        viewModel.handleCancellationResult()

        val state = viewModel.uiState.value
        assertIs<GooglePayUIState.Error>(state)
        assertIs<GooglePayException.CancellationException>(state.exception)
        assertTrue(telemetry.sdkErrors.isEmpty())
    }

    @Test
    fun `result parse failure reports one SdkError with the parse cause and still sets ResultException (GP5)`() =
        runTest {
            val paymentData = mockk<PaymentData>()
            every { paymentData.toJson() } throws IllegalStateException()

            viewModel.processGooglePayPaymentResult(paymentData)

            val state = viewModel.uiState.value
            assertIs<GooglePayUIState.Error>(state)
            assertIs<GooglePayException.ResultException>(state.exception)
            val reported = telemetry.sdkErrors.single()
            assertEquals(SdkWidget.GOOGLE_PAY, reported.widget)
            assertEquals("ResultException", reported.errorCode)
            assertEquals(IllegalStateException::class.java.name, reported.error.cause?.type)
        }

    @Test
    fun `token API failure reports the ResultException with the mapped cause and its API fields (GP6)`() = runTest {
        val paymentData = mockk<PaymentData>()
        every { paymentData.toJson() } returns
            """{"paymentMethodData":{"tokenizationData":{"token":"unit_test_token"},"info":{"cardNetwork":"VISA"}}}"""
        every { android.util.Base64.encodeToString(any(), any()) } answers {
            java.util.Base64.getEncoder().encodeToString(firstArg<ByteArray>())
        }
        val mapped = GooglePayException.TokenisingGooglePayException(
            error = ApiErrorResponse(
                status = HttpStatusCode.BadRequest.value,
                summary = ErrorSummary(
                    code = "invalid_token",
                    message = MobileSDKTestConstants.Errors.MOCK_TOKENIZATION_ERROR
                )
            )
        )
        coEvery { createGooglePayTokenUseCase(any(), any()) } returns Result.failure(mapped)

        viewModel.processGooglePayPaymentResult(paymentData)
        val state = viewModel.uiState.first { it is GooglePayUIState.Error }

        assertIs<GooglePayUIState.Error>(state)
        assertIs<GooglePayException.ResultException>(state.exception)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.GOOGLE_PAY, reported.widget)
        assertEquals("ResultException", reported.errorCode)
        assertEquals(mapped.javaClass.name, reported.error.cause?.type)
        assertEquals(HttpStatusCode.BadRequest.value, reported.apiError?.status)
        assertEquals("invalid_token", reported.apiError?.summaryCode)
    }

    @Test
    fun `Google Pay failing status reports one SdkError and still sets the mapped SDKException (GP7)`() = runTest {
        viewModel.handleWalletResultErrors(Status(CommonStatusCodes.TIMEOUT))

        val state = viewModel.uiState.value
        assertIs<GooglePayUIState.Error>(state)
        assertIs<GooglePayException.SDKException.Timeout>(state.exception)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.GOOGLE_PAY, reported.widget)
        assertEquals("Timeout", reported.errorCode)
        assertNull(reported.error.cause)
    }

    @Test
    fun `Google Pay cancelled status reports no SdkError and still sets CancelledBySdk (GP7)`() = runTest {
        listOf(CommonStatusCodes.CANCELED, WalletConstants.ERROR_CODE_USER_CANCELLED).forEach { statusCode ->
            viewModel.handleGooglePayResultErrors(statusCode)

            val state = viewModel.uiState.value
            assertIs<GooglePayUIState.Error>(state)
            assertIs<GooglePayException.SDKException.CancelledBySdk>(state.exception)
        }
        assertTrue(telemetry.sdkErrors.isEmpty())
    }

    @Test
    fun `Google Pay sign-in required status reports no SdkError and still sets PlayServicesError (GP7)`() = runTest {
        viewModel.handleWalletResultErrors(Status(CommonStatusCodes.SIGN_IN_REQUIRED))

        val state = viewModel.uiState.value
        assertIs<GooglePayUIState.Error>(state)
        assertIs<GooglePayException.SDKException.PlayServicesError>(state.exception)
        assertTrue(telemetry.sdkErrors.isEmpty())
    }

    @Test
    fun `reportWidgetError reports one Google Pay SdkError with its cause and changes no state (GP8)`() = runTest {
        val failure = GooglePayException.IsReadyToPayException(
            MobileSDKConstants.GooglePayConfig.Errors.IS_READY_TO_PAY_ERROR
        )
        val before = viewModel.uiState.value

        viewModel.reportWidgetError(failure, cause = IllegalStateException())

        assertSame(before, viewModel.uiState.value)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.GOOGLE_PAY, reported.widget)
        assertEquals("IsReadyToPayException", reported.errorCode)
        assertEquals(IllegalStateException::class.java.name, reported.error.cause?.type)
    }

    @Test
    fun `the Pay button onError reports the failure it hands on, once, with its cause (GP8)`() {
        val source = WidgetSourceScan.read("feature/googlepay/presentation/GooglePayWidget.kt")
        val onError = WidgetSourceScan.between(source, "onError = {", "enabled =")

        assertEquals(1, WidgetSourceScan.count(source, "viewModel.reportWidgetError("))
        assertEquals(1, WidgetSourceScan.count(onError, "viewModel.reportWidgetError(failure, cause = it)"))
        assertEquals(1, WidgetSourceScan.count(onError, "completion(Result.failure(failure))"))
    }

    // endregion
}
