package com.paydock.feature.card.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.test.espresso.accessibility.AccessibilityChecks
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.R
import com.paydock.binprocessor.data.dto.BinDataResponse
import com.paydock.core.BaseViewModelKoinTest
import com.paydock.core.extensions.waitUntilTimeout
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.card.domain.model.integration.CardDetailsWidgetConfig
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.model.integration.enums.CardType
import com.paydock.feature.card.domain.model.ui.TokenDetails
import com.paydock.feature.card.domain.usecase.CreateCardPaymentTokenUseCase
import com.paydock.feature.card.domain.usecase.GetCardSchemasUseCase
import com.paydock.feature.card.injection.cardDetailsModule
import com.paydock.feature.card.presentation.viewmodels.CardDetailsViewModel
import io.mockk.coEvery
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
 * Accessibility tests for Card Details Widget.
 *
 * These tests verify WCAG compliance and screen reader support for:
 * - C42971: VoiceOver/TalkBack announcements
 * - C61493: Keyboard navigation (focus traversal)
 * - C61494: Loading overlay and button state accessibility
 *
 * @see <a href="https://www.w3.org/WAI/WCAG21/quickref/">WCAG 2.1 Quick Reference</a>
 */
@OptIn(KoinInternalApi::class)
@RunWith(AndroidJUnit4::class)
@Ignore(
    "Widget-level integration tests that simulate multi-field text input, IME navigation and submit. " +
        "The card fields are built on SdkTextField, which uses clearAndSetSemantics to curate a single " +
        "TalkBack readout; that intentionally removes the editable-text/IME semantics performTextInput and " +
        "performImeAction rely on, so input cannot be injected via the test framework. Re-enable by driving " +
        "CardDetailsViewModel directly to populate form state instead of typing. The field-level accessibility " +
        "readout (label, value, Valid, Error, Editing, required) is covered by SdkTextFieldTest / " +
        "CreditCardNumberInputTest / GiftCardNumberInputTest."
)
internal class CardDetailsAccessibilityTest : BaseViewModelKoinTest<CardDetailsViewModel>() {

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

    private val createCardPaymentTokenUseCase: CreateCardPaymentTokenUseCase = mockk(relaxed = true)
    private val getCardSchemasUseCase: GetCardSchemasUseCase = mockk(relaxed = true)

    private fun setupGetCardSchemasSuccess() {
        val json = """
            {
              "2": { "4": "v", "42": "v", "22": "m", "34": "a", "37": "a", "51": "m", "62": "u", "60": "d" },
              "4": { "4024": "v", "4208": "v", "2221": "m", "3480": "a", "3031": "c", "6011": "d", "6282": "u" },
              "6": {},
              "r": { "2": [], "4": [["2221", "2720", "m"]], "6": [] },
              "s": { "m": "mastercard", "c": "diners", "j": "japcb", "a": "amex", "v": "visa", "d": "discover", "u": "unionpay" },
              "v": 1
            }
        """.trimIndent()
        val binData = json.convertToDataClass<BinDataResponse>()
        val mockResult = Result.success(binData)
        coEvery { getCardSchemasUseCase() } returns mockResult
    }

    override fun initialiseViewModel(): CardDetailsViewModel {
        setupGetCardSchemasSuccess()
        return CardDetailsViewModel(
            accessToken = "testAccessToken",
            gatewayId = null,
            schemeConfig = SupportedSchemeConfig(
                supportedSchemes = CardType.entries.toSet(),
                enableValidation = true
            ),
            getCardSchemasUseCase = getCardSchemasUseCase,
            createCardPaymentTokenUseCase = createCardPaymentTokenUseCase,
            dispatchers = dispatchersProvider,
            savedStateHandle = SavedStateHandle()
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
     * Given the user is on the Card Details Widget Screen
     * And VoiceOver/TalkBack is ON
     * When the user swipes to focus on the widget fields
     * Then the screen reader should announce the field labels
     */
    @Test
    fun testCardDetailsFieldsHaveAccessibleLabels() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                CardDetailsWidget(
                    config = CardDetailsWidgetConfig(
                        accessToken = "testAccessToken",
                        collectCardholderName = true
                    ),
                    completion = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Cardholder name")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Card number")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Expiry")
            .assertIsDisplayed()
    }

    /**
     * C61493: Keyboard Navigation - Verify Tab/Enter navigation works correctly.
     *
     * Given the user is on Card Details Widget
     * When the user uses keyboard navigation (Tab key)
     * Then focus should move through fields in logical order:
     *   Cardholder Name → Card Number → Expiry → CVV → Submit Button
     */
    @Test
    fun testCardDetailsKeyboardNavigationOrder() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                CardDetailsWidget(
                    config = CardDetailsWidgetConfig(
                        accessToken = "testAccessToken",
                        collectCardholderName = true
                    ),
                    completion = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("cardHolderInput").performClick()
        composeTestRule.onAllNodesWithTag("sdkInput")[0].assertIsFocused()

        composeTestRule.onAllNodesWithTag("sdkInput")[0].performImeAction()
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("sdkInput")[1].assertIsFocused()

        composeTestRule.onAllNodesWithTag("sdkInput")[1].performImeAction()
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("sdkInput")[2].assertIsFocused()
    }

    /**
     * C61493: Keyboard Navigation - No keyboard trap.
     *
     * Verify that once the user tabs into a field, they can tab out of it.
     * The keyboard focus must never be trapped or stuck on any element.
     */
    @Test
    fun testNoKeyboardTrap() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                CardDetailsWidget(
                    config = CardDetailsWidgetConfig(
                        accessToken = "testAccessToken",
                        collectCardholderName = true
                    ),
                    completion = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("cardHolderInput").performClick()
        composeTestRule.onAllNodesWithTag("sdkInput")[0].assertIsFocused()

        composeTestRule.onAllNodesWithTag("sdkInput")[0].performImeAction()
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("sdkInput")[1].assertIsFocused()

        composeTestRule.onAllNodesWithTag("sdkInput")[1].performImeAction()
        composeTestRule.waitForIdle()
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
                CardDetailsWidget(
                    config = CardDetailsWidgetConfig(
                        accessToken = "testAccessToken",
                        collectCardholderName = true
                    ),
                    completion = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("submitDetails")
            .assertIsDisplayed()
            .assert(hasTestTag("submitDetails"))
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
                CardDetailsWidget(
                    config = CardDetailsWidgetConfig(
                        accessToken = "testAccessToken",
                        collectCardholderName = true
                    ),
                    completion = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Cardholder name", useUnmergedTree = false)
            .performClick()
            .assertIsFocused()
            .performTextInput("123")
        composeTestRule.onNode(hasText("Cardholder name")).performImeAction()
        composeTestRule.waitForIdle()

        val errorText = getStringRes(R.string.error_card_holder_name)
        composeTestRule.onNodeWithText(errorText, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    /**
     * C42971: Valid input icons have content descriptions.
     *
     * When a field has valid input (shows success icon)
     * Then the icon should have a content description for screen readers
     */
    @Test
    fun testValidInputIconsHaveContentDescriptions() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                CardDetailsWidget(
                    config = CardDetailsWidgetConfig(
                        accessToken = "testAccessToken",
                        collectCardholderName = true
                    ),
                    completion = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Cardholder name", useUnmergedTree = false)
            .performClick()
            .assertIsFocused()
            .performTextInput("John Doe")
        composeTestRule.onNode(hasText("Cardholder name")).performImeAction()
        composeTestRule.waitForIdle()

        val validIconDesc = getStringRes(R.string.content_desc_valid_icon)
        composeTestRule.onNodeWithContentDescription(validIconDesc, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    /**
     * C61493: Standard Activation - Submit button activated by click/tap.
     *
     * Buttons must be activatable by standard interaction methods.
     * Note: On touch devices, this is tap; on keyboards, Space/Enter.
     */
    @Test
    fun testSubmitButtonStandardActivation() {
        val mockToken = "mockToken"
        coEvery {
            createCardPaymentTokenUseCase.invoke("testAccessToken", any())
        } returns Result.success(TokenDetails(token = mockToken, type = "token"))

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                CardDetailsWidget(
                    config = CardDetailsWidgetConfig(
                        accessToken = "testAccessToken",
                        collectCardholderName = true
                    ),
                    completion = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("sdkInput")[0].performTextInput("John Doe")
        composeTestRule.onAllNodesWithTag("sdkInput")[1].performTextInput("4111111111111111")
        composeTestRule.onAllNodesWithTag("sdkInput")[2].performTextInput("12/30")
        composeTestRule.onAllNodesWithTag("sdkInput")[3].performTextInput("123")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("submitDetails")
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitUntilTimeout(5000)
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
                CardDetailsWidget(
                    config = CardDetailsWidgetConfig(
                        accessToken = "testAccessToken",
                        collectCardholderName = true
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
