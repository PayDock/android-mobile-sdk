package com.paydock.feature.card.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.core.BaseViewModelKoinTest
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.extensions.waitUntilTimeout
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.card.data.dto.CardSchemasResponse
import com.paydock.feature.card.data.mapper.asEntity
import com.paydock.feature.card.domain.model.integration.CardDetailsWidgetConfig
import com.paydock.feature.card.domain.model.integration.CardResult
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.model.integration.enums.CardType
import com.paydock.feature.card.domain.model.ui.TokenDetails
import com.paydock.feature.card.domain.usecase.CreateCardPaymentTokenUseCase
import com.paydock.feature.card.domain.usecase.GetCardSchemasUseCase
import com.paydock.feature.card.injection.cardDetailsModule
import com.paydock.feature.card.presentation.viewmodels.CardDetailsViewModel
import io.ktor.http.HttpStatusCode
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
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
              "card_schemas": [
                { "bin": "400000~420317", "schema": "visa"},
                { "bin": "420412", "schema": "visa"},
                { "bin": "2221~2720", "schema": "mastercard" },
                { "bin": "324000", "schema": "amex" },
                { "bin": "309", "schema": "diners" },
                { "bin": "6011", "schema": "discover" },
                { "bin": "180000~180099", "schema": "japcb" },
                { "bin": "633454", "schema": "solo" },
                { "bin": "5610", "schema": "ausbc" }
              ]
            }
        """.trimIndent()
        val response = json.convertToDataClass<CardSchemasResponse>()
        // Act
        val cardSchemas = response.asEntity()
        val mockResult = Result.success(cardSchemas)
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
    private val getCardSchemasUseCase: GetCardSchemasUseCase =
        mockk(relaxed = true)

    override fun initialiseViewModel(): CardDetailsViewModel {
        setupGetCardSchemasSuccess()
        return CardDetailsViewModel(
            accessToken = "testAccessToken",
            gatewayId = null,
            schemeConfig = SupportedSchemeConfig(
                supportedSchemes = CardType.entries.toSet(),
                enableValidation = true
            ),
            createCardPaymentTokenUseCase = createCardPaymentTokenUseCase,
            getCardSchemasUseCase = getCardSchemasUseCase,
            dispatchers = dispatchersProvider
        )
    }

    @Test
    fun testCardDetailsInitialStateInput() {
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                CardDetailsWidget(
                    config = CardDetailsWidgetConfig(
                        gatewayId = "testGateway",
                        accessToken = "testAccessToken"
                    ),
                    completion = {}
                )
            }
        }

        // Verify UI elements and interactions
        composeTestRule.onNodeWithText("Card information").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cardHolderInput").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cardNumberInput").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cardExpiryInput").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cardSecurityCodeInput").assertIsDisplayed()
        composeTestRule.onNodeWithTag("submitDetails").assertIsDisplayed().assertIsNotEnabled()
    }

    @Test
    fun testCardDetailsValidInput() {
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                CardDetailsWidget(
                    config = CardDetailsWidgetConfig(
                        gatewayId = "testGateway",
                        accessToken = "testAccessToken"
                    ),
                    completion = {}
                )
            }
        }
        // Simulate user interactions
        // ... Use composeTestRule.onNode and composeTestRule.onNodeWithContentDescription
        //     to interact with specific UI elements
        // Allow some time for the UI to update
        composeTestRule.onNodeWithText("Cardholder name", useUnmergedTree = false)
            .performClick()
            .assertIsFocused()
            .performTextInput("John Doe")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNode(hasText("Cardholder name")).performImeAction()

        composeTestRule.onNodeWithText("Card number", useUnmergedTree = false)
            .performClick()
            .assertIsFocused()
            .performTextInput("4111111111111111")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNode(hasText("Card number")).performImeAction()

        composeTestRule.onNodeWithText("Expiry")
            .performClick()
            .assertIsFocused()
            .performTextInput("0536")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNode(hasText("Expiry")).performImeAction()

        composeTestRule.onNodeWithText("CVV")
            .performClick()
            .assertIsFocused()
            .performTextInput("123")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNode(hasText("CVV")).performImeAction()

        // Allow some time for the UI to update
        composeTestRule.waitForIdle()

        // Verify UI updates/changes
        composeTestRule.onNodeWithText("Cardholder name").assert(hasText("John Doe"))
        composeTestRule.onNodeWithText("Card number").assert(hasText("4111 1111 1111 1111 "))
        composeTestRule.onNodeWithText("Expiry").assert(hasText("05/36"))
        composeTestRule.onNodeWithText("CVV").assert(hasText("123"))
        composeTestRule.onNodeWithTag("submitDetails").assertIsDisplayed().assertIsEnabled()

        // Assert ViewModel interactions
        assertTrue(viewModel.inputStateFlow.value.isDataValid)
    }

    @Test
    fun testCardDetailsInvalidInput() {
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                CardDetailsWidget(
                    config = CardDetailsWidgetConfig(
                        gatewayId = "testGateway",
                        accessToken = "testAccessToken"
                    ),
                    completion = {}
                )
            }
        }

        // Simulate user interactions
        // ... Use composeTestRule.onNode and composeTestRule.onNodeWithContentDescription
        //     to interact with specific UI elements
        // Allow some time for the UI to update
        composeTestRule.onNodeWithText("Cardholder name", useUnmergedTree = false)
            .performClick()
            .assertIsFocused()
            .performTextInput("John Doe")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNode(hasText("Cardholder name")).performImeAction()

        composeTestRule.onNodeWithText("Card number", useUnmergedTree = false)
            .performClick()
            .assertIsFocused()
            .performTextInput("4111")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNode(hasText("Card number")).performImeAction()

        composeTestRule.onNodeWithText("Expiry")
            .performClick()
            .assertIsFocused()
            .performTextInput("0520")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNode(hasText("Expiry")).performImeAction()

        composeTestRule.onNodeWithText("CVV")
            .performClick()
            .assertIsFocused()
            .performTextInput("12")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNode(hasText("CVV")).performImeAction()

        // Allow some time for the UI to update
        composeTestRule.waitForIdle()

        // Assert ViewModel interactions
        assertFalse(viewModel.inputStateFlow.value.isDataValid)
    }

    @Test
    fun testValidSubmissionWithSuccessTokenResult() {
        val onCardDetailsResult: (Result<CardResult>) -> Unit = mockk()
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                // Set up your ViewModel and other dependencies
                CardDetailsWidget(
                    config = CardDetailsWidgetConfig(
                        gatewayId = "testGateway",
                        accessToken = "testAccessToken"
                    ),
                    completion = onCardDetailsResult
                )
            }
        }

        // Simulate user interactions
        // ... Use composeTestRule.onNode and composeTestRule.onNodeWithContentDescription
        //     to interact with specific UI elements
        // Allow some time for the UI to update
        composeTestRule.onNodeWithText("Cardholder name", useUnmergedTree = false)
            .performClick()
            .assertIsFocused()
            .performTextInput("John Doe")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNode(hasText("Cardholder name")).performImeAction()

        composeTestRule.onNodeWithText("Card number", useUnmergedTree = false)
            .performClick()
            .assertIsFocused()
            .performTextInput("4111111111111111")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNode(hasText("Card number")).performImeAction()

        composeTestRule.onNodeWithText("Expiry")
            .performClick()
            .assertIsFocused()
            .performTextInput("0536")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNode(hasText("Expiry")).performImeAction()

        composeTestRule.onNodeWithText("CVV")
            .performClick()
            .assertIsFocused()
            .performTextInput("123")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNode(hasText("CVV")).performImeAction()

        // Verify UI updates/changes
        composeTestRule.onNodeWithText("Cardholder name").assert(hasText("John Doe"))
        composeTestRule.onNodeWithText("Card number").assert(hasText("4111 1111 1111 1111 "))
        composeTestRule.onNodeWithText("Expiry").assert(hasText("05/36"))
        composeTestRule.onNodeWithText("CVV").assert(hasText("123"))
        composeTestRule.onNodeWithTag("submitDetails").assertIsDisplayed().assertIsEnabled()

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
            createCardPaymentTokenUseCase.invoke(
                "testAccessToken",
                any()
            )
        } returns mockResult
        every { onCardDetailsResult(any()) } just Runs

        composeTestRule.onNodeWithTag("submitDetails").assertIsEnabled().performClick()

        // Trigger the LaunchedEffects
        composeTestRule.waitUntilTimeout(5000)

        verify {
            onCardDetailsResult(Result.success(CardResult(mockToken)))
            viewModel.resetResultState()
        }
    }

    @Test
    fun testSubmissionWithFailureResult() = runTest {
        val onCardDetailsResult: (Result<CardResult>) -> Unit = mockk()

        // Set up your ViewModel and other dependencies
        composeTestRule.setContent {
            // This shouldn't be needed, but allows robolectric tests to run successfully
            // TODO remove once a solution is found or a fix in koin - https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                CardDetailsWidget(
                    config = CardDetailsWidgetConfig(
                        gatewayId = "testGateway",
                        accessToken = "testAccessToken"
                    ),
                    completion = onCardDetailsResult
                )
            }
        }

        // Simulate user interactions
        // ... Use composeTestRule.onNode and composeTestRule.onNodeWithContentDescription
        //     to interact with specific UI elements
        // Allow some time for the UI to update
        composeTestRule.onNodeWithText("Cardholder name", useUnmergedTree = false)
            .performClick()
            .assertIsFocused()
            .performTextInput("John Doe")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNode(hasText("Cardholder name")).performImeAction()

        composeTestRule.onNodeWithText("Card number", useUnmergedTree = false)
            .performClick()
            .assertIsFocused()
            .performTextInput("4111111111111111")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNode(hasText("Card number")).performImeAction()

        composeTestRule.onNodeWithText("Expiry")
            .performClick()
            .assertIsFocused()
            .performTextInput("0536")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNode(hasText("Expiry")).performImeAction()

        composeTestRule.onNodeWithText("CVV")
            .performClick()
            .assertIsFocused()
            .performTextInput("123")
        // Send the IME action (e.g., Done) to the TextField
        composeTestRule.onNode(hasText("CVV")).performImeAction()

        // Verify UI updates/changes
        composeTestRule.onNodeWithText("Cardholder name").assert(hasText("John Doe"))
        composeTestRule.onNodeWithText("Card number").assert(hasText("4111 1111 1111 1111 "))
        composeTestRule.onNodeWithText("Expiry").assert(hasText("05/36"))
        composeTestRule.onNodeWithText("CVV").assert(hasText("123"))
        composeTestRule.onNodeWithTag("submitDetails").assertIsDisplayed().assertIsEnabled()

        // Allow some time for the UI to update
        composeTestRule.waitForIdle()

        // For token case
        val mockError = CardDetailsException.TokenisingCardException(
            error = ApiErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                summary = ErrorSummary(
                    code = "tokenisation_error",
                    message = "Tokenization failed"
                )
            )
        )
        val mockResult = Result.failure<TokenDetails>(mockError)
        coEvery { createCardPaymentTokenUseCase.invoke("testAccessToken", any()) } returns mockResult
        every { onCardDetailsResult(any()) } just Runs

        composeTestRule.onNodeWithTag("submitDetails").assertIsEnabled().performClick()

        // Trigger the LaunchedEffects
        composeTestRule.waitForIdle()

        verify {
            onCardDetailsResult(Result.failure(mockError))
        }
    }
}