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
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.googlepay.domain.model.integration.GooglePayWidgetConfig
import com.paydock.feature.googlepay.domain.usecase.CreateGooglePayTokenUseCase
import com.paydock.feature.googlepay.presentation.state.GooglePayUIState
import com.paydock.feature.googlepay.util.PaymentsUtil
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Before
    fun setup() {
        createGooglePayTokenUseCase = mockk()
        paymentsClient = mockk()

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
            dispatchersProvider
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
                dispatchersProvider
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
}
