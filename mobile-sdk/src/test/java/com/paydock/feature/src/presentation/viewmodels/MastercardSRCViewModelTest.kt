package com.paydock.feature.src.presentation.viewmodels

import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.src.presentation.model.CardData
import com.paydock.feature.src.presentation.model.CheckoutData
import com.paydock.feature.src.presentation.model.ErrorData
import com.paydock.feature.src.presentation.model.EventData
import com.paydock.feature.src.presentation.model.MastercardSRCEvent
import com.paydock.feature.src.presentation.model.SuccessEventData
import com.paydock.feature.src.presentation.model.mapToException
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
class MastercardSRCViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: MastercardSRCViewModel

    @Before
    fun setup() {
        viewModel = MastercardSRCViewModel(dispatchersProvider)
    }

    @Test
    fun `updateSRCEvent with iFrameLoaded event should update loading state to true`() = runTest {
        val event = MastercardSRCEvent.IframeLoadedEvent(data = EventData())
        // ACTION
        viewModel.updateSRCEvent(event)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertTrue(state.isLoading)
    }

    @Test
    fun `updateSRCEvent with checkoutReady event should update loading state to false`() = runTest {
        val event = MastercardSRCEvent.CheckoutReadyEvent(data = EventData())
        // ACTION
        viewModel.updateSRCEvent(event)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertFalse(state.isLoading)
    }

    @Test
    fun `updateSRCEvent with checkoutCompleted event should update token state`() = runTest {
        val event = MastercardSRCEvent.CheckoutCompletedEvent(
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
        val event = MastercardSRCEvent.CheckoutErrorEvent(
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
            val event = MastercardSRCEvent.CheckoutErrorEvent(
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
}
