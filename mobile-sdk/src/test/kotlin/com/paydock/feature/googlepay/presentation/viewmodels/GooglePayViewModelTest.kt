package com.paydock.feature.googlepay.presentation.viewmodels

import app.cash.turbine.test
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
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.googlepay.domain.model.GooglePayWidgetConfig
import com.paydock.feature.googlepay.presentation.state.GooglePayUIState
import com.paydock.feature.googlepay.util.PaymentsUtil
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
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
    private lateinit var captureWalletChargeUseCase: CaptureWalletChargeUseCase
    private lateinit var declineWalletChargeUseCase: DeclineWalletChargeUseCase
    private lateinit var getWalletCallbackUseCase: GetWalletCallbackUseCase

    @Before
    fun setup() {
        captureWalletChargeUseCase = mockk()
        declineWalletChargeUseCase = mockk()
        getWalletCallbackUseCase = mockk()
        paymentsClient = mockk()
        val isReadyToPayRequestJson = PaymentsUtil.createIsReadyToPayRequest()
        viewModel = GooglePayViewModel(
            paymentsClient,
            GooglePayWidgetConfig(
                isReadyToPayRequestJson,
                JSONObject()
            ),
            captureWalletChargeUseCase,
            declineWalletChargeUseCase,
            getWalletCallbackUseCase,
            dispatchersProvider
        )
        val mockToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        viewModel.setWalletToken(mockToken)
        // This is to ensure we are able to mock the init function
        coEvery { paymentsClient.isReadyToPay(any()) } returns Tasks.forResult(true)
    }

    @Test
    fun `ViewModel initialization when isReadyToPay returns false updates googlePayAvailable`() =
        runTest {
            // Re-initialize ViewModel with specific mock behavior for this test
            coEvery { paymentsClient.isReadyToPay(any()) } returns Tasks.forResult(false)
            viewModel = GooglePayViewModel( // Recreate to trigger init with new mock
                paymentsClient,
                GooglePayWidgetConfig(PaymentsUtil.createIsReadyToPayRequest(), JSONObject()),
                captureWalletChargeUseCase,
                declineWalletChargeUseCase,
                getWalletCallbackUseCase,
                dispatchersProvider
            ).apply { setWalletToken(MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN) }

            viewModel.googlePayAvailable.test {
                assertEquals(false, awaitItem())
            }
            viewModel.uiState.test {
                // Assuming no specific error state is set, just availability changes
                assertIs<GooglePayUIState.Idle>(awaitItem())
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
                    CommonStatusCodes.getStatusCodeString(CommonStatusCodes.TIMEOUT),
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
                        CommonStatusCodes.getStatusCodeString(CommonStatusCodes.SIGN_IN_REQUIRED),
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
                    CommonStatusCodes.getStatusCodeString(CommonStatusCodes.DEVELOPER_ERROR),
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
                assertIs<GooglePayException.CancellationException>(state.exception)
                assertEquals(state.exception.message, "User cancelled request")
            }
        }
    }

    @Test
    fun `handleGooglePayResultErrors sets cancellation state on ERROR_CODE_DEVELOPER_ERROR`() = runTest {
        val status = Status(WalletConstants.ERROR_CODE_DEVELOPER_ERROR, "Developer error occurred")
        viewModel.handleWalletResultErrors(status)
        viewModel.uiState.test {
            awaitItem().let { state ->
                assertIs<GooglePayUIState.Error>(state)
                assertIs<GooglePayException.ResultException>(state.exception)
                assertEquals(state.exception.message, "Developer error occurred")
            }
        }
    }

    @Test
    fun `handleGooglePayResultErrors sets cancellation state on all other errors`() = runTest {
        val status = Status(WalletConstants.ERROR_CODE_INTERNAL_ERROR, "An unexpected error occurred!")
        viewModel.handleWalletResultErrors(status)
        viewModel.uiState.test {
            awaitItem().let { state ->
                assertIs<GooglePayUIState.Error>(state)
                assertIs<GooglePayException.ResultException>(state.exception)
                assertEquals(state.exception.message, "An unexpected error occurred!")
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
        assertEquals(
            """[{"type":"CARD","parameters":{"allowedAuthMethods":["PAN_ONLY","CRYPTOGRAM_3DS"],"billingAddressRequired":true,"billingAddressParameters":{"format":"FULL"},"allowedCardNetworks":["AMEX","DISCOVER","JCB","MASTERCARD","VISA"]},"tokenizationSpecification":{"type":"PAYMENT_GATEWAY","parameters":{"gatewayMerchantId":"unit_test","gateway":"paydock"}}}]""",
            methods
        )
    }

    @Test
    fun `extractAllowedPaymentMethods sets error state on invalid request`() = runTest {
        val invalidRequest = JSONObject("{}")

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
    fun `processGooglePayPaymentResult extracts google token and calls CaptureWalletTransaction with success result`() =
        runTest {
            val paymentData = mockk<PaymentData>()
            val googlePayToken = "googlePayToken"
            every { paymentData.toJson() } returns """{"paymentMethodData": {"tokenizationData": {"token": "$googlePayToken"}}}"""
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
            coEvery { captureWalletChargeUseCase(any(), any()) } returns mockResult
            viewModel.processGooglePayPaymentResult(paymentData)

            viewModel.uiState.test {
                // Initial state
                assertIs<GooglePayUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<GooglePayUIState.Loading>(awaitItem())
                coVerify { captureWalletChargeUseCase(any(), any()) }
                awaitItem().let { state ->
                    assertIs<GooglePayUIState.Success>(state)
                    assertEquals(mockResult.getOrNull(), state.chargeData)
                }
            }
        }

    @Test
    fun `processGooglePayPaymentResult extracts google token and calls CaptureWalletTransaction with failure result`() =
        runTest {
            val paymentData = mockk<PaymentData>()
            val googlePayToken = "googlePayToken"
            every { paymentData.toJson() } returns """{"paymentMethodData": {"tokenizationData": {"token": "$googlePayToken"}}}"""

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

            viewModel.processGooglePayPaymentResult(paymentData)
            viewModel.uiState.test {
                // Initial state
                assertIs<GooglePayUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<GooglePayUIState.Loading>(awaitItem())
                coVerify { captureWalletChargeUseCase(any(), any()) }
                awaitItem().let { state ->
                    assertIs<GooglePayUIState.Error>(state)
                    assertTrue(state.exception is GooglePayException.CapturingChargeException)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_GENERAL_ERROR,
                        state.exception.message
                    )
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
    fun `resetResultState should reset UI state`() = runTest {
        viewModel.handleGooglePayResultErrors(CommonStatusCodes.CANCELED)
        // Assert state is Idle
        viewModel.uiState.test {
            assertIs<GooglePayUIState.Error>(awaitItem())
            viewModel.resetResultState()
            assertIs<GooglePayUIState.Idle>(awaitItem())
        }
    }
}
