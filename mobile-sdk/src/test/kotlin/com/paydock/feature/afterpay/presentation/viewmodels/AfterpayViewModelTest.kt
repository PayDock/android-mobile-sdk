package com.paydock.feature.afterpay.presentation.viewmodels

import android.content.Context
import app.cash.turbine.test
import com.afterpay.android.CancellationStatus
import com.paydock.MobileSDK
import com.paydock.api.charges.data.dto.CaptureChargeResponse
import com.paydock.api.charges.data.dto.ChargeDeclineResponse
import com.paydock.api.charges.data.dto.WalletCallbackResponse
import com.paydock.api.charges.data.mapper.asEntity
import com.paydock.api.charges.domain.model.WalletCallback
import com.paydock.api.charges.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.api.charges.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.api.charges.domain.usecase.GetWalletCallbackUseCase
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
import com.paydock.feature.charge.domain.model.integration.ChargeResponse
import com.paydock.initializeMobileSDK
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
    fun `setWalletToken should update token`() = runTest {
        val walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        // ACTION
        viewModel.setWalletToken(walletToken)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertEquals(walletToken, state.token)
    }

    @Test
    fun `updateCancellationState should update error state`() = runTest {
        val status = CancellationStatus.USER_INITIATED
        // ACTION
        viewModel.updateCancellationState(status)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertNotNull(state.error)
        assertEquals(
            MobileSDKConstants.Afterpay.USER_INITIATED_ERROR_MESSAGE,
            state.error?.message
        )
    }

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
                assertFalse(state.validLocale)
                assertNull(state.error)
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
        viewModel.stateFlow.test {
            // ACTION
            viewModel.configureAfterpaySdk(configuration.config)
            // CHECK
            // Initial state
            assertFalse(awaitItem().validLocale)
            // Result state - success
            assertTrue(awaitItem().validLocale)
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
        viewModel.stateFlow.test {
            // ACTION
            viewModel.configureAfterpaySdk(configuration.config)
            // CHECK
            // Initial state
            assertFalse(awaitItem().validLocale)
            // Result state - failure
            awaitItem().let { state ->
                assertNotNull(state.error)
                assertEquals(mockError, state.error?.message)
            }
        }
    }

    @Test
    fun `get Afterpay wallet callback should update isLoading, call useCase, and update state on success`() =
        runTest {
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val mockCheckoutToken = MobileSDKTestConstants.Afterpay.MOCK_CHECKOUT_TOKEN
            val response =
                readResourceFile("charges/success_afterpay_wallet_callback_response.json").convertToDataClass<WalletCallbackResponse>()
            val mockResult = Result.success(response.asEntity())
            coEvery { getWalletCallbackUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.setWalletToken(accessToken)
                viewModel.loadCheckoutToken()
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
                    assertNotNull(state.callbackData?.refToken)
                    assertEquals(mockCheckoutToken, state.callbackData?.refToken)
                    assertNull(state.error)
                }
            }
        }

    @Test
    fun `get Afterpay wallet callback should initiate ProvideCheckoutTokenResult command with success result`() =
        runTest {
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val mockCheckoutToken = MobileSDKTestConstants.Afterpay.MOCK_INVALID_CHECKOUT_TOKEN
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
            viewModel.commands().test {
                // ACTION
                viewModel.setWalletToken(accessToken)
                viewModel.getWalletCallback(walletToken = accessToken)
                // CHECK
                // 4.
                // Initial state
                awaitItem().let { item ->
                    assertIs<AfterpayViewModel.Command.ProvideCheckoutTokenResult>(item)
                    assertTrue(item.tokenResult.isSuccess)
                    assertNotNull(item.tokenResult.getOrNull())
                    assertEquals(mockCheckoutToken, item.tokenResult.getOrNull())
                }
            }
        }

    @Test
    fun `get Afterpay wallet callback should initiate ProvideCheckoutTokenResult command with failure result`() =
        runTest {
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val mockCheckoutToken = null
            val mockExceptionMessage = MobileSDKTestConstants.Errors.MOCK_AFTER_PAY_TOKEN_ERROR
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
            viewModel.commands().test {
                // ACTION
                viewModel.setWalletToken(accessToken)
                viewModel.loadCheckoutToken()
                // CHECK
                // 4.
                // Initial state
                awaitItem().let { item ->
                    assertIs<AfterpayViewModel.Command.ProvideCheckoutTokenResult>(item)
                    assertFalse(item.tokenResult.isSuccess)
                    assertNull(item.tokenResult.getOrNull())
                    assertEquals(mockCheckoutToken, item.tokenResult.getOrNull())
                    assertEquals(mockExceptionMessage, item.tokenResult.exceptionOrNull()?.message)
                }
            }
        }

    @Test
    fun `get Afterpay wallet callback should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
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
            viewModel.stateFlow.test {
                // ACTION
                viewModel.setWalletToken(accessToken)
                viewModel.loadCheckoutToken()
                // CHECK
                // 4.
                // Initial state
                assertFalse(awaitItem().isLoading)
                // wallet token is added to state
                assertNotNull(awaitItem().token)
                // Loading state - before execution
                assertTrue(awaitItem().isLoading)
                coVerify { getWalletCallbackUseCase(any(), any()) }
                // Result state - failure
                awaitItem().let { state ->
                    assertFalse(state.isLoading)
                    assertNull(state.callbackData)
                    assertNotNull(state.error)
                    assertIs<AfterpayException.FetchingUrlException>(state.error)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_GENERAL_ERROR,
                        state.error.message
                    )
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
            viewModel.commands().test {
                // ACTION
                viewModel.provideShippingOptions(shippingOptions)
                // CHECK
                // 4.
                // Initial state
                awaitItem().let { item ->
                    assertIs<AfterpayViewModel.Command.ProvideShippingOptionsResult>(item)
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
            viewModel.commands().test {
                // ACTION
                viewModel.provideShippingOptionUpdate(shippingUpdate)
                // CHECK
                // 4.
                // Initial state
                awaitItem().let { item ->
                    assertIs<AfterpayViewModel.Command.ProvideShippingOptionUpdateResult>(item)
                    assertNotNull(item.shippingOptionUpdateResult)
                }
            }
        }

    @Test
    fun `capture Afterpay wallet charge should update isLoading, call useCase, and update state on success`() =
        runTest {
            val walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val afterPayToken = MobileSDKTestConstants.Afterpay.MOCK_CHECKOUT_TOKEN
            val response =
                readResourceFile("charges/success_capture_wallet_response.json").convertToDataClass<CaptureChargeResponse>()
            val mockResult = Result.success(response.asEntity())
            coEvery { captureWalletChargeUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.captureWalletTransaction(walletToken, afterPayToken)
                // CHECK
                // 4.
                // Initial state
                assertFalse(awaitItem().isLoading)
                // Loading state - before execution
                assertTrue(awaitItem().isLoading)
                coVerify { captureWalletChargeUseCase(any(), any()) }
                // Resul state - success
                awaitItem().let { state ->
                    assertFalse(state.isLoading)
                    assertEquals(mockResult.getOrNull(), state.chargeData)
                    assertNull(state.error)
                }
            }
        }

    @Test
    fun `capture Afterpay wallet charge should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val invalidWalletToken = MobileSDKTestConstants.Wallet.MOCK_INVALID_WALLET_TOKEN
            val afterPayToken = MobileSDKTestConstants.Afterpay.MOCK_CHECKOUT_TOKEN
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
            viewModel.stateFlow.test {
                // ACTION
                viewModel.captureWalletTransaction(invalidWalletToken, afterPayToken)
                // CHECK
                // 4.
                // Initial state
                assertFalse(awaitItem().isLoading)
                // Loading state - before execution
                assertTrue(awaitItem().isLoading)
                coVerify { captureWalletChargeUseCase(any(), any()) }
                // Resul state - failure
                awaitItem().let { state ->
                    assertFalse(state.isLoading)
                    assertNull(state.chargeData)
                    assertNotNull(state.error)
                    assertIs<AfterpayException.CapturingChargeException>(state.error)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_GENERAL_ERROR,
                        state.error.message
                    )
                }
            }
        }

    @Test
    fun `decline Afterpay wallet charge should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val walletToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val chargeId = MobileSDKTestConstants.Charge.MOCK_CHARGE_ID
            val response =
                readResourceFile("charges/success_afterpay_decline_wallet_charge_response.json").convertToDataClass<ChargeDeclineResponse>()
            val mockResult = Result.success(response.asEntity())
            coEvery { declineWalletChargeUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.declineWalletTransaction(walletToken, chargeId)
                // CHECK
                // 4.
                // Initial state
                assertFalse(awaitItem().isLoading)
                // Loading state - before execution
                assertTrue(awaitItem().isLoading)
                coVerify { declineWalletChargeUseCase(any(), any()) }
                // Resul state - success
                awaitItem().let { state ->
                    assertFalse(state.isLoading)
                    assertEquals(mockResult.getOrNull(), state.chargeData)
                    assertNull(state.error)
                }
            }
        }

}
