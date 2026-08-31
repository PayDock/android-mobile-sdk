package com.paydock.feature.card.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
 * Accessibility tests for [CardDetailsWidget].
 *
 * Verifies screen-reader support and button-state accessibility. The card fields curate a single
 * TalkBack readout via `clearAndSetSemantics` (which removes editable-text/IME semantics), so form
 * state is driven directly through the shared [CardDetailsViewModel] (resolved via the Koin binding
 * below) rather than by typing, and assertions read the curated `contentDescription`.
 *
 * Keyboard/IME focus-traversal cases (Next/Done between fields) cannot be exercised through this
 * curated-semantics setup and remain verified manually (see the card-details manual QA checklist).
 */
@OptIn(KoinInternalApi::class)
@RunWith(AndroidJUnit4::class)
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

    // region Helpers

    private fun setWidget(config: CardDetailsWidgetConfig) {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                CardDetailsWidget(config = config, completion = {})
            }
        }
        composeTestRule.waitForIdle()
    }

    /**
     * The curated (single) semantics node of a field, addressed by its container test tag. In the
     * widget each field's caller-supplied tag (e.g. `cardNumberInput`) overrides `SdkTextField`'s
     * internal `sdkInput` tag and carries the merged `contentDescription` readout.
     */
    private fun field(tag: String) = composeTestRule.onNodeWithTag(tag)

    // endregion

    /**
     * C42971: VoiceOver/TalkBack - input fields expose accessible labels.
     */
    @Test
    fun testCardDetailsFieldsHaveAccessibleLabels() {
        setWidget(
            CardDetailsWidgetConfig(accessToken = "testAccessToken", collectCardholderName = true)
        )

        field("cardHolderInput")
            .assertContentDescriptionContains(getStringRes(R.string.label_cardholder_name), substring = true)
        field("cardNumberInput")
            .assertContentDescriptionContains(getStringRes(R.string.label_card_number), substring = true)
        field("cardExpiryInput")
            .assertContentDescriptionContains(getStringRes(R.string.label_expiry), substring = true)
    }

    /**
     * C61493: Keyboard navigation focus order (Cardholder → Number → Expiry → CVV → Submit).
     *
     * Not automatable here: `SdkTextField` curates semantics via `clearAndSetSemantics`, which
     * removes the IME/editable semantics `performImeAction` needs to move focus between fields.
     * Verified manually — see docs/qa/card-details-android-manual-checklist.md (Keyboard & input).
     */
    @Test
    @Ignore("IME focus traversal cannot be driven through curated (clearAndSetSemantics) fields; verified manually.")
    fun testCardDetailsKeyboardNavigationOrder() = Unit

    /**
     * C61493: No keyboard trap. Same IME-semantics limitation as the focus-order case; manual.
     */
    @Test
    @Ignore("IME focus traversal cannot be driven through curated (clearAndSetSemantics) fields; verified manually.")
    fun testNoKeyboardTrap() = Unit

    /**
     * C61494: Loading overlay - disabled button state is exposed to accessibility services.
     * With activePrimaryButton = false the button is gated on validity, so an empty form disables it.
     */
    @Test
    fun testDisabledButtonStateAccessibility() {
        setWidget(
            CardDetailsWidgetConfig(
                accessToken = "testAccessToken",
                collectCardholderName = true,
                activePrimaryButton = false
            )
        )

        composeTestRule.onNodeWithTag("submitDetails")
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    /**
     * C42971: VoiceOver/TalkBack - error messages are announced via the field's curated readout.
     */
    @Test
    fun testErrorMessagesAreAccessible() {
        setWidget(
            CardDetailsWidgetConfig(accessToken = "testAccessToken", collectCardholderName = true)
        )

        composeTestRule.runOnIdle {
            viewModel.updateCardholderName("123") // not a valid name
            viewModel.validateAllFields() // surface errors without simulated keystrokes
        }
        composeTestRule.waitForIdle()

        field("cardHolderInput")
            .assertContentDescriptionContains("Error", substring = true)
            .assertContentDescriptionContains(getStringRes(R.string.error_card_holder_name), substring = true)
    }

    /**
     * C42971: Valid input exposes the valid-state icon description to screen readers.
     */
    @Test
    fun testValidInputIconsHaveContentDescriptions() {
        setWidget(
            CardDetailsWidgetConfig(accessToken = "testAccessToken", collectCardholderName = true)
        )

        composeTestRule.runOnIdle {
            viewModel.updateCardholderName("John Doe")
        }
        composeTestRule.waitForIdle()

        field("cardHolderInput")
            .assertContentDescriptionContains(getStringRes(R.string.content_desc_valid_icon), substring = true)
    }

    /**
     * C61493: Submit button is activatable by standard interaction (tap).
     */
    @Test
    fun testSubmitButtonStandardActivation() {
        coEvery {
            createCardPaymentTokenUseCase.invoke("testAccessToken", any())
        } returns Result.success(TokenDetails(token = "mockToken", type = "token"))

        setWidget(
            CardDetailsWidgetConfig(accessToken = "testAccessToken", collectCardholderName = true)
        )

        composeTestRule.runOnIdle {
            viewModel.updateCardholderName("John Doe")
            viewModel.updateCardNumber("4111111111111111")
            viewModel.updateExpiry("0536")
            viewModel.updateSecurityCode("123")
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("submitDetails")
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitUntilTimeout(5000)
    }

    /**
     * C60848/C60849: every input field is exposed to accessibility services as its own node.
     * With cardholder collection on, the form exposes four curated field nodes
     * (cardholder, number, expiry, security).
     */
    @Test
    fun testAllFieldsAreExposedToAccessibility() {
        setWidget(
            CardDetailsWidgetConfig(accessToken = "testAccessToken", collectCardholderName = true)
        )

        listOf("cardHolderInput", "cardNumberInput", "cardExpiryInput", "cardSecurityCodeInput")
            .forEach { tag -> composeTestRule.onNodeWithTag(tag).assertIsDisplayed() }
    }
}
