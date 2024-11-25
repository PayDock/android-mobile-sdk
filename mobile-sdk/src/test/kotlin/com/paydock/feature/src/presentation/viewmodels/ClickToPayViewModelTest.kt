package com.paydock.feature.src.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.src.domain.model.ui.CardData
import com.paydock.feature.src.domain.model.ui.CheckoutData
import com.paydock.feature.src.domain.model.ui.ClickToPayEvent
import com.paydock.feature.src.domain.model.ui.ErrorData
import com.paydock.feature.src.domain.model.ui.EventData
import com.paydock.feature.src.domain.model.ui.SuccessEventData
import com.paydock.feature.src.domain.model.ui.mapToException
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
internal class ClickToPayViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: ClickToPayViewModel

    @Before
    fun setup() {
        viewModel = ClickToPayViewModel(dispatchersProvider)
    }

    @Test
    fun `updateSRCEvent with iFrameLoaded event should update loading state to true`() = runTest {
        val event = ClickToPayEvent.IframeLoadedEvent(data = EventData())
        // ACTION
        viewModel.updateSRCEvent(event)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertTrue(state.isLoading)
    }

    @Test
    fun `updateSRCEvent with checkoutReady event should update loading state to false`() = runTest {
        val event = ClickToPayEvent.CheckoutReadyEvent(data = EventData())
        // ACTION
        viewModel.updateSRCEvent(event)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertFalse(state.isLoading)
    }

    @Test
    fun `updateSRCEvent with checkoutCompleted event should update token state`() = runTest {
        val event = ClickToPayEvent.CheckoutCompletedEvent(
            data = SuccessEventData(
                data = CheckoutData(
                    type = "src",
                    token = "4da9bee6-b2bc-45cb-994a-1715cf015b5a",
                    checkoutData = CardData(
                        cardNumberBin = "512035",
                        cardNumberLast4 = "4537",
                        cardScheme = "mastercard",
                        cardType = "credit"
                    )
                )
            )
        )
        // ACTION
        viewModel.updateSRCEvent(event)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertEquals(event.data.data.token, state.token)
    }

    @Test
    fun `updateSRCEvent with checkoutError userError event should update error state`() = runTest {
        val event = ClickToPayEvent.CheckoutErrorEvent(
            data = ErrorData.UserErrorData(
                data = "Request failed due to CARD_MISSING"
            )
        )
        // ACTION
        viewModel.updateSRCEvent(event)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertEquals(
            event.data.mapToException().message,
            state.error?.message
        )
    }

    @Test
    fun `updateSRCEvent with checkoutError criticalError event should update error state`() =
        runTest {
            val event = ClickToPayEvent.CheckoutErrorEvent(
                data = ErrorData.CriticalErrorData(
                    data = "403 - OK [object Object]"
                )
            )
            // ACTION
            viewModel.updateSRCEvent(event)
            // CHECK
            val state = viewModel.stateFlow.first()
            assertEquals(
                event.data.mapToException().message,
                state.error?.message
            )
        }

    @Test
    fun `resetResultState should reset UI state`() = runTest {
        val checkoutToken = MobileSDKTestConstants.ClickToPay.MOCK_CHECKOUT_TOKEN
        viewModel.stateFlow.test {
            // ACTION
            viewModel.updateSRCEvent(
                ClickToPayEvent.CheckoutCompletedEvent(
                    data = SuccessEventData(
                        data = CheckoutData(
                            token = checkoutToken,
                            type = "src",
                            checkoutData = null
                        )
                    )
                )
            )
            viewModel.resetResultState()
            // Initial state
            assertNull(awaitItem().token)
            assertEquals(checkoutToken, awaitItem().token)
            // Result state - success
            awaitItem().let { state ->
                assertFalse(state.isLoading)
                assertNull(state.token)
                assertNull(state.error)
            }
        }
    }
}
