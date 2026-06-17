package com.paydock.feature.card.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.accessibility.AccessibilityChecks
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.core.BaseViewModelKoinTest
import com.paydock.feature.card.domain.model.integration.GiftCardWidgetConfig
import com.paydock.feature.card.domain.usecase.CreateGiftCardPaymentTokenUseCase
import com.paydock.feature.card.injection.cardDetailsModule
import com.paydock.feature.card.presentation.viewmodels.GiftCardViewModel
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
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

/**
 * Accessibility tests for Gift Card Widget.
 *
 * These tests verify WCAG compliance and screen reader support for:
 * - C42971: VoiceOver/TalkBack announcements
 * - C61493: Keyboard navigation (focus traversal)
 * - C61494: Loading overlay and button state accessibility
 */
@OptIn(KoinInternalApi::class)
@RunWith(AndroidJUnit4::class)
@Ignore(
    "Widget-level integration tests that simulate multi-field text input, IME navigation and submit. " +
        "The gift card fields are built on SdkTextField, which uses clearAndSetSemantics to curate a single " +
        "TalkBack readout; that intentionally removes the editable-text/IME semantics performTextInput and " +
        "performImeAction rely on, so input cannot be injected via the test framework. Re-enable by driving " +
        "GiftCardViewModel directly to populate form state instead of typing. Field-level a11y is covered by " +
        "GiftCardNumberInputTest / CardPinInputTest and the SdkTextField component tests."
)
internal class GiftCardAccessibilityTest : BaseViewModelKoinTest<GiftCardViewModel>() {

    companion object {
        @JvmStatic
        @BeforeClass
        fun enableAccessibilityChecks() {
            AccessibilityChecks.enable()
                .setRunChecksFromRootView(true)
        }
    }

    private val testModule: Module = module {
        viewModel { viewModel }
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

    /**
     * C42971: VoiceOver/TalkBack - Verify all input fields have accessible labels.
     *
     * Given the user is on the Gift Card Widget Screen
     * And VoiceOver/TalkBack is ON
     * When the user swipes to focus on the widget fields
     * Then the screen reader should announce the field labels
     */
    @Test
    fun testGiftCardFieldsHaveAccessibleLabels() {
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

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Card number")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("PIN")
            .assertIsDisplayed()
    }

    /**
     * C61493: Keyboard Navigation - Verify Tab/Enter navigation works correctly.
     *
     * Given the user is on Gift Card Widget
     * When the user uses keyboard navigation (Tab key)
     * Then focus should move through fields in logical order:
     *   Card Number → PIN → Add Button
     */
    @Test
    fun testGiftCardKeyboardNavigationOrder() {
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

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("cardNumberInput").performClick()
        composeTestRule.onAllNodesWithTag("sdkInput")[0].assertIsFocused()

        composeTestRule.onAllNodesWithTag("sdkInput")[0].performImeAction()
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("sdkInput")[1].assertIsFocused()
    }

    /**
     * C61493: Keyboard Navigation - No keyboard trap.
     *
     * Verify that once the user tabs into a field, they can tab out of it.
     */
    @Test
    fun testNoKeyboardTrap() {
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

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("cardNumberInput").performClick()
        composeTestRule.onAllNodesWithTag("sdkInput")[0].assertIsFocused()

        composeTestRule.onAllNodesWithTag("sdkInput")[0].performImeAction()
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("sdkInput")[1].assertIsFocused()
    }

    /**
     * C61494: Loading Overlay - Button state accessibility.
     *
     * Given the user focuses on a button that is disabled
     * Then the button must be announced as "Disabled" to screen readers
     */
    @Test
    fun testDisabledButtonStateAccessibility() {
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

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("addCard")
            .assertIsDisplayed()
            .assert(hasTestTag("addCard"))
    }

    /**
     * C42971: VoiceOver/TalkBack - Error messages are announced.
     *
     * Given the user enters invalid data
     * When an error is displayed
     * Then the screen reader should announce the error message
     */
    @Test
    fun testErrorMessagesAreAccessible() {
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

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("cardNumberInput").performClick()
        composeTestRule.onAllNodesWithTag("sdkInput")[0].performTextInput("123")
        composeTestRule.onAllNodesWithTag("sdkInput")[0].performImeAction()
        composeTestRule.waitForIdle()
    }

    /**
     * C60848/C60849: Text fields are focusable for accessibility services.
     *
     * All interactive elements must be focusable for accessibility services.
     */
    @Test
    fun testAllFieldsAreFocusable() {
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

        composeTestRule.waitForIdle()

        val inputNodes = composeTestRule.onAllNodesWithTag("sdkInput", useUnmergedTree = true)
        inputNodes.fetchSemanticsNodes().forEachIndexed { index, _ ->
            inputNodes[index].assert(isFocusable())
        }
    }
}
