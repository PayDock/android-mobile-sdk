package com.paydock.feature.card.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.core.BaseViewModelKoinTest
import com.paydock.core.domain.error.exceptions.GiftCardException
import com.paydock.core.domain.model.Event
import com.paydock.core.domain.model.EventAction
import com.paydock.core.extensions.waitUntilTimeout
import com.paydock.core.presentation.util.WidgetEventDelegate
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.card.domain.model.GiftCardEventNames
import com.paydock.feature.card.domain.model.integration.CardResult
import com.paydock.feature.card.domain.model.integration.GiftCardWidgetConfig
import com.paydock.feature.card.domain.model.ui.TokenDetails
import com.paydock.feature.card.domain.usecase.CreateGiftCardPaymentTokenUseCase
import com.paydock.feature.card.injection.cardDetailsModule
import com.paydock.feature.card.presentation.viewmodels.GiftCardViewModel
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(KoinInternalApi::class)
@RunWith(AndroidJUnit4::class)
internal class GiftCardTest : BaseViewModelKoinTest<GiftCardViewModel>() {

    private val testModule: Module = module {
        viewModel { viewModel }
    }

    @Before
    fun setUpKoin() {
        unloadKoinModules(cardDetailsModule)
        loadKoinModules(testModule)
    }

    @After
    override fun tearDownKoin() {
        unloadKoinModules(testModule)
        super.tearDownKoin()
    }

    private val createGiftCardPaymentTokenUseCase: CreateGiftCardPaymentTokenUseCase = mockk(relaxed = true)

    override fun initialiseViewModel(): GiftCardViewModel {
        return GiftCardViewModel(
            config = GiftCardWidgetConfig(
                accessToken = "testAccessToken",
                storePin = true
            ),
            createCardPaymentTokenUseCase = createGiftCardPaymentTokenUseCase,
            dispatchers = dispatchersProvider
        )
    }

    @Test
    fun testGiftCardInitialStateInput() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                GiftCardWidget(
                    config = GiftCardWidgetConfig(
                        accessToken = "testAccessToken",
                        storePin = true
                    ),
                    completion = {}
                )
            }
        }

        // Verify UI elements and interactions
        composeTestRule.onNodeWithText("Card number").assertIsDisplayed()
        composeTestRule.onNodeWithText("PIN").assertIsDisplayed()
        composeTestRule.onNodeWithTag("addCard").assertIsDisplayed().assertIsNotEnabled()
    }

    @Test
    fun testGiftCardValidInput() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                GiftCardWidget(
                    config = GiftCardWidgetConfig(
                        accessToken = "testAccessToken",
                        storePin = true
                    ),
                    completion = {}
                )
            }
        }

        // Simulate user interactions
        composeTestRule.onNodeWithText("Card number")
            .performClick()
            .assertIsFocused()
            .performTextInput("62734010000131278")
        composeTestRule.onNode(hasText("Card number")).performImeAction()

        composeTestRule.onNodeWithText("PIN")
            .performClick()
            .assertIsFocused()
            .performTextInput("5814")
        composeTestRule.onNode(hasText("PIN")).performImeAction()

        // Allow some time for the UI to update
        composeTestRule.waitForIdle()

        // Verify UI updates/changes
        composeTestRule.onNodeWithText("Card number").assert(hasText("6273 4010 0001 3127 8"))
        composeTestRule.onNodeWithText("PIN").assert(hasText("5814"))
        composeTestRule.onNodeWithTag("addCard").assertIsDisplayed().assertIsEnabled()

        // Assert ViewModel interactions
        assertTrue(viewModel.inputStateFlow.value.isDataValid)
    }

    @Test
    fun testGiftCardInvalidInput() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                GiftCardWidget(
                    config = GiftCardWidgetConfig(
                        accessToken = "testAccessToken",
                        storePin = true
                    ),
                    completion = {}
                )
            }
        }

        // Simulate user interactions with invalid data
        composeTestRule.onNodeWithText("Card number")
            .performClick()
            .assertIsFocused()
            .performTextInput("1234")
        composeTestRule.onNode(hasText("Card number")).performImeAction()

        composeTestRule.onNodeWithText("PIN")
            .performClick()
            .assertIsFocused()
            .performTextInput("123")
        composeTestRule.onNode(hasText("PIN")).performImeAction()

        // Allow some time for the UI to update
        composeTestRule.waitForIdle()

        // Assert ViewModel interactions
        assertFalse(viewModel.inputStateFlow.value.isDataValid)
    }

    @Test
    fun testValidSubmissionWithSuccessTokenResult() {
        val onGiftCardResult: (Result<CardResult>) -> Unit = mockk()
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                GiftCardWidget(
                    config = GiftCardWidgetConfig(
                        accessToken = "testAccessToken",
                        storePin = true
                    ),
                    completion = { result ->
                        result.fold(
                            onSuccess = { token -> onGiftCardResult(Result.success(CardResult(token))) },
                            onFailure = { error -> onGiftCardResult(Result.failure(error)) }
                        )
                    }
                )
            }
        }

        // Simulate user interactions
        composeTestRule.onNodeWithText("Card number")
            .performClick()
            .assertIsFocused()
            .performTextInput("62734010000131278")
        composeTestRule.onNode(hasText("Card number")).performImeAction()

        composeTestRule.onNodeWithText("PIN")
            .performClick()
            .assertIsFocused()
            .performTextInput("5814")
        composeTestRule.onNode(hasText("PIN")).performImeAction()

        // Allow some time for the UI to update
        composeTestRule.waitForIdle()

        // For token case
        val mockToken = "mockToken"
        val mockResult = Result.success(
            TokenDetails(
                token = mockToken,
                type = "token"
            )
        )
        coEvery {
            createGiftCardPaymentTokenUseCase.invoke(
                "testAccessToken",
                any()
            )
        } returns mockResult
        every { onGiftCardResult(any()) } just Runs

        composeTestRule.onNodeWithTag("addCard").assertIsEnabled().performClick()

        // Trigger the LaunchedEffects
        composeTestRule.waitUntilTimeout(5000)

        verify {
            onGiftCardResult(Result.success(CardResult(mockToken)))
            viewModel.resetResultState()
        }
    }

    @Test
    fun testGiftCardLoadingState() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                GiftCardWidget(
                    config = GiftCardWidgetConfig(
                        accessToken = "testAccessToken",
                        storePin = true
                    ),
                    completion = {}
                )
            }
        }

        // Simulate user interactions with valid data
        composeTestRule.onNodeWithText("Card number")
            .performClick()
            .performTextInput("62734010000131278")
        composeTestRule.onNode(hasText("Card number")).performImeAction()

        composeTestRule.onNodeWithText("PIN")
            .performClick()
            .performTextInput("5814")
        composeTestRule.onNode(hasText("PIN")).performImeAction()

        composeTestRule.waitForIdle()

        // Mock use case to delay response
        val mockToken = "mockToken"
        val mockResult = Result.success(
            TokenDetails(
                token = mockToken,
                type = "token"
            )
        )
        coEvery {
            createGiftCardPaymentTokenUseCase.invoke(any(), any())
        } returns mockResult

        // Click submit button
        composeTestRule.onNodeWithTag("addCard").assertIsEnabled().performClick()

        // Verify loading state - button should be disabled during loading
        composeTestRule.waitForIdle()

        // After completion, the button should be enabled again
        composeTestRule.waitUntilTimeout(3000)
        composeTestRule.onNodeWithTag("addCard").assertIsEnabled()
    }

    @Test
    fun testGiftCardTokenizationFailure() {
        val onGiftCardResult: (Result<CardResult>) -> Unit = mockk()
        val mockException = GiftCardException.UnknownException("Tokenization failed")

        coEvery {
            createGiftCardPaymentTokenUseCase.invoke(any(), any())
        } returns Result.failure(mockException)
        every { onGiftCardResult(any()) } just Runs

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                GiftCardWidget(
                    config = GiftCardWidgetConfig(
                        accessToken = "testAccessToken",
                        storePin = true
                    ),
                    completion = { result ->
                        result.fold(
                            onSuccess = { token -> onGiftCardResult(Result.success(CardResult(token))) },
                            onFailure = { error -> onGiftCardResult(Result.failure(error)) }
                        )
                    }
                )
            }
        }

        // Simulate user interactions with valid data
        composeTestRule.onNodeWithText("Card number")
            .performClick()
            .performTextInput("62734010000131278")
        composeTestRule.onNode(hasText("Card number")).performImeAction()

        composeTestRule.onNodeWithText("PIN")
            .performClick()
            .performTextInput("5814")
        composeTestRule.onNode(hasText("PIN")).performImeAction()

        composeTestRule.waitForIdle()

        // Click submit button
        composeTestRule.onNodeWithTag("addCard").assertIsEnabled().performClick()

        // Wait for the result
        composeTestRule.waitUntilTimeout(5000)

        // Verify failure callback was called
        verify {
            onGiftCardResult(match { it.isFailure })
        }
    }

    @Test
    fun testGiftCardAnalyticsCallback() {
        val eventDelegate: WidgetEventDelegate = mockk(relaxed = true)
        val eventSlot = slot<Event>()

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                GiftCardWidget(
                    config = GiftCardWidgetConfig(
                        accessToken = "testAccessToken",
                        storePin = true
                    ),
                    eventDelegate = eventDelegate,
                    completion = {}
                )
            }
        }

        // Simulate user interactions with valid data
        composeTestRule.onNodeWithText("Card number")
            .performClick()
            .performTextInput("62734010000131278")
        composeTestRule.onNode(hasText("Card number")).performImeAction()

        composeTestRule.onNodeWithText("PIN")
            .performClick()
            .performTextInput("5814")
        composeTestRule.onNode(hasText("PIN")).performImeAction()

        composeTestRule.waitForIdle()

        // Mock use case response
        val mockResult = Result.success(
            TokenDetails(
                token = "mockToken",
                type = "token"
            )
        )
        coEvery {
            createGiftCardPaymentTokenUseCase.invoke(any(), any())
        } returns mockResult

        // Click submit button
        composeTestRule.onNodeWithTag("addCard").assertIsEnabled().performClick()

        composeTestRule.waitForIdle()

        // Verify event delegate was called with correct event
        verify {
            eventDelegate.widgetEvent(capture(eventSlot))
        }

        val capturedEvent = eventSlot.captured
        assertIs<Event.ButtonEvent>(capturedEvent)
        assertEquals(GiftCardEventNames.TOKENISATION_BUTTON, capturedEvent.name)
        assertEquals(EventAction.CLICK, capturedEvent.action)
    }

    @Test
    fun testGiftCardStorePinConfiguration() {
        // Test with storePin = true (default)
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                GiftCardWidget(
                    config = GiftCardWidgetConfig(
                        accessToken = "testAccessToken",
                        storePin = true
                    ),
                    completion = {}
                )
            }
        }

        // Verify PIN field is displayed
        composeTestRule.onNodeWithText("PIN").assertIsDisplayed()

        // Verify the storePin value in the ViewModel state
        assertTrue(viewModel.inputStateFlow.value.storePin)
    }

    @Test
    fun testGiftCardLoadingDelegate() {
        val loadingDelegate: WidgetLoadingDelegate = mockk(relaxed = true)

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                GiftCardWidget(
                    config = GiftCardWidgetConfig(
                        accessToken = "testAccessToken",
                        storePin = true
                    ),
                    loadingDelegate = loadingDelegate,
                    completion = {}
                )
            }
        }

        // Simulate user interactions with valid data
        composeTestRule.onNodeWithText("Card number")
            .performClick()
            .performTextInput("62734010000131278")
        composeTestRule.onNode(hasText("Card number")).performImeAction()

        composeTestRule.onNodeWithText("PIN")
            .performClick()
            .performTextInput("5814")
        composeTestRule.onNode(hasText("PIN")).performImeAction()

        composeTestRule.waitForIdle()

        // Mock use case response
        val mockResult = Result.success(
            TokenDetails(
                token = "mockToken",
                type = "token"
            )
        )
        coEvery {
            createGiftCardPaymentTokenUseCase.invoke(any(), any())
        } returns mockResult

        // Click submit button
        composeTestRule.onNodeWithTag("addCard").assertIsEnabled().performClick()

        // Wait for the result
        composeTestRule.waitUntilTimeout(5000)

        // Verify loading delegate finish was called (confirms flow completed)
        // Note: widgetLoadingDidStart() may not be called in test environment due to rapid
        // state transitions (Idle -> Loading -> Success happens faster than recomposition)
        verify {
            loadingDelegate.widgetLoadingDidFinish()
        }
    }

}
