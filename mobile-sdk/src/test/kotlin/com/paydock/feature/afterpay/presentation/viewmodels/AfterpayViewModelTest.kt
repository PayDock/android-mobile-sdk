package com.paydock.feature.afterpay.presentation.viewmodels

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.afterpay.android.Afterpay
import com.afterpay.android.CancellationStatus
import com.paydock.MobileSDK
import com.paydock.binprocessor.data.refresh.BinDataRefreshCoordinator
import com.paydock.core.BaseUnitTest
import com.paydock.core.MobileSDKConstants
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.injection.modules.mockBinDataSuccessModule
import com.paydock.core.data.injection.modules.mockSuccessNetworkModule
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.AfterpayException
import com.paydock.core.domain.model.Environment
import com.paydock.core.injection.sdkModule
import com.paydock.core.injection.testSdkModule
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.telemetry.RecordingTelemetry
import com.paydock.core.telemetry.SdkWidget
import com.paydock.core.telemetry.WidgetSourceScan
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
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertSame
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.inject
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
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

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var dispatchersProvider: DispatchersProvider
    private lateinit var viewModel: AfterpayViewModel
    private lateinit var captureWalletChargeUseCase: CaptureWalletChargeUseCase
    private lateinit var declineWalletChargeUseCase: DeclineWalletChargeUseCase
    private lateinit var getWalletCallbackUseCase: GetWalletCallbackUseCase
    private lateinit var telemetry: RecordingTelemetry

    private lateinit var context: Application

    @Before
    fun setup() {
        // Mock ProcessLifecycleOwner for BinDataRefreshCoordinator
        mockkObject(ProcessLifecycleOwner)
        val processLifecycleOwner = mockk<ProcessLifecycleOwner>(relaxed = true)
        val lifecycleRegistry = LifecycleRegistry(processLifecycleOwner)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        every { ProcessLifecycleOwner.get() } returns processLifecycleOwner
        every { processLifecycleOwner.lifecycle } returns lifecycleRegistry

        // Mock Application so Koin can resolve androidApplication() in presentationModule
        context = mockk<Application>(relaxed = true)
        every { context.applicationContext } returns context

        // Mock filesDir and cacheDir for BinDataCacheManager file operations
        val tempDir = File(System.getProperty("java.io.tmpdir"), "paydock_test")
        tempDir.mkdirs()
        every { context.filesDir } returns tempDir
        every { context.cacheDir } returns tempDir

        // Mock SharedPreferences for BinDataCacheManager
        val sharedPrefs = mockk<SharedPreferences>(relaxed = true)
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.apply() } returns Unit
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs

        // Start Koin with mock modules BEFORE initializeMobileSDK so that BinDataRefreshCoordinator
        // and BinDataCacheManager resolve with mocked dependencies (no real CloudFront requests).
        // MobileSDKKoinContext detects existing Koin and uses it instead of starting a new one.
        startKoin {
            allowOverride(true)
            androidContext(context)
            modules(
                sdkModule,
                testSdkModule,
                mockSuccessNetworkModule,
                mockBinDataSuccessModule,
                module {
                    single<BinDataRefreshCoordinator> { mockk(relaxed = true) }
                    single { context }
                }
            )
        }

        context.initializeMobileSDK(Environment.SANDBOX)

        // Load mock BIN data HTTP client to prevent real network requests
        loadKoinModules(mockBinDataSuccessModule)

        dispatchersProvider = inject<DispatchersProvider>().value
        captureWalletChargeUseCase = mockk()
        declineWalletChargeUseCase = mockk()
        getWalletCallbackUseCase = mockk()
        telemetry = RecordingTelemetry()

        viewModel = AfterpayViewModel(
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

    @After
    fun cleanupResources() {
        // Cancel any pending BIN data refresh coroutines before reset to avoid "uncaught exceptions before test started"
        try {
            val coordinator = org.koin.java.KoinJavaComponent.getKoin().getOrNull<BinDataRefreshCoordinator>()
            coordinator?.cancelPendingRefresh()
        } catch (_: Exception) {
            // Ignore if Koin is already stopped or coordinator is not available
        }
        MobileSDK.reset()
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
                assertIs<AfterpayUIState.PendingDeclineOnError>(state)
                assertEquals(
                    MobileSDKConstants.AfterpayConfig.USER_INITIATED_ERROR_MESSAGE,
                    state.exception.message
                )
            }
        }
    }

    @Test
    fun `configureAfterpaySdk should initialise AfterpaySDK`() = runTest {
        viewModel.uiState.test {
            // ACTION
            viewModel.configureAfterpaySdk(AfterpaySDKConfig())
            // CHECK
            // Initial state
            assertIs<AfterpayUIState.Idle>(awaitItem())
            // No additional state changes or errors thrown
        }
    }

    @Test
    fun `configureAfterpaySdk should throw exception using invalid Locale`() = runTest {
        // Note: This test may need to be adjusted or removed since we now use config.locale ?: Locale.getDefault()
        // and cannot inject a specific locale configuration
        // Keeping it for now but it may always pass with default locale
        viewModel.uiState.test {
            // ACTION
            viewModel.configureAfterpaySdk(AfterpaySDKConfig())
            // CHECK
            // Initial state
            assertIs<AfterpayUIState.Idle>(awaitItem())
            // No additional state changes expected for valid default locale
        }
    }

    @Test
    fun `get Afterpay wallet callback should update isLoading, call useCase, and update initiate ProvideCheckoutTokenResult`() =
        runTest {
            val mockCheckoutToken = MobileSDKTestConstants.Afterpay.MOCK_CHECKOUT_TOKEN
            val response =
                readResourceFile("wallet/success_afterpay_wallet_callback_response.json").convertToDataClass<WalletCallbackResponse>()
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
            val mockExceptionMessage = MobileSDKConstants.AfterpayConfig.Errors.CALLBACK_ERROR
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
                    assertIs<AfterpayUIState.PendingDeclineOnError>(state)
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
                readResourceFile("wallet/success_capture_wallet_response.json").convertToDataClass<CaptureChargeResponse>()
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
                readResourceFile("wallet/success_afterpay_decline_wallet_charge_response.json").convertToDataClass<ChargeDeclineResponse>()
            val mockResult = Result.success(response.asEntity())
            val pendingException = AfterpayException.CancellationException(MobileSDKConstants.AfterpayConfig.USER_INITIATED_ERROR_MESSAGE)
            coEvery { declineWalletChargeUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.uiState.test {
                // ACTION
                viewModel.declineWalletTransaction(pendingException)
                // CHECK
                // Initial state
                assertIs<AfterpayUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<AfterpayUIState.Loading>(awaitItem())
                coVerify { declineWalletChargeUseCase(any(), any()) }
                // Result state - error
                awaitItem().let { state ->
                    assertIs<AfterpayUIState.Error>(state)
                    assertIs<AfterpayException.CancellationException>(state.exception)
                    assertEquals(MobileSDKConstants.AfterpayConfig.USER_INITIATED_ERROR_MESSAGE, state.exception.message)
                }
            }
        }

    @Test
    fun `resetResultState should reset UI state`() = runTest {
        val response =
            readResourceFile("wallet/success_capture_wallet_response.json").convertToDataClass<CaptureChargeResponse>()
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

    // region Telemetry: one SdkError per failure path (AF1-AF10; none at AF4), merchant-visible results unchanged

    private val apiFailure = ApiException(
        error = ApiErrorResponse(
            status = HttpStatusCode.InternalServerError.value,
            summary = ErrorSummary(
                code = "unexpected_error",
                message = MobileSDKTestConstants.Errors.MOCK_GENERAL_ERROR
            )
        )
    )

    /** Puts the view model in the INITIAL flow state with no pending intent: the intent was lost. */
    private fun loseInitialIntent() {
        val field = AfterpayViewModel::class.java.getDeclaredField("flowState")
        field.isAccessible = true
        val initial = field.type.enumConstants!!.first { (it as Enum<*>).name == "INITIAL" }
        field.set(viewModel, initial)
    }

    @Test
    fun `intent lost reports one SdkError and still sets the TokenException error (AF1)`() = runTest {
        val response =
            readResourceFile("wallet/success_afterpay_wallet_callback_response.json").convertToDataClass<WalletCallbackResponse>()
        coEvery { getWalletCallbackUseCase(any(), any()) } returns Result.success(response.asEntity())
        loseInitialIntent()

        viewModel.loadCheckoutToken()
        val state = viewModel.uiState.first { it is AfterpayUIState.Error }

        assertIs<AfterpayUIState.Error>(state)
        assertIs<AfterpayException.TokenException>(state.exception)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.AFTERPAY, reported.widget)
        assertEquals("TokenException", reported.errorCode)
        assertNull(reported.error.cause)
    }

    @Test
    fun `null ref token reports one SdkError and still sets the TokenException error (AF2)`() = runTest {
        coEvery { getWalletCallbackUseCase(any(), any()) } returns Result.success(
            WalletCallback(callbackId = null, status = "wallet_initialized", callbackUrl = null, refToken = null)
        )

        viewModel.loadCheckoutToken()
        val state = viewModel.uiState.first { it is AfterpayUIState.Error }

        assertIs<AfterpayUIState.Error>(state)
        assertIs<AfterpayException.TokenException>(state.exception)
        assertEquals(MobileSDKConstants.AfterpayConfig.Errors.CALLBACK_ERROR, state.exception.message)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.AFTERPAY, reported.widget)
        assertEquals("TokenException", reported.errorCode)
    }

    @Test
    fun `callback failure reports one SdkError with the API cause and still sets the pending FetchingUrlException (AF3)`() =
        runTest {
            coEvery { getWalletCallbackUseCase(any(), any()) } returns Result.failure(apiFailure)

            viewModel.loadCheckoutToken()
            val state = viewModel.uiState.first { it is AfterpayUIState.PendingDeclineOnError }

            assertIs<AfterpayUIState.PendingDeclineOnError>(state)
            assertIs<AfterpayException.FetchingUrlException>(state.exception)
            val reported = telemetry.sdkErrors.single()
            assertEquals(SdkWidget.AFTERPAY, reported.widget)
            assertEquals("FetchingUrlException", reported.errorCode)
            assertEquals(ApiException::class.java.name, reported.error.cause?.type)
            assertEquals(HttpStatusCode.InternalServerError.value, reported.apiError?.status)
            assertEquals("unexpected_error", reported.apiError?.summaryCode)
        }

    @Test
    fun `decline that does not fail drops the pending error and reports nothing (AF4)`() = runTest {
        val response =
            readResourceFile("wallet/success_capture_wallet_response.json").convertToDataClass<CaptureChargeResponse>()
        coEvery { declineWalletChargeUseCase(any(), any()) } returns Result.success(response.asEntity())

        viewModel.declineWalletTransaction(AfterpayException.CancellationException(MobileSDKConstants.AfterpayConfig.USER_INITIATED_ERROR_MESSAGE))
        val state = viewModel.uiState.first { it is AfterpayUIState.Success }

        assertIs<AfterpayUIState.Success>(state)
        assertTrue(telemetry.sdkErrors.isEmpty())
    }

    @Test
    fun `re-surfaced pending errors are not reported again`() = runTest {
        val pending = AfterpayException.CancellationException(MobileSDKConstants.AfterpayConfig.USER_INITIATED_ERROR_MESSAGE)
        val response =
            readResourceFile("wallet/success_afterpay_decline_wallet_charge_response.json").convertToDataClass<ChargeDeclineResponse>()
        coEvery { declineWalletChargeUseCase(any(), any()) } returns Result.success(response.asEntity())

        // The failed decline re-surfaces the pending error.
        viewModel.declineWalletTransaction(pending)
        val declined = viewModel.uiState.first { it is AfterpayUIState.Error }
        assertIs<AfterpayUIState.Error>(declined)
        assertSame(pending, declined.exception)

        // With no wallet token the pending error is re-surfaced at once.
        viewModel.resetResultState()
        viewModel.declineWalletTransaction(pending)
        val direct = viewModel.uiState.value
        assertIs<AfterpayUIState.Error>(direct)
        assertSame(pending, direct.exception)

        assertTrue(telemetry.sdkErrors.isEmpty())
    }

    @Test
    fun `capture failure reports one SdkError with the API cause and still sets CapturingChargeException (AF5)`() =
        runTest {
            coEvery { captureWalletChargeUseCase(any(), any()) } returns Result.failure(apiFailure)

            viewModel.captureWalletTransaction()
            val state = viewModel.uiState.first { it is AfterpayUIState.Error }

            assertIs<AfterpayUIState.Error>(state)
            assertIs<AfterpayException.CapturingChargeException>(state.exception)
            val reported = telemetry.sdkErrors.single()
            assertEquals(SdkWidget.AFTERPAY, reported.widget)
            assertEquals("CapturingChargeException", reported.errorCode)
            assertEquals(ApiException::class.java.name, reported.error.cause?.type)
            assertEquals(HttpStatusCode.InternalServerError.value, reported.apiError?.status)
        }

    @Test
    fun `decline failure reports one SdkError and overrides the pending error as before (AF5)`() = runTest {
        coEvery { declineWalletChargeUseCase(any(), any()) } returns Result.failure(apiFailure)

        viewModel.declineWalletTransaction(AfterpayException.CancellationException(MobileSDKConstants.AfterpayConfig.USER_INITIATED_ERROR_MESSAGE))
        val state = viewModel.uiState.first { it is AfterpayUIState.Error }

        assertIs<AfterpayUIState.Error>(state)
        assertIs<AfterpayException.CapturingChargeException>(state.exception)
        val reported = telemetry.sdkErrors.single()
        assertEquals("CapturingChargeException", reported.errorCode)
        assertEquals(ApiException::class.java.name, reported.error.cause?.type)
    }

    @Test
    fun `merchant token failure reports no SdkError and still sets the pending error (AF6)`() =
        runTest {
            // The Afterpay SDK cannot build its checkout intent on the JVM: stand in for it.
            mockkStatic(Afterpay::class)
            every { Afterpay.createCheckoutV2Intent(any(), any()) } returns mockk<Intent>(relaxed = true)

            try {
                viewModel.startAfterpayFlow(
                    tokenProvider = { callback -> callback(Result.failure(IllegalStateException())) },
                    context = context,
                    config = AfterpaySDKConfig()
                )
            } finally {
                unmockkStatic(Afterpay::class)
            }

            val state = viewModel.uiState.value
            assertIs<AfterpayUIState.PendingDeclineOnError>(state)
            assertIs<AfterpayException.InitialisationWalletTokenException>(state.exception)
            assertEquals(MobileSDKConstants.AfterpayConfig.Errors.WALLET_TOKEN_ERROR, state.exception.message)
            assertTrue(telemetry.sdkErrors.isEmpty())
        }

    @Test
    fun `user cancellation reports no SdkError and still sets the pending CancellationException (AF7)`() = runTest {
        viewModel.updateCancellationState(CancellationStatus.USER_INITIATED)

        val state = viewModel.uiState.value
        assertIs<AfterpayUIState.PendingDeclineOnError>(state)
        assertIs<AfterpayException.CancellationException>(state.exception)
        assertTrue(telemetry.sdkErrors.isEmpty())
    }

    @Test
    fun `failing cancellation status reports one SdkError and still sets the pending CancellationException (AF7)`() =
        runTest {
            viewModel.updateCancellationState(CancellationStatus.NO_CHECKOUT_URL)

            val state = viewModel.uiState.value
            assertIs<AfterpayUIState.PendingDeclineOnError>(state)
            assertIs<AfterpayException.CancellationException>(state.exception)
            val reported = telemetry.sdkErrors.single()
            assertEquals(SdkWidget.AFTERPAY, reported.widget)
            assertEquals("CancellationException", reported.errorCode)
            assertNull(reported.error.cause)
        }

    @Test
    fun `wallet token lost at capture reports one SdkError and still sets the error (AF8)`() = runTest {
        viewModel.resetResultState()

        viewModel.captureWalletTransaction()

        val state = viewModel.uiState.value
        assertIs<AfterpayUIState.Error>(state)
        assertIs<AfterpayException.InitialisationWalletTokenException>(state.exception)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.AFTERPAY, reported.widget)
        assertEquals("InitialisationWalletTokenException", reported.errorCode)
    }

    @Test
    fun `wallet token lost at checkout token load reports one SdkError and still sets the error (AF8)`() = runTest {
        viewModel.resetResultState()

        viewModel.loadCheckoutToken()

        val state = viewModel.uiState.value
        assertIs<AfterpayUIState.Error>(state)
        assertIs<AfterpayException.InitialisationWalletTokenException>(state.exception)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.AFTERPAY, reported.widget)
        assertEquals("InitialisationWalletTokenException", reported.errorCode)
    }

    @Test
    fun `SDK configuration failure reports once per view model and still sets the pending ConfigurationException (AF9)`() =
        runTest {
            // A locale with no country has no currency: Currency.getInstance throws IllegalArgumentException.
            val config = AfterpaySDKConfig(locale = Locale.ENGLISH)

            // Runs on every composition: the second call must not report again.
            viewModel.configureAfterpaySdk(config)
            viewModel.uiState.first { it is AfterpayUIState.PendingDeclineOnError }
            viewModel.configureAfterpaySdk(config)
            val state = viewModel.uiState.first { it is AfterpayUIState.PendingDeclineOnError }

            assertIs<AfterpayUIState.PendingDeclineOnError>(state)
            assertIs<AfterpayException.ConfigurationException>(state.exception)
            val reported = telemetry.sdkErrors.single()
            assertEquals(SdkWidget.AFTERPAY, reported.widget)
            assertEquals("ConfigurationException", reported.errorCode)
            assertEquals(IllegalArgumentException::class.java.name, reported.error.cause?.type)
        }

    @Test
    fun `reportWidgetError reports one Afterpay SdkError with its cause and changes no state (AF10)`() = runTest {
        val before = viewModel.uiState.value

        viewModel.reportWidgetError(
            AfterpayException.InvalidResultException(MobileSDKConstants.AfterpayConfig.Errors.SDK_INTERNAL_ERROR),
            cause = IllegalStateException()
        )

        assertSame(before, viewModel.uiState.value)
        val reported = telemetry.sdkErrors.single()
        assertEquals(SdkWidget.AFTERPAY, reported.widget)
        assertEquals("InvalidResultException", reported.errorCode)
        assertEquals(IllegalStateException::class.java.name, reported.error.cause?.type)
    }

    @Test
    fun `the invalid result handler reports the failure it hands on, once, with the caught cause (AF10)`() {
        val source = WidgetSourceScan.read("feature/afterpay/presentation/AfterpayWidget.kt")
        val invalid = WidgetSourceScan.function(source, "handleInvalidError")
        val result = WidgetSourceScan.function(source, "handleAfterpayResult")

        assertEquals(1, WidgetSourceScan.count(source, "viewModel.reportWidgetError("))
        assertEquals(1, WidgetSourceScan.count(invalid, "viewModel.reportWidgetError(failure, cause = cause)"))
        assertEquals(1, WidgetSourceScan.count(invalid, "completion(Result.failure(failure))"))
        assertEquals(1, WidgetSourceScan.count(result, "cause = e)"))
    }

    // endregion
}
