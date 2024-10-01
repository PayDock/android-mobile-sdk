package com.paydock.feature.flypay.presentation.viewmodels

import android.content.Context
import app.cash.turbine.test
import com.paydock.MobileSDK
import com.paydock.core.BaseUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.FlyPayException
import com.paydock.core.domain.model.Environment
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.wallet.data.api.dto.WalletCallbackResponse
import com.paydock.feature.wallet.data.mapper.asEntity
import com.paydock.feature.wallet.domain.model.WalletCallback
import com.paydock.feature.wallet.domain.usecase.CaptureWalletTransactionUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletTransactionUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
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
import kotlin.test.assertIs

@Suppress("MaxLineLength")
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class FlyPayViewModelTest : BaseUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dispatchersProvider: DispatchersProvider
    private lateinit var viewModel: FlyPayViewModel
    private lateinit var captureWalletTransactionUseCase: CaptureWalletTransactionUseCase
    private lateinit var declineWalletTransactionUseCase: DeclineWalletTransactionUseCase
    private lateinit var getWalletCallbackUseCase: GetWalletCallbackUseCase

    private lateinit var context: Context

    @Before
    fun setup() {
        // Mock the Context object
        context = mockk()
        // Configure the getApplicationContext() method to return the mock Context
        every { context.applicationContext } returns context
        // We need to initialise the SDK to start Koin
        context.initializeMobileSDK(Environment.SANDBOX)

        dispatchersProvider = inject<DispatchersProvider>().value
        captureWalletTransactionUseCase = mockk()
        declineWalletTransactionUseCase = mockk()
        getWalletCallbackUseCase = mockk()
        viewModel = FlyPayViewModel(
            captureWalletTransactionUseCase,
            declineWalletTransactionUseCase,
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
        val walletToken = "testToken"
        // ACTION
        viewModel.setWalletToken(walletToken)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertEquals(walletToken, state.token)
    }

    @Test
    fun `resetResultState should reset UI state`() = runTest {
        val walletToken = "testToken"
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
                assertNull(state.error)
            }
        }
    }

    @Test
    fun `get FlyPay wallet callback should update isLoading, call useCase, and update state on success`() =
        runTest {
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val mockFlyPayOrderId = MobileSDKTestConstants.FlyPay.MOCK_ORDER_ID
            val response =
                readResourceFile("wallet/success_flypay_wallet_callback_response.json").convertToDataClass<WalletCallbackResponse>()
            val mockResult = Result.success(response.asEntity())
            coEvery { getWalletCallbackUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.setWalletToken(accessToken)
                viewModel.getWalletCallback(walletToken = accessToken)
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
                    assertNotNull(state.callbackData?.callbackId)
                    assertEquals(mockFlyPayOrderId, state.callbackData?.callbackId)
                    assertNull(state.error)
                }
            }
        }

    @Test
    fun `get FlyPay wallet callback should update isLoading, call useCase, and update state on failure`() =
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
            viewModel.stateFlow.test {
                // ACTION
                viewModel.setWalletToken(accessToken)
                viewModel.getWalletCallback(walletToken = accessToken)
                // CHECK
                // 4.
                // Initial state
                assertFalse(awaitItem().isLoading)
                // wallet token is added to state
                assertNotNull(awaitItem().token)
                // Loading state - before execution
                assertTrue(awaitItem().isLoading)
                coVerify { getWalletCallbackUseCase(any(), any()) }
                // Resul state - failure
                awaitItem().let { state ->
                    assertFalse(state.isLoading)
                    assertNull(state.callbackData)
                    assertNotNull(state.error)
                    assertIs<FlyPayException.FetchingUrlException>(state.error)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_INVALID_GATEWAY_ID_ERROR,
                        state.error.message
                    )
                }
            }
        }

}
