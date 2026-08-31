package com.paydock.feature.card.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.binprocessor.data.dto.BinDataResponse
import com.paydock.core.BaseViewModelKoinTest
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.domain.error.exceptions.GenericException
import com.paydock.core.domain.model.Event
import com.paydock.core.domain.model.EventAction
import com.paydock.core.extensions.waitUntilTimeout
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.dto.error.displayableMessage
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.presentation.util.WidgetEventDelegate
import com.paydock.feature.card.data.dto.CreateCardPaymentTokenRequest
import com.paydock.feature.card.domain.model.CardDetailsEventNames
import com.paydock.feature.card.domain.model.integration.CardDetailsWidgetConfig
import com.paydock.feature.card.domain.model.integration.CardResult
import com.paydock.feature.card.domain.model.integration.SaveCardConfig
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.model.integration.enums.CardType
import com.paydock.feature.card.domain.model.ui.TokenDetails
import com.paydock.feature.card.domain.usecase.CreateCardPaymentTokenUseCase
import com.paydock.feature.card.domain.usecase.GetCardSchemasUseCase
import com.paydock.feature.card.injection.cardDetailsModule
import com.paydock.feature.card.presentation.state.CardDetailsInputState
import com.paydock.feature.card.presentation.state.CardDetailsWidgetState
import com.paydock.feature.card.presentation.state.rememberCardDetailsWidgetState
import com.paydock.feature.card.presentation.viewmodels.CardDetailsViewModel
import io.ktor.http.HttpStatusCode
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Widget-level integration tests for [CardDetailsWidget].
 *
 * The card fields are built on `SdkTextField`, which uses `clearAndSetSemantics` to curate a single
 * TalkBack readout; that intentionally removes the editable-text/IME semantics `performTextInput`
 * relies on, so form input cannot be injected by typing. These tests therefore **drive the shared
 * [CardDetailsViewModel] directly** (the widget resolves the same instance bound below via Koin) to
 * populate form state, and assert on the widget via test tags, curated `contentDescription`s and the
 * completion callback. Component-level a11y/validation is covered by the per-field component tests
 * and the validator unit tests.
 */
@OptIn(KoinInternalApi::class)
@RunWith(AndroidJUnit4::class)
internal class CardDetailsTest : BaseViewModelKoinTest<CardDetailsViewModel>() {

    private val testModule: Module = module {
        viewModel { viewModel }
    }

    @Before
    fun setUpKoin() {
        unloadKoinModules(cardDetailsModule)
        loadKoinModules(testModule)
    }

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
        coEvery {
            getCardSchemasUseCase()
        } returns mockResult
    }

    @After
    override fun tearDownKoin() {
        unloadKoinModules(testModule)
        super.tearDownKoin()
    }

    private val createCardPaymentTokenUseCase: CreateCardPaymentTokenUseCase = mockk(relaxed = true)
    private val getCardSchemasUseCase: GetCardSchemasUseCase = mockk(relaxed = true)

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

    // region Helpers

    /** Captured by [setWidget] so tests can drive [CardDetailsWidgetState.submit] from outside composition. */
    private lateinit var cardDetailsWidgetState: CardDetailsWidgetState

    private fun setWidget(
        config: CardDetailsWidgetConfig,
        eventDelegate: WidgetEventDelegate? = null,
        enabled: Boolean = true,
        completion: (Result<CardResult>) -> Unit = {}
    ) {
        composeTestRule.setContent {
            // Provides the root Koin scope so the widget resolves the test-bound ViewModel.
            // TODO remove once koin fix lands - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                cardDetailsWidgetState = rememberCardDetailsWidgetState()
                CardDetailsWidget(
                    enabled = enabled,
                    config = config,
                    eventDelegate = eventDelegate,
                    state = cardDetailsWidgetState,
                    completion = completion
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    /**
     * Populates the shared ViewModel with a valid card so the form is valid, without simulating
     * keystrokes. Card number is digits-only and expiry is raw MMYY, matching what the field
     * components pass to the ViewModel's update callbacks.
     */
    private fun setValidInputs(includeCardholder: Boolean = true) {
        composeTestRule.runOnIdle {
            if (includeCardholder) viewModel.updateCardholderName("John Doe")
            viewModel.updateCardNumber("4111111111111111")
            viewModel.updateExpiry("0536")
            viewModel.updateSecurityCode("123")
        }
        composeTestRule.waitForIdle()
    }

    // endregion

    @Test
    fun testCardDetailsInitialStateInput() {
        setWidget(
            config = CardDetailsWidgetConfig(gatewayId = "testGateway", accessToken = "testAccessToken")
        )

        composeTestRule.onNodeWithTag("cardHolderInput").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cardNumberInput").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cardExpiryInput").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cardSecurityCodeInput").assertIsDisplayed()
        // Default config uses activePrimaryButton = true, so the submit button is enabled and
        // validates on tap (it is not gated on form validity).
        composeTestRule.onNodeWithTag("submitDetails").assertIsDisplayed().assertIsEnabled()
        assertFalse(viewModel.inputStateFlow.value.isDataValid)
    }

    @Test
    fun testCardDetailsValidInput() {
        setWidget(
            config = CardDetailsWidgetConfig(gatewayId = "testGateway", accessToken = "testAccessToken")
        )

        setValidInputs()

        assertTrue(viewModel.inputStateFlow.value.isDataValid)
        composeTestRule.onNodeWithTag("submitDetails").assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun testCardDetailsInvalidInput() {
        setWidget(
            config = CardDetailsWidgetConfig(gatewayId = "testGateway", accessToken = "testAccessToken")
        )

        composeTestRule.runOnIdle {
            viewModel.updateCardholderName("John Doe")
            viewModel.updateCardNumber("4111") // too short
            viewModel.updateExpiry("0520") // expired
            viewModel.updateSecurityCode("12") // too short
        }
        composeTestRule.waitForIdle()

        assertFalse(viewModel.inputStateFlow.value.isDataValid)
    }

    @Test
    fun testValidSubmissionWithSuccessTokenResult() {
        val onCardDetailsResult: (Result<CardResult>) -> Unit = mockk()
        val mockToken = "mockToken"
        coEvery {
            createCardPaymentTokenUseCase.invoke("testAccessToken", any())
        } returns Result.success(TokenDetails(token = mockToken, type = "token"))
        every { onCardDetailsResult(any()) } just Runs

        setWidget(
            config = CardDetailsWidgetConfig(gatewayId = "testGateway", accessToken = "testAccessToken"),
            completion = onCardDetailsResult
        )

        setValidInputs()
        composeTestRule.onNodeWithTag("submitDetails").assertIsEnabled().performClick()
        composeTestRule.waitUntilTimeout(5000)

        verify {
            onCardDetailsResult(Result.success(CardResult(mockToken)))
        }
    }

    @Test
    fun testSubmissionWithFailureResult() = runTest {
        val onCardDetailsResult: (Result<CardResult>) -> Unit = mockk()
        val mockError = CardDetailsException.TokenisingCardException(
            error = ApiErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                summary = ErrorSummary(code = "tokenisation_error", message = "Tokenization failed")
            )
        )
        coEvery {
            createCardPaymentTokenUseCase.invoke("testAccessToken", any())
        } returns Result.failure(mockError)
        every { onCardDetailsResult(any()) } just Runs

        setWidget(
            config = CardDetailsWidgetConfig(gatewayId = "testGateway", accessToken = "testAccessToken"),
            completion = onCardDetailsResult
        )

        setValidInputs()
        composeTestRule.onNodeWithTag("submitDetails").assertIsEnabled().performClick()
        composeTestRule.waitUntilTimeout(5000)

        verify {
            onCardDetailsResult(Result.failure(mockError))
        }
    }

    @Test
    fun testCardDetailsWithoutCardholderName() {
        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                collectCardholderName = false
            )
        )

        composeTestRule.onNodeWithTag("cardHolderInput").assertDoesNotExist()
        composeTestRule.onNodeWithTag("cardNumberInput").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cardExpiryInput").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cardSecurityCodeInput").assertIsDisplayed()
        // Default activePrimaryButton = true -> enabled.
        composeTestRule.onNodeWithTag("submitDetails").assertIsDisplayed().assertIsEnabled()

        // Valid without a cardholder name.
        setValidInputs(includeCardholder = false)
        assertTrue(viewModel.inputStateFlow.value.isDataValid)
    }

    @Test
    fun testCardDetailsWithSaveCardToggleDisabled() {
        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                allowSaveCard = null
            )
        )

        composeTestRule.onNodeWithTag("saveCardToggle").assertDoesNotExist()
        composeTestRule.onNodeWithTag("submitDetails").assertIsDisplayed()
    }

    @Test
    fun testCardDetailsWithSaveCardToggle() {
        val onCardDetailsResult: (Result<CardResult>) -> Unit = mockk()
        val consentText = "Remember this card for next time."
        val mockToken = "mockToken"
        coEvery {
            createCardPaymentTokenUseCase.invoke("testAccessToken", any())
        } returns Result.success(TokenDetails(token = mockToken, type = "token"))
        every { onCardDetailsResult(any()) } just Runs

        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                allowSaveCard = SaveCardConfig(consentText = consentText)
            ),
            completion = onCardDetailsResult
        )

        composeTestRule.onNodeWithTag("saveCardToggle").assertIsDisplayed()
        composeTestRule.onNodeWithText(consentText).assertIsDisplayed()

        setValidInputs()
        composeTestRule.onNodeWithTag("submitDetails").assertIsEnabled().performClick()
        composeTestRule.waitUntilTimeout(5000)

        verify {
            onCardDetailsResult(Result.success(CardResult(mockToken, saveCard = false)))
        }
    }

    @Test
    fun testCardDetailsWithSaveCardToggleEnabledSubmitsTrue() {
        val onCardDetailsResult: (Result<CardResult>) -> Unit = mockk()
        val mockToken = "mockToken"
        coEvery {
            createCardPaymentTokenUseCase.invoke("testAccessToken", any())
        } returns Result.success(TokenDetails(token = mockToken, type = "token"))
        every { onCardDetailsResult(any()) } just Runs

        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                allowSaveCard = SaveCardConfig(consentText = "Remember this card for next time.")
            ),
            completion = onCardDetailsResult
        )

        setValidInputs()

        composeTestRule.onNodeWithTag("saveCardToggleSwitch").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("submitDetails").assertIsEnabled().performClick()
        composeTestRule.waitUntilTimeout(5000)

        verify {
            onCardDetailsResult(Result.success(CardResult(mockToken, saveCard = true)))
        }
    }

    @Test
    fun testSupportedCardSchemesDisplay() {
        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                schemeSupport = SupportedSchemeConfig(
                    supportedSchemes = setOf(CardType.VISA, CardType.MASTERCARD),
                    enableValidation = true
                )
            )
        )

        composeTestRule.onNodeWithTag("supportedCardBanner").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(
            "Supported card schemes: Visa, MasterCard"
        ).assertIsDisplayed()
    }

    @Test
    fun testCardNumberValidationDisabledWhenEnableValidationFalse() {
        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                collectCardholderName = false,
                schemeSupport = SupportedSchemeConfig(
                    supportedSchemes = setOf(CardType.MASTERCARD),
                    enableValidation = false
                )
            )
        )

        // Visa number is not in the supported set, but with enableValidation = false the scheme
        // check is skipped, so the (Luhn-valid) number is accepted.
        setValidInputs(includeCardholder = false)

        assertTrue(viewModel.inputStateFlow.value.isDataValid)
    }

    @Test
    fun testCardDetailsWithPrivacyPolicyLink() {
        val privacyPolicyText = "Read our privacy policy"
        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                allowSaveCard = SaveCardConfig(
                    consentText = "Remember this card for next time.",
                    privacyPolicyConfig = SaveCardConfig.PrivacyPolicyConfig(
                        privacyPolicyText = privacyPolicyText,
                        privacyPolicyURL = "https://example.com/privacy"
                    )
                )
            )
        )

        composeTestRule.onNodeWithText(privacyPolicyText).assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun testTokenizationFailureShowsSpecificMessage() = runTest {
        val onCardDetailsResult: (Result<CardResult>) -> Unit = mockk()
        val timeoutError = CardDetailsException.TokenisingCardException(
            error = ApiErrorResponse(
                status = HttpStatusCode.RequestTimeout.value,
                summary = ErrorSummary(
                    code = "request_timeout",
                    message = "The request timed out. Please check your connection."
                )
            )
        )
        coEvery {
            createCardPaymentTokenUseCase.invoke("testAccessToken", any())
        } returns Result.failure(timeoutError)
        every { onCardDetailsResult(any()) } just Runs

        setWidget(
            config = CardDetailsWidgetConfig(gatewayId = "testGateway", accessToken = "testAccessToken"),
            completion = onCardDetailsResult
        )

        setValidInputs()
        composeTestRule.onNodeWithTag("submitDetails").assertIsEnabled().performClick()
        composeTestRule.waitUntilTimeout(5000)

        verify {
            onCardDetailsResult(Result.failure(timeoutError))
        }
    }

    @Test
    fun testStoreSecurityCodeTrueMappedToStoreCcvInApiRequest() = runTest {
        val requestSlot = slot<CreateCardPaymentTokenRequest.TokeniseCardRequest.CreditCard>()
        coEvery {
            createCardPaymentTokenUseCase.invoke(any(), capture(requestSlot))
        } returns Result.success(TokenDetails(token = "tok", type = "token"))

        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                storeSecurityCode = true
            )
        )

        setValidInputs()
        composeTestRule.onNodeWithTag("submitDetails").assertIsEnabled().performClick()
        composeTestRule.waitUntilTimeout(5000)

        coVerify { createCardPaymentTokenUseCase.invoke(any(), any()) }
        assertTrue(requestSlot.captured.storeCVV == true)
    }

    @Test
    fun testStoreSecurityCodeFalseMappedToStoreCcvInApiRequest() = runTest {
        val requestSlot = slot<CreateCardPaymentTokenRequest.TokeniseCardRequest.CreditCard>()
        coEvery {
            createCardPaymentTokenUseCase.invoke(any(), capture(requestSlot))
        } returns Result.success(TokenDetails(token = "tok", type = "token"))

        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                storeSecurityCode = false
            )
        )

        setValidInputs()
        composeTestRule.onNodeWithTag("submitDetails").assertIsEnabled().performClick()
        composeTestRule.waitUntilTimeout(5000)

        coVerify { createCardPaymentTokenUseCase.invoke(any(), any()) }
        assertTrue(requestSlot.captured.storeCVV == false)
    }

    @Test
    fun testStoreSecurityCodeNullOmitsStoreCcvFromApiRequest() = runTest {
        val requestSlot = slot<CreateCardPaymentTokenRequest.TokeniseCardRequest.CreditCard>()
        coEvery {
            createCardPaymentTokenUseCase.invoke(any(), capture(requestSlot))
        } returns Result.success(TokenDetails(token = "tok", type = "token"))

        setWidget(
            config = CardDetailsWidgetConfig(gatewayId = "testGateway", accessToken = "testAccessToken")
        )

        setValidInputs()
        composeTestRule.onNodeWithTag("submitDetails").assertIsEnabled().performClick()
        composeTestRule.waitUntilTimeout(5000)

        coVerify { createCardPaymentTokenUseCase.invoke(any(), any()) }
        assertTrue(requestSlot.captured.storeCVV == null)
    }

    @Test
    fun testTokenizationFailure500ShowsSpecificMessage() = runTest {
        val onCardDetailsResult: (Result<CardResult>) -> Unit = mockk()
        val serverError = CardDetailsException.TokenisingCardException(
            error = ApiErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                summary = ErrorSummary(
                    code = "internal_server_error",
                    message = "Something went wrong on our end. Please try again."
                )
            )
        )
        coEvery {
            createCardPaymentTokenUseCase.invoke("testAccessToken", any())
        } returns Result.failure(serverError)
        every { onCardDetailsResult(any()) } just Runs

        setWidget(
            config = CardDetailsWidgetConfig(gatewayId = "testGateway", accessToken = "testAccessToken"),
            completion = onCardDetailsResult
        )

        setValidInputs()
        composeTestRule.onNodeWithTag("submitDetails").assertIsEnabled().performClick()
        composeTestRule.waitUntilTimeout(5000)

        verify {
            onCardDetailsResult(Result.failure(serverError))
        }
        assertTrue(serverError.error.displayableMessage == "Something went wrong on our end. Please try again.")
    }

    @Test
    fun testTokenizationFailureNetworkShowsSpecificMessage() = runTest {
        val onCardDetailsResult: (Result<CardResult>) -> Unit = mockk()
        val networkError = GenericException.ConnectionException(
            "Could not connect to the server. Please check your internet."
        )
        coEvery {
            createCardPaymentTokenUseCase.invoke("testAccessToken", any())
        } returns Result.failure(networkError)
        every { onCardDetailsResult(any()) } just Runs

        setWidget(
            config = CardDetailsWidgetConfig(gatewayId = "testGateway", accessToken = "testAccessToken"),
            completion = onCardDetailsResult
        )

        setValidInputs()
        composeTestRule.onNodeWithTag("submitDetails").assertIsEnabled().performClick()
        composeTestRule.waitUntilTimeout(5000)

        verify {
            onCardDetailsResult(Result.failure(networkError))
        }
        assertTrue(networkError.message == "Could not connect to the server. Please check your internet.")
    }

    @Test
    fun testPrivacyPolicyLinkClickFiresEventDelegate() {
        val eventSlot = slot<Event>()
        val eventDelegate: WidgetEventDelegate = mockk(relaxed = true) {
            every { widgetEvent(capture(eventSlot)) } just Runs
        }
        val privacyPolicyUrl = "https://example.com/privacy"
        val privacyPolicyText = "Read our privacy policy"

        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                allowSaveCard = SaveCardConfig(
                    consentText = "Remember this card for next time.",
                    privacyPolicyConfig = SaveCardConfig.PrivacyPolicyConfig(
                        privacyPolicyText = privacyPolicyText,
                        privacyPolicyURL = privacyPolicyUrl
                    )
                )
            ),
            eventDelegate = eventDelegate
        )

        composeTestRule.onNodeWithText(privacyPolicyText).assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        val capturedEvent = eventSlot.captured
        assertTrue(capturedEvent is Event.LinkTextEvent)
        val linkEvent = capturedEvent as Event.LinkTextEvent
        assertTrue(linkEvent.url == privacyPolicyUrl)
        assertTrue(linkEvent.name == CardDetailsEventNames.PRIVACY_POLICY_LINK)
        assertTrue(linkEvent.action == EventAction.CLICK)
    }

    @Test
    fun testInvalidPrivacyPolicyUrlHidesLink() {
        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                allowSaveCard = SaveCardConfig(
                    consentText = "Remember this card for next time.",
                    privacyPolicyConfig = SaveCardConfig.PrivacyPolicyConfig(
                        privacyPolicyText = "",
                        privacyPolicyURL = ""
                    )
                )
            )
        )

        composeTestRule.onNodeWithText("Remember this card for next time.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Read our privacy policy").assertDoesNotExist()
    }

    @Test
    fun testSecurityCodeValidationAfterChangingCardType() {
        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                collectCardholderName = false
            )
        )

        // Amex (4-digit CID) with a valid 4-digit code.
        composeTestRule.runOnIdle {
            viewModel.updateCardNumber("371449635398431")
            viewModel.updateExpiry("0536")
            viewModel.updateSecurityCode("1234")
        }
        composeTestRule.waitForIdle()

        // Switch to Visa (expects a 3-digit CVV) - the retained 4-digit code is now invalid.
        composeTestRule.runOnIdle {
            viewModel.updateCardNumber("4111111111111111")
        }
        composeTestRule.waitForIdle()

        assertTrue(
            viewModel.inputStateFlow.value.invalidFields.contains(
                CardDetailsInputState.CardField.SECURITY_CODE
            )
        )
    }

    @Test
    fun testTokenisationButtonClickFiresEventDelegate() {
        val eventSlot = slot<Event>()
        val eventDelegate: WidgetEventDelegate = mockk(relaxed = true) {
            every { widgetEvent(capture(eventSlot)) } just Runs
        }
        coEvery {
            createCardPaymentTokenUseCase.invoke(any(), any())
        } returns Result.success(TokenDetails(token = "tok", type = "token"))

        setWidget(
            config = CardDetailsWidgetConfig(gatewayId = "testGateway", accessToken = "testAccessToken"),
            eventDelegate = eventDelegate
        )

        setValidInputs()
        composeTestRule.onNodeWithTag("submitDetails").assertIsEnabled().performClick()
        composeTestRule.waitForIdle()

        val capturedEvent = eventSlot.captured
        assertTrue(capturedEvent is Event.ButtonEvent)
        val buttonEvent = capturedEvent as Event.ButtonEvent
        assertTrue(buttonEvent.name == CardDetailsEventNames.TOKENISATION_BUTTON)
        assertTrue(buttonEvent.action == EventAction.CLICK)
    }

    @Test
    fun testSaveCardToggleClickFiresEventDelegate() {
        val eventSlot = slot<Event>()
        val eventDelegate: WidgetEventDelegate = mockk(relaxed = true) {
            every { widgetEvent(capture(eventSlot)) } just Runs
        }

        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                allowSaveCard = SaveCardConfig(consentText = "Remember this card for next time.")
            ),
            eventDelegate = eventDelegate
        )

        composeTestRule.onNodeWithTag("saveCardToggleSwitch").performClick()
        composeTestRule.waitForIdle()

        val capturedEvent = eventSlot.captured
        assertTrue(capturedEvent is Event.ToggleEvent)
        val toggleEvent = capturedEvent as Event.ToggleEvent
        assertTrue(toggleEvent.name == CardDetailsEventNames.SAVE_CARD_TOGGLE)
        assertTrue(toggleEvent.action == EventAction.CLICK)
        assertTrue(toggleEvent.state)
    }

    @Test
    fun testSaveCardToggleEnabledMapsToSavedCardConsentAcceptedInApiRequest() = runTest {
        val requestSlot = slot<CreateCardPaymentTokenRequest.TokeniseCardRequest.CreditCard>()
        coEvery {
            createCardPaymentTokenUseCase.invoke(any(), capture(requestSlot))
        } returns Result.success(TokenDetails(token = "tok", type = "token"))

        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                allowSaveCard = SaveCardConfig(consentText = "Remember this card for next time.")
            )
        )

        setValidInputs()

        composeTestRule.onNodeWithTag("saveCardToggleSwitch").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("submitDetails").assertIsEnabled().performClick()
        composeTestRule.waitUntilTimeout(5000)

        coVerify { createCardPaymentTokenUseCase.invoke(any(), any()) }
        assertTrue(requestSlot.captured.savedCardConsentAccepted)
    }

    // region showSubmitButton / showSchemeList / state.submit()

    @Test
    fun testShowSubmitButtonFalseHidesInternalButton() {
        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                showSubmitButton = false
            )
        )

        composeTestRule.onAllNodesWithTag("submitDetails").assertCountEquals(0)
    }

    @Test
    fun testShowSchemeListFalseHidesSchemeBannerWithoutDisablingValidation() {
        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                schemeSupport = SupportedSchemeConfig(
                    supportedSchemes = setOf(CardType.VISA, CardType.MASTERCARD),
                    enableValidation = true,
                    showSchemeList = false
                )
            )
        )

        composeTestRule.onAllNodesWithTag("supportedCardBanner").assertCountEquals(0)

        // Scheme validation itself must still be active: an Amex number (not in supportedSchemes)
        // should still be rejected even though the banner announcing the restriction is hidden.
        composeTestRule.runOnIdle {
            viewModel.updateCardNumber("371449635398431") // Amex
        }
        composeTestRule.waitForIdle()
        assertFalse(viewModel.inputStateFlow.value.isDataValid)
    }

    @Test
    fun testStateSubmitTokenisesWhenFormValid() {
        val mockToken = "mockToken"
        coEvery {
            createCardPaymentTokenUseCase.invoke("testAccessToken", any())
        } returns Result.success(TokenDetails(token = mockToken, type = "token"))

        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                showSubmitButton = false
            )
        )
        setValidInputs()

        composeTestRule.runOnIdle { cardDetailsWidgetState.submit() }
        composeTestRule.waitUntilTimeout(5000)

        coVerify { createCardPaymentTokenUseCase.invoke("testAccessToken", any()) }
    }

    @Test
    fun testStateSubmitWithActivePrimaryButtonFalseDoesNotTokeniseInvalidForm() {
        // Regression test: activePrimaryButton only controls the *internal* button's enabled state.
        // With showSubmitButton = false there is no button at all, so state.submit() must still
        // re-validate rather than tokenising invalid input straight away.
        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                activePrimaryButton = false,
                showSubmitButton = false
            )
        )
        // Form is left empty/invalid - no setValidInputs() call.
        assertFalse(viewModel.inputStateFlow.value.isDataValid)

        composeTestRule.runOnIdle { cardDetailsWidgetState.submit() }
        composeTestRule.waitUntilTimeout(2000)

        coVerify(exactly = 0) { createCardPaymentTokenUseCase.invoke(any(), any()) }
    }

    @Test
    fun testStateSubmitWhenWidgetDisabledDoesNotTokenise() {
        // Regression test: state.submit() bypasses the internal button's `enabled = isEnabled` UI
        // guard entirely, so the widget's own `enabled` param must be re-checked inside submitTapped.
        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                showSubmitButton = false
            ),
            enabled = false
        )
        setValidInputs()

        composeTestRule.runOnIdle { cardDetailsWidgetState.submit() }
        composeTestRule.waitUntilTimeout(2000)

        coVerify(exactly = 0) { createCardPaymentTokenUseCase.invoke(any(), any()) }
    }

    @Test
    fun testStateIsFormValidReflectsFormValidity() {
        setWidget(
            config = CardDetailsWidgetConfig(
                gatewayId = "testGateway",
                accessToken = "testAccessToken",
                activePrimaryButton = false,
                showSubmitButton = false
            )
        )
        // Invalid/empty form: isFormValid must reflect that even though there is no button to observe.
        assertFalse(cardDetailsWidgetState.isFormValid)

        setValidInputs()
        assertTrue(cardDetailsWidgetState.isFormValid)
    }

    // endregion
}
