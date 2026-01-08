package com.paydock.feature.zip.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.ZipException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.zip.domain.model.ZipCallbackData
import com.paydock.feature.zip.domain.model.ZipStatus
import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig
import com.paydock.feature.zip.domain.usecase.CreateZipPaymentSourceTokenUseCase
import com.paydock.feature.zip.domain.usecase.InitializeZipCheckoutUseCase
import com.paydock.feature.zip.presentation.state.ZipUIState
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal
import kotlin.test.assertIs

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
internal class ZipViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: ZipViewModel
    private lateinit var initializeZipCheckoutUseCase: InitializeZipCheckoutUseCase
    private lateinit var createZipPaymentSourceTokenUseCase: CreateZipPaymentSourceTokenUseCase

    private val testConfig = ZipWidgetConfig(
        accessToken = MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
        gatewayId = MobileSDKTestConstants.General.MOCK_GATEWAY_ID,
        amount = BigDecimal("100.00"),
        currency = "AUD",
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com",
        phone = "+1234567890",
        billing = ZipWidgetConfig.Address(
            firstName = "John",
            lastName = "Doe",
            line1 = "123 Main St",
            city = "Sydney",
            state = "NSW",
            postcode = "2000",
            country = "AU"
        )
    )

    private fun setupInitializeCheckoutSuccess() {
        val mockResult = Result.success(
            Pair(
                MobileSDKTestConstants.Zip.MOCK_CHECKOUT_URL,
                MobileSDKTestConstants.Zip.MOCK_CHECKOUT_TOKEN
            )
        )
        coEvery {
            initializeZipCheckoutUseCase(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                any()
            )
        } returns mockResult
    }

    private fun setupInitializeCheckoutFailure() {
        val mockError = ZipException.FetchingCheckoutUrlException(
            error = ApiErrorResponse(
                status = HttpStatusCode.BadRequest.value,
                summary = ErrorSummary(
                    code = "invalid_gateway",
                    message = MobileSDKTestConstants.Errors.MOCK_INVALID_GATEWAY_ID_ERROR
                )
            )
        )
        val mockResult = Result.failure<Pair<String, String>>(mockError)
        coEvery {
            initializeZipCheckoutUseCase(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                any()
            )
        } returns mockResult
    }

    private fun setupCreatePaymentSourceTokenSuccess() {
        val mockResult = Result.success(MobileSDKTestConstants.Zip.MOCK_PAYMENT_SOURCE_TOKEN)
        coEvery {
            createZipPaymentSourceTokenUseCase(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.Zip.MOCK_CHECKOUT_TOKEN,
                MobileSDKTestConstants.General.MOCK_GATEWAY_ID
            )
        } returns mockResult
    }

    private fun setupCreatePaymentSourceTokenFailure() {
        val mockError = ZipException.CreatingPaymentSourceTokenException(
            error = ApiErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                summary = ErrorSummary(
                    code = "token_creation_failed",
                    message = "Failed to create payment source token"
                )
            )
        )
        val mockResult = Result.failure<String>(mockError)
        coEvery {
            createZipPaymentSourceTokenUseCase(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                MobileSDKTestConstants.Zip.MOCK_CHECKOUT_TOKEN,
                MobileSDKTestConstants.General.MOCK_GATEWAY_ID
            )
        } returns mockResult
    }

    @Before
    fun setup() {
        initializeZipCheckoutUseCase = mockk()
        createZipPaymentSourceTokenUseCase = mockk()

        viewModel = ZipViewModel(
            testConfig,
            initializeZipCheckoutUseCase,
            createZipPaymentSourceTokenUseCase,
            dispatchersProvider,
            SavedStateHandle()
        )
    }

    // MARK: - Initialization Tests

    @Test
    fun `test initial state is Idle`() = runTest {
        viewModel.stateFlow.test {
            assertIs<ZipUIState.Idle>(awaitItem())
        }
    }

    // MARK: - Initialize Checkout Tests

    @Test
    fun `test initializeCheckout success updates state to LaunchCheckout`() = runTest {
        setupInitializeCheckoutSuccess()

        viewModel.stateFlow.test {
            // Initial state
            assertIs<ZipUIState.Idle>(awaitItem())

            // ACTION
            viewModel.initializeCheckout()

            // Loading state
            assertIs<ZipUIState.Loading>(awaitItem())

            // CHECK
            coVerify {
                initializeZipCheckoutUseCase(
                    MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                    any()
                )
            }

            // LaunchCheckout state
            awaitItem().let { state ->
                assertIs<ZipUIState.LaunchCheckout>(state)
                kotlin.test.assertEquals(MobileSDKTestConstants.Zip.MOCK_CHECKOUT_URL, state.checkoutUrl)
                kotlin.test.assertEquals(MobileSDKTestConstants.Zip.MOCK_CHECKOUT_TOKEN, state.checkoutToken)
            }
        }
    }

    @Test
    fun `test initializeCheckout failure updates state to Error`() = runTest {
        setupInitializeCheckoutFailure()

        viewModel.stateFlow.test {
            // Initial state
            assertIs<ZipUIState.Idle>(awaitItem())

            // ACTION
            viewModel.initializeCheckout()

            // Loading state
            assertIs<ZipUIState.Loading>(awaitItem())

            // CHECK
            coVerify {
                initializeZipCheckoutUseCase(
                    MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                    any()
                )
            }

            // Error state
            awaitItem().let { state ->
                assertIs<ZipUIState.Error>(state)
                assertIs<ZipException.FetchingCheckoutUrlException>(state.exception)
            }
        }
    }

    // MARK: - Handle Zip Callback Tests

    @Test
    fun `test handleZipCallback with approved status creates payment source token`() = runTest {
        setupInitializeCheckoutSuccess()
        setupCreatePaymentSourceTokenSuccess()

        viewModel.stateFlow.test {
            // Initial state
            assertIs<ZipUIState.Idle>(awaitItem())

            // First, initialize checkout to get the checkout token
            viewModel.initializeCheckout()
            assertIs<ZipUIState.Loading>(awaitItem())
            assertIs<ZipUIState.LaunchCheckout>(awaitItem())

            // ACTION - Handle approved callback
            val callbackData = ZipCallbackData(
                status = ZipStatus.APPROVED,
                checkoutId = MobileSDKTestConstants.Zip.MOCK_CHECKOUT_ID,
                orderId = MobileSDKTestConstants.Zip.MOCK_ORDER_ID
            )
            viewModel.handleZipCallback(callbackData)

            // Loading state for token creation
            assertIs<ZipUIState.Loading>(awaitItem())

            // CHECK
            coVerify {
                createZipPaymentSourceTokenUseCase(
                    MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                    MobileSDKTestConstants.Zip.MOCK_CHECKOUT_TOKEN,
                    MobileSDKTestConstants.General.MOCK_GATEWAY_ID
                )
            }

            // Success state
            awaitItem().let { state ->
                assertIs<ZipUIState.Success>(state)
                kotlin.test.assertEquals(MobileSDKTestConstants.Zip.MOCK_PAYMENT_SOURCE_TOKEN, state.token)
            }
        }
    }

    @Test
    fun `test handleZipCallback with declined status updates state to Error`() = runTest {
        viewModel.stateFlow.test {
            // Initial state
            assertIs<ZipUIState.Idle>(awaitItem())

            // ACTION
            val callbackData = ZipCallbackData(
                status = ZipStatus.DECLINED,
                checkoutId = MobileSDKTestConstants.Zip.MOCK_CHECKOUT_ID
            )
            viewModel.handleZipCallback(callbackData)

            // CHECK
            awaitItem().let { state ->
                assertIs<ZipUIState.Error>(state)
                assertIs<ZipException.DeclinedException>(state.exception)
            }
        }
    }

    @Test
    fun `test handleZipCallback with cancelled status updates state to Error`() = runTest {
        viewModel.stateFlow.test {
            // Initial state
            assertIs<ZipUIState.Idle>(awaitItem())

            // ACTION
            val callbackData = ZipCallbackData(
                status = ZipStatus.CANCELLED,
                checkoutId = MobileSDKTestConstants.Zip.MOCK_CHECKOUT_ID
            )
            viewModel.handleZipCallback(callbackData)

            // CHECK
            awaitItem().let { state ->
                assertIs<ZipUIState.Error>(state)
                assertIs<ZipException.CancellationException>(state.exception)
            }
        }
    }

    @Test
    fun `test handleZipCallback with referred status updates state to Error`() = runTest {
        viewModel.stateFlow.test {
            // Initial state
            assertIs<ZipUIState.Idle>(awaitItem())

            // ACTION
            val callbackData = ZipCallbackData(
                status = ZipStatus.REFERRED,
                checkoutId = MobileSDKTestConstants.Zip.MOCK_CHECKOUT_ID
            )
            viewModel.handleZipCallback(callbackData)

            // CHECK
            awaitItem().let { state ->
                assertIs<ZipUIState.Error>(state)
                assertIs<ZipException.ReferredException>(state.exception)
            }
        }
    }

    @Test
    fun `test handleZipCallback with unexpected status updates state to Error`() = runTest {
        viewModel.stateFlow.test {
            // Initial state
            assertIs<ZipUIState.Idle>(awaitItem())

            // ACTION
            val callbackData = ZipCallbackData(
                status = ZipStatus.UNEXPECTED,
                checkoutId = MobileSDKTestConstants.Zip.MOCK_CHECKOUT_ID
            )
            viewModel.handleZipCallback(callbackData)

            // CHECK
            awaitItem().let { state ->
                assertIs<ZipUIState.Error>(state)
                assertIs<ZipException.UnexpectedStatusException>(state.exception)
            }
        }
    }

    @Test
    fun `test handleZipCallback with approved but no checkout token returns error`() = runTest {
        // Don't initialize checkout - no checkout token will be available

        viewModel.stateFlow.test {
            // Initial state
            assertIs<ZipUIState.Idle>(awaitItem())

            // ACTION
            val callbackData = ZipCallbackData(
                status = ZipStatus.APPROVED,
                checkoutId = MobileSDKTestConstants.Zip.MOCK_CHECKOUT_ID
            )
            viewModel.handleZipCallback(callbackData)

            // CHECK - Should error because no checkout token
            awaitItem().let { state ->
                assertIs<ZipUIState.Error>(state)
                assertIs<ZipException.UnknownException>(state.exception)
            }
        }
    }

    @Test
    fun `test handleZipCallback with approved but token creation fails`() = runTest {
        setupInitializeCheckoutSuccess()
        setupCreatePaymentSourceTokenFailure()

        viewModel.stateFlow.test {
            // Initial state
            assertIs<ZipUIState.Idle>(awaitItem())

            // Initialize checkout first
            viewModel.initializeCheckout()
            assertIs<ZipUIState.Loading>(awaitItem())
            assertIs<ZipUIState.LaunchCheckout>(awaitItem())

            // ACTION - Handle approved callback
            val callbackData = ZipCallbackData(
                status = ZipStatus.APPROVED,
                checkoutId = MobileSDKTestConstants.Zip.MOCK_CHECKOUT_ID
            )
            viewModel.handleZipCallback(callbackData)

            // Loading state
            assertIs<ZipUIState.Loading>(awaitItem())

            // CHECK
            awaitItem().let { state ->
                assertIs<ZipUIState.Error>(state)
                assertIs<ZipException.CreatingPaymentSourceTokenException>(state.exception)
            }
        }
    }

    // MARK: - WebView Error Tests

    @Test
    fun `test handleWebViewError updates state to Error`() = runTest {
        viewModel.stateFlow.test {
            // Initial state
            assertIs<ZipUIState.Idle>(awaitItem())

            // ACTION
            viewModel.handleWebViewError(-1, "WebView error occurred")

            // CHECK
            awaitItem().let { state ->
                assertIs<ZipUIState.Error>(state)
                assertIs<ZipException.WebViewException>(state.exception)
            }
        }
    }

    // MARK: - User Cancellation Tests

    @Test
    fun `test handleUserCancellation updates state to Error`() = runTest {
        viewModel.stateFlow.test {
            // Initial state
            assertIs<ZipUIState.Idle>(awaitItem())

            // ACTION
            viewModel.handleUserCancellation()

            // CHECK
            awaitItem().let { state ->
                assertIs<ZipUIState.Error>(state)
                assertIs<ZipException.CancellationException>(state.exception)
            }
        }
    }

    // MARK: - Reset State Tests

    @Test
    fun `test resetResultState resets state to Idle`() = runTest {
        setupInitializeCheckoutSuccess()

        viewModel.stateFlow.test {
            // Initial state
            assertIs<ZipUIState.Idle>(awaitItem())

            // Move to another state
            viewModel.initializeCheckout()
            assertIs<ZipUIState.Loading>(awaitItem())
            assertIs<ZipUIState.LaunchCheckout>(awaitItem())

            // ACTION
            viewModel.resetResultState()

            // CHECK
            assertIs<ZipUIState.Idle>(awaitItem())
        }
    }

    // MARK: - Configuration Tests

    @Test
    fun `test config is properly set`() {
        kotlin.test.assertEquals(testConfig.accessToken, MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN)
        kotlin.test.assertEquals(testConfig.gatewayId, MobileSDKTestConstants.General.MOCK_GATEWAY_ID)
        kotlin.test.assertEquals(testConfig.amount, BigDecimal("100.00"))
        kotlin.test.assertEquals(testConfig.currency, "AUD")
        kotlin.test.assertEquals(testConfig.firstName, "John")
        kotlin.test.assertEquals(testConfig.lastName, "Doe")
        kotlin.test.assertEquals(testConfig.email, "john.doe@example.com")
    }
}
