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
import com.paydock.core.KoinTestRule
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.extensions.waitUntilTimeout
import com.paydock.feature.card.domain.model.TokenisedCardDetails
import com.paydock.feature.card.domain.usecase.TokeniseCreditCardUseCase
import com.paydock.feature.card.presentation.model.CardResult
import com.paydock.feature.card.presentation.viewmodels.CardDetailsViewModel
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.module.Module
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

    @get:Rule
    override val koinTestRule = KoinTestRule(
        modules = listOf(instrumentedTestModule, testModule)
    )

    private val useCase: TokeniseCreditCardUseCase = mockk(relaxed = true)

    override fun initialiseViewModel(): CardDetailsViewModel =
        CardDetailsViewModel(useCase = useCase, dispatchers = dispatchersProvider)

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
                    gatewayId = "testGateway",
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
        composeTestRule.onNodeWithTag("saveCard").assertIsDisplayed().assertIsNotEnabled()
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
                    gatewayId = "testGateway",
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
        composeTestRule.onNodeWithTag("saveCard").assertIsDisplayed().assertIsEnabled()

        // Assert ViewModel interactions
        assertTrue(viewModel.stateFlow.value.isDataValid)
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
                    gatewayId = "testGateway",
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
        assertFalse(viewModel.stateFlow.value.isDataValid)
    }

    @Test
    fun testValidSubmissionWithSuccessTokenResult() {
        val onCardDetailsResult: (Result<CardResult>) -> Unit = mockk()

        // Set up your ViewModel and other dependencies
        composeTestRule.setContent {
            CardDetailsWidget(
                gatewayId = "testGateway",
                completion = onCardDetailsResult
            )
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
        composeTestRule.onNodeWithTag("saveCard").assertIsDisplayed().assertIsEnabled()

        // Allow some time for the UI to update
        composeTestRule.waitForIdle()

        // For token case
        val mockToken = "mockToken"
        val mockResult = Result.success(TokenisedCardDetails(token = mockToken, type = "token"))
        coEvery { useCase.invoke(any()) } returns mockResult
        every { onCardDetailsResult(any()) } just Runs

        composeTestRule.onNodeWithTag("saveCard").assertIsEnabled().performClick()

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
                    gatewayId = "testGateway",
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
        composeTestRule.onNodeWithTag("saveCard").assertIsDisplayed().assertIsEnabled()

        // Allow some time for the UI to update
        composeTestRule.waitForIdle()

        // For token case
        val mockError = Exception("Tokenization failed")
        val mockResult = Result.failure<TokenisedCardDetails>(mockError)
        coEvery { useCase.invoke(any()) } returns mockResult
        every { onCardDetailsResult(any()) } just Runs

        composeTestRule.onNodeWithTag("saveCard").assertIsEnabled().performClick()

        // Trigger the LaunchedEffects
        composeTestRule.waitForIdle()

        verify {
            onCardDetailsResult(Result.failure(CardDetailsException.UnknownException("_Unknown error")))
        }
    }

}