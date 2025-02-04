package com.paydock.feature.afterpay.presentation.viewmodels

import android.content.Context
import app.cash.turbine.test
import com.afterpay.android.CancellationStatus
import com.paydock.MobileSDK
import com.paydock.core.BaseUnitTest
import com.paydock.core.MobileSDKConstants
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.AfterpayException
import com.paydock.core.domain.model.Environment
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.afterpay.domain.model.integration.AfterpaySDKConfig
import com.paydock.feature.afterpay.domain.model.integration.AfterpayShippingOption
import com.paydock.feature.afterpay.domain.model.integration.AfterpayShippingOptionUpdate
import com.paydock.feature.afterpay.presentation.state.AfterpayUIState
import com.paydock.feature.wallet.data.dto.CaptureChargeResponse
import com.paydock.feature.wallet.data.dto.ChargeDeclineResponse
import com.paydock.feature.wallet.data.dto.WalletCallbackResponse
import com.paydock.feature.wallet.data.mapper.asEntity
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.ui.WalletCallback
import com.paydock.feature.wallet.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import com.paydock.initializeMobileSDK
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.inject
import org.mockito.junit.MockitoJUnitRunner
import java.util.Currency
import java.util.Locale
import kotlin.test.assertIs

@Suppress("MaxLineLength")
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
internal class AfterpayViewModelTest : BaseUnitTest() {
    // This requires using the MobileSDK for the environment mapping

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dispatchersProvider: DispatchersProvider
    private lateinit var viewModel: AfterpayViewModel
    private lateinit var captureWalletChargeUseCase: CaptureWalletChargeUseCase
    private lateinit var declineWalletChargeUseCase: DeclineWalletChargeUseCase
    private lateinit var getWalletCallbackUseCase: GetWalletCallbackUseCase

    private lateinit var context: Context

    @Before
    fun setup() {
        // Mock the Context object
        context = mockk()
        // Configure the getApplicationContext() method to return the mock Context
        every { context.applicationContext } returns context
        // We need to initialise the SDK to start Koin
        context.initializeMobileSDK(
            Environment.SANDBOX
        )

        dispatchersProvider = inject<DispatchersProvider>().value
        captureWalletChargeUseCase = mockk()
        declineWalletChargeUseCase = mockk()
        getWalletCallbackUseCase = mockk()
        viewModel = AfterpayViewModel(
            captureWalletChargeUseCase,
            declineWalletChargeUseCase,
            getWalletCallbackUseCase,
            dispatchersProvider
        )
        val mockToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        viewModel.setWalletToken(mockToken)
    }

    @After
    fun resetMocks() {
        MobileSDK.reset() // Reset MobileSDK before each test
    }

    @After
    fun tearDownKoin() {
        // As the SDK will startKoin, we need to ensure that after each test we stop koin to be able to restart it in each test
        stopKoin()
    }

    @Test
    fun `updateCancellationState should update error state`() = runTest {
        val status = CancellationStatus.USER_INITIATED
        // CHECK
        viewModel.uiState.test {
            // ACTION
            viewModel.updateCancellationState(status)
            // CHECK
            // Initial state
            assertIs<AfterpayUIState.Idle>(awaitItem())
            // Result state - failure
            awaitItem().let { state ->
                assertIs<AfterpayUIState.Error>(state)
                assertEquals(
                    MobileSDKConstants.Afterpay.USER_INITIATED_ERROR_MESSAGE,
                    state.exception.message
                )
            }
        }
    }

    @Test
    fun `configureAfterpaySdk should initialise AfterpaySDK`() = runTest {
        val configuration = AfterpaySDKConfig(
            config = AfterpaySDKConfig.AfterpayConfiguration(
                maximumAmount = "100",
                currency = "AUD",
                language = "en",
                country = "AU"
            )
        )
        viewModel.uiState.test {
            // ACTION
            viewModel.configureAfterpaySdk(configuration.config)
            // CHECK
            // Initial state
            assertIs<AfterpayUIState.Idle>(awaitItem())
            // No additional state changes or errors thrown
        }
    }

    @Test
    fun `configureAfterpaySdk should throw exception using invalid Locale`() = runTest {
        val mockError = MobileSDKTestConstants.Errors.MOCK_AFTER_PAY_LOCALE_ERROR
        val configuration = AfterpaySDKConfig(
            config = AfterpaySDKConfig.AfterpayConfiguration(
                maximumAmount = "100",
                currency = "AUD",
                language = Locale.CHINA.language,
                country = Locale.CHINA.country
            )
        )
        viewModel.uiState.test {
            // ACTION
            viewModel.configureAfterpaySdk(configuration.config)
            // CHECK
            // Initial state
            assertIs<AfterpayUIState.Idle>(awaitItem())
            // Result state - failure
            awaitItem().let { state ->
                assertIs<AfterpayUIState.Error>(state)
                assertEquals(mockError, state.exception.message)
            }
        }
    }

    @Test
    fun `get Afterpay wallet callback should update isLoading, call useCase, and update initiate ProvideCheckoutTokenResult`() =
        runTest {
            val mockCheckoutToken = MobileSDKTestConstants.Afterpay.MOCK_CHECKOUT_TOKEN
            val response =
                readResourceFile("charges/success_afterpay_wallet_callback_response.json").convertToDataClass<WalletCallbackResponse>()
            val mockResult = Result.success(response.asEntity())
            coEvery { getWalletCallbackUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.uiState.test {
                // ACTION
                viewModel.loadCheckoutToken()
                // CHECK
                // Initial state
                assertIs<AfterpayUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<AfterpayUIState.Loading>(awaitItem())
                coVerify { getWalletCallbackUseCase(any(), any()) }
                // Result state - success
                awaitItem().let { state ->
                    assertIs<AfterpayUIState.ProvideCheckoutTokenResult>(state)
                    assertNotNull(state.tokenResult.getOrNull())
                    assertEquals(mockCheckoutToken, state.tokenResult.getOrNull())
                }
            }
        }

    @Test
    fun `get Afterpay wallet callback should initiate ProvideCheckoutTokenResult command with failure result`() =
        runTest {
            val mockCheckoutToken = null
            val mockExceptionMessage = MobileSDKConstants.Errors.AFTER_PAY_CALLBACK_ERROR
            val mockResult = Result.success(
                WalletCallback(
                    callbackId = null,
                    status = "wallet_initialized",
                    callbackUrl = null,
                    refToken = mockCheckoutToken
                )
            )
            coEvery { getWalletCallbackUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.uiState.test {
                // ACTION
                viewModel.loadCheckoutToken()
                // CHECK
                // Initial state
                assertIs<AfterpayUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<AfterpayUIState.Loading>(awaitItem())
                coVerify { getWalletCallbackUseCase(any(), any()) }
                // Result state - failure
                awaitItem().let { state ->
                    assertIs<AfterpayUIState.Error>(state)
                    assertEquals(mockExceptionMessage, state.exception.message)
                }
            }
        }

    @Test
    fun `get Afterpay wallet callback should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val mockError = ApiException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.InternalServerError.value,
                    summary = ErrorSummary(
                        code = "unexpected_error",
                        message = MobileSDKTestConstants.Errors.MOCK_GENERAL_ERROR
                    )
                )
            )
            val mockResult = Result.failure<WalletCallback>(mockError)
            coEvery { getWalletCallbackUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.uiState.test {
                // ACTION
                viewModel.loadCheckoutToken()
                // CHECK
                // Initial state
                assertIs<AfterpayUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<AfterpayUIState.Loading>(awaitItem())
                coVerify { getWalletCallbackUseCase(any(), any()) }
                // Result state - failure
                awaitItem().let { state ->
                    assertIs<AfterpayUIState.Error>(state)
                    assertEquals(MobileSDKTestConstants.Errors.MOCK_GENERAL_ERROR, state.exception.message)
                }
            }
        }

    @Test
    fun `provideShippingOptions should initiate ProvideShippingOptionsResult to UI`() =
        runTest {
            val shippingOptions = listOf(
                AfterpayShippingOption(
                    "standard",
                    "Standard",
                    "",
                    Currency.getInstance("AUD"),
                    "0.00".toBigDecimal(),
                    "50.00".toBigDecimal(),
                    "0.00".toBigDecimal(),
                ),
                AfterpayShippingOption(
                    "priority",
                    "Priority",
                    "Next business day",
                    Currency.getInstance("AUD"),
                    "10.00".toBigDecimal(),
                    "60.00".toBigDecimal(),
                    null,
                )
            )
            // Allows for testing flow state
            viewModel.uiState.test {
                // ACTION
                viewModel.provideShippingOptions(shippingOptions)
                // CHECK
                // Initial state
                assertIs<AfterpayUIState.Idle>(awaitItem())
                // Result state
                awaitItem().let { item ->
                    assertIs<AfterpayUIState.ProvideShippingOptionsResult>(item)
                    assertNotNull(item.shippingOptionsResult)
                }
            }
        }

    @Test
    fun `provideShippingOptionUpdate should initiate ProvideShippingOptionUpdateResult to UI`() =
        runTest {
            val shippingUpdate = AfterpayShippingOptionUpdate(
                "standard",
                Currency.getInstance("AUD"),
                "0.00".toBigDecimal(),
                "50.00".toBigDecimal(),
                "2.00".toBigDecimal(),
            )
            // Allows for testing flow state
            viewModel.uiState.test {
                // ACTION
                viewModel.provideShippingOptionUpdate(shippingUpdate)
                // CHECK
                // Initial state
                assertIs<AfterpayUIState.Idle>(awaitItem())
                awaitItem().let { item ->
                    assertIs<AfterpayUIState.ProvideShippingOptionUpdateResult>(item)
                    assertNotNull(item.shippingOptionUpdateResult)
                }
            }
        }

    @Test
    fun `capture Afterpay wallet charge should update isLoading, call useCase, and update state on success`() =
        runTest {
            val response =
                readResourceFile("charges/success_capture_wallet_response.json").convertToDataClass<CaptureChargeResponse>()
            val mockResult = Result.success(response.asEntity())
            coEvery { captureWalletChargeUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.uiState.test {
                // ACTION
                viewModel.captureWalletTransaction()
                // CHECK
                // Initial state
                assertIs<AfterpayUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<AfterpayUIState.Loading>(awaitItem())
                coVerify { captureWalletChargeUseCase(any(), any()) }
                // Resul state - success
                awaitItem().let { state ->
                    assertIs<AfterpayUIState.Success>(state)
                    assertEquals(mockResult.getOrNull(), state.chargeData)
                    assertEquals("complete", state.chargeData.resource.data?.status)
                }
            }
        }

    @Test
    fun `capture Afterpay wallet charge should update isLoading, call useCase, and update state on failure`() =
        runTest {
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
                viewModel.captureWalletTransaction()
                // CHECK
                // Initial state
                assertIs<AfterpayUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<AfterpayUIState.Loading>(awaitItem())
                coVerify { captureWalletChargeUseCase(any(), any()) }
                // Resul state - failure
                awaitItem().let { state ->
                    assertIs<AfterpayUIState.Error>(state)
                    assertIs<AfterpayException.CapturingChargeException>(state.exception)
                    assertEquals(MobileSDKTestConstants.Errors.MOCK_GENERAL_ERROR, state.exception.message)
                }
            }
        }

    @Test
    fun `decline Afterpay wallet charge should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val response =
                readResourceFile("charges/success_afterpay_decline_wallet_charge_response.json").convertToDataClass<ChargeDeclineResponse>()
            val mockResult = Result.success(response.asEntity())
            coEvery { declineWalletChargeUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.uiState.test {
                // ACTION
                viewModel.declineWalletTransaction()
                // CHECK
                // Initial state
                assertIs<AfterpayUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<AfterpayUIState.Loading>(awaitItem())
                coVerify { declineWalletChargeUseCase(any(), any()) }
                // Result state - success
                awaitItem().let { state ->
                    assertIs<AfterpayUIState.Success>(state)
                    assertEquals(mockResult.getOrNull(), state.chargeData)
                    assertEquals("failed", state.chargeData.resource.data?.status)
                }
            }
        }

    @Test
    fun `resetResultState should reset UI state`() = runTest {
        val response =
            readResourceFile("charges/success_capture_wallet_response.json").convertToDataClass<CaptureChargeResponse>()
        val mockResult = Result.success(response.asEntity())
        coEvery { captureWalletChargeUseCase(any(), any()) } returns mockResult
        // Allows for testing flow state
        viewModel.uiState.test {
            // ACTION
            viewModel.captureWalletTransaction()
            // CHECK
            // Initial state
            assertIs<AfterpayUIState.Idle>(awaitItem())
            // Loading state - before execution
            assertIs<AfterpayUIState.Loading>(awaitItem())
            assertIs<AfterpayUIState.Success>(awaitItem())
            // ACTION
            viewModel.resetResultState()
            assertIs<AfterpayUIState.Idle>(awaitItem())
        }
    }
}
