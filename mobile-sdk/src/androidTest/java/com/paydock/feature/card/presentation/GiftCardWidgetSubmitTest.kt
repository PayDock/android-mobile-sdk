package com.paydock.feature.card.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.core.BaseViewModelKoinTest
import com.paydock.core.extensions.waitUntilTimeout
import com.paydock.feature.card.domain.model.integration.GiftCardWidgetConfig
import com.paydock.feature.card.domain.model.ui.TokenDetails
import com.paydock.feature.card.domain.usecase.CreateGiftCardPaymentTokenUseCase
import com.paydock.feature.card.injection.cardDetailsModule
import com.paydock.feature.card.presentation.state.GiftCardWidgetState
import com.paydock.feature.card.presentation.state.rememberGiftCardWidgetState
import com.paydock.feature.card.presentation.viewmodels.GiftCardViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
 * Regression tests for [GiftCardWidget]'s `state.submit()` external-trigger path
 * (`GiftCardWidgetConfig.showSubmitButton` / [GiftCardWidgetState]).
 *
 * Unlike [GiftCardTest] (currently `@Ignore`d because `SdkTextField`'s `clearAndSetSemantics` blocks
 * `performTextInput`), these tests drive the shared [GiftCardViewModel] directly to populate form
 * state, so they don't hit that limitation and can run normally.
 */
@OptIn(KoinInternalApi::class)
@RunWith(AndroidJUnit4::class)
internal class GiftCardWidgetSubmitTest : BaseViewModelKoinTest<GiftCardViewModel>() {

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
            config = GiftCardWidgetConfig(accessToken = "testAccessToken", storePin = true),
            createCardPaymentTokenUseCase = createGiftCardPaymentTokenUseCase,
            dispatchers = dispatchersProvider
        )
    }

    private lateinit var giftCardWidgetState: GiftCardWidgetState

    private fun setWidget(config: GiftCardWidgetConfig, enabled: Boolean = true) {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                giftCardWidgetState = rememberGiftCardWidgetState()
                GiftCardWidget(
                    enabled = enabled,
                    config = config,
                    state = giftCardWidgetState,
                    completion = {}
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun setValidInputs() {
        composeTestRule.runOnIdle {
            viewModel.updateCardNumber("62734010000131278")
            viewModel.updateCardPin("5814")
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testShowSubmitButtonFalseHidesInternalButton() {
        setWidget(
            config = GiftCardWidgetConfig(accessToken = "testAccessToken", showSubmitButton = false)
        )

        composeTestRule.onAllNodesWithTag("addCard").assertCountEquals(0)
    }

    @Test
    fun testStateSubmitTokenisesWhenFormValid() {
        coEvery {
            createGiftCardPaymentTokenUseCase.invoke("testAccessToken", any())
        } returns Result.success(TokenDetails(token = "mockToken", type = "token"))

        setWidget(
            config = GiftCardWidgetConfig(accessToken = "testAccessToken", showSubmitButton = false)
        )
        setValidInputs()

        composeTestRule.runOnIdle { giftCardWidgetState.submit() }
        composeTestRule.waitUntilTimeout(5000)

        coVerify { createGiftCardPaymentTokenUseCase.invoke("testAccessToken", any()) }
    }

    @Test
    fun testStateSubmitWithActivePrimaryButtonFalseDoesNotTokeniseInvalidForm() {
        // Regression test: activePrimaryButton only controls the *internal* button's enabled state.
        // With showSubmitButton = false there is no button at all, so state.submit() must still
        // re-validate rather than tokenising invalid input straight away.
        setWidget(
            config = GiftCardWidgetConfig(
                accessToken = "testAccessToken",
                activePrimaryButton = false,
                showSubmitButton = false
            )
        )
        // Form is left empty/invalid - no setValidInputs() call.
        assertFalse(viewModel.inputStateFlow.value.isDataValid)

        composeTestRule.runOnIdle { giftCardWidgetState.submit() }
        composeTestRule.waitUntilTimeout(2000)

        coVerify(exactly = 0) { createGiftCardPaymentTokenUseCase.invoke(any(), any()) }
    }

    @Test
    fun testStateSubmitWhenWidgetDisabledDoesNotTokenise() {
        // Regression test: state.submit() bypasses the internal button's `enabled = isEnabled` UI
        // guard entirely, so the widget's own `enabled` param must be re-checked inside submitTapped.
        setWidget(
            config = GiftCardWidgetConfig(accessToken = "testAccessToken", showSubmitButton = false),
            enabled = false
        )
        setValidInputs()

        composeTestRule.runOnIdle { giftCardWidgetState.submit() }
        composeTestRule.waitUntilTimeout(2000)

        coVerify(exactly = 0) { createGiftCardPaymentTokenUseCase.invoke(any(), any()) }
    }

    @Test
    fun testStateIsFormValidReflectsFormValidity() {
        setWidget(
            config = GiftCardWidgetConfig(
                accessToken = "testAccessToken",
                activePrimaryButton = false,
                showSubmitButton = false
            )
        )
        assertFalse(giftCardWidgetState.isFormValid)

        setValidInputs()
        assertTrue(giftCardWidgetState.isFormValid)
    }
}
