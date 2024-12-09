package com.paydock.feature.flypay.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.api.charges.data.dto.WalletCallbackResponse
import com.paydock.api.charges.data.mapper.asEntity
import com.paydock.api.charges.domain.model.WalletCallback
import com.paydock.api.charges.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.api.charges.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.api.charges.domain.usecase.GetWalletCallbackUseCase
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.FlyPayException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.flypay.presentation.state.FlyPayUIState
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
internal class FlyPayViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()
    private lateinit var viewModel: FlyPayViewModel
    private lateinit var captureWalletChargeUseCase: CaptureWalletChargeUseCase
    private lateinit var declineWalletChargeUseCase: DeclineWalletChargeUseCase
    private lateinit var getWalletCallbackUseCase: GetWalletCallbackUseCase

    @Before
    fun setup() {
        captureWalletChargeUseCase = mockk()
        declineWalletChargeUseCase = mockk()
        getWalletCallbackUseCase = mockk()
        viewModel = FlyPayViewModel(
            MobileSDKTestConstants.FlyPay.MOCK_CLIENT_ID,
            captureWalletChargeUseCase,
            declineWalletChargeUseCase,
            getWalletCallbackUseCase,
            dispatchersProvider
        )
        val mockToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
        viewModel.setWalletToken(mockToken)
    }

    @Test
    fun `get FlyPay wallet callback should update isLoading, call useCase, and update state to launch intent`() =
        runTest {
            val accessToken = MobileSDKTestConstants.Wallet.MOCK_WALLET_TOKEN
            val mockFlyPayOrderId = MobileSDKTestConstants.FlyPay.MOCK_ORDER_ID
            val response =
                readResourceFile("charges/success_flypay_wallet_callback_response.json")
                    .convertToDataClass<WalletCallbackResponse>()
            val mockResult = Result.success(response.asEntity())
            coEvery { getWalletCallbackUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.getWalletCallback(walletToken = accessToken)
                // CHECK
                // Initial state
                assertIs<FlyPayUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<FlyPayUIState.Loading>(awaitItem())
                coVerify { getWalletCallbackUseCase(any(), any()) }
                // Result state - success
                awaitItem().let { state ->
                    assertIs<FlyPayUIState.LaunchIntent>(state)
                    assertNotNull(state.callbackData.callbackId)
                    assertEquals(mockFlyPayOrderId, state.callbackData.callbackId)
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
                viewModel.getWalletCallback(walletToken = accessToken)
                // CHECK
                // Initial state
                assertIs<FlyPayUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<FlyPayUIState.Loading>(awaitItem())
                coVerify { getWalletCallbackUseCase(any(), any()) }
                // Result state - failure
                awaitItem().let { state ->
                    assertIs<FlyPayUIState.Error>(state)
                    assertIs<FlyPayException.FetchingUrlException>(state.exception)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_INVALID_GATEWAY_ID_ERROR,
                        state.exception.message
                    )
                }
            }
        }

    @Test
    fun `completeResult should update UI state to success`() = runTest {
        val mockOrderId = MobileSDKTestConstants.FlyPay.MOCK_ORDER_ID
        viewModel.stateFlow.test {
            // Initial state
            assertIs<FlyPayUIState.Idle>(awaitItem())
            // ACTION
            viewModel.completeResult(mockOrderId)
            // Result state - success
            awaitItem().let { state ->
                assertIs<FlyPayUIState.Success>(state)
                assertEquals(mockOrderId, state.orderId)
            }
        }
    }

    @Test
    fun `resetResultState should reset UI state`() = runTest {
        viewModel.completeResult(MobileSDKTestConstants.FlyPay.MOCK_ORDER_ID)
        viewModel.stateFlow.test {
            assertIs<FlyPayUIState.Success>(awaitItem())
            viewModel.resetResultState()
            // Result state - reset
            assertIs<FlyPayUIState.Idle>(awaitItem())
        }
    }

}
