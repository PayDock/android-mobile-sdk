package com.paydock.feature.card.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.core.BaseViewModelKoinTest
import com.paydock.core.extensions.waitUntilTimeout
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
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.ComposeContextWrapper
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import kotlin.test.assertFalse
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
                LocalKoinScope provides ComposeContextWrapper(
                    KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope
                ),
                LocalKoinApplication provides ComposeContextWrapper(
                    KoinPlatformTools.defaultContext().get()
                )
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
                LocalKoinScope provides ComposeContextWrapper(
                    KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope
                ),
                LocalKoinApplication provides ComposeContextWrapper(
                    KoinPlatformTools.defaultContext().get()
                )
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
                LocalKoinScope provides ComposeContextWrapper(
                    KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope
                ),
                LocalKoinApplication provides ComposeContextWrapper(
                    KoinPlatformTools.defaultContext().get()
                )
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
                LocalKoinScope provides ComposeContextWrapper(
                    KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope
                ),
                LocalKoinApplication provides ComposeContextWrapper(
                    KoinPlatformTools.defaultContext().get()
                )
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

}
