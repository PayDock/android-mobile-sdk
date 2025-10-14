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
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.colespay.integration.ColesPayWidgetConfig
import com.paydock.feature.colespay.presentation.state.ColesPayUIState
import com.paydock.feature.wallet.data.dto.WalletCallbackResponse
import com.paydock.feature.wallet.data.mapper.asEntity
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Before
    fun setup() {
        captureWalletChargeUseCase = mockk()
        declineWalletChargeUseCase = mockk()
        getWalletCallbackUseCase = mockk()

        viewModel = ColesPayViewModel(
            ColesPayWidgetConfig(MobileSDKTestConstants.ColesPay.MOCK_CLIENT_ID),
            SavedStateHandle(),
            captureWalletChargeUseCase,
            declineWalletChargeUseCase,
            getWalletCallbackUseCase,
            dispatchersProvider
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

}
