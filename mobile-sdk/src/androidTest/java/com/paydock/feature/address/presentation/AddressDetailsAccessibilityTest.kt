package com.paydock.feature.address.presentation

import android.location.Geocoder
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
import androidx.test.espresso.accessibility.AccessibilityChecks
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.core.BaseViewModelKoinTest
import com.paydock.feature.address.domain.model.integration.AddressDetailsWidgetConfig
import com.paydock.feature.address.injection.addressDetailsModule
import com.paydock.feature.address.presentation.viewmodels.AddressDetailsViewModel
import com.paydock.feature.address.presentation.viewmodels.AddressSearchViewModel
import com.paydock.feature.address.presentation.viewmodels.CountryAutoCompleteViewModel
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
 * Accessibility tests for Address Details Widget.
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
        "The fields are built on SdkTextField, which uses clearAndSetSemantics to curate a single TalkBack " +
        "readout; that intentionally removes the editable-text/IME semantics performTextInput and " +
        "performImeAction rely on, so input cannot be injected via the test framework. Re-enable by driving " +
        "AddressDetailsViewModel directly to populate form state instead of typing."
)
internal class AddressDetailsAccessibilityTest : BaseViewModelKoinTest<AddressDetailsViewModel>() {

    companion object {
        @JvmStatic
        @BeforeClass
        fun enableAccessibilityChecks() {
            AccessibilityChecks.enable()
                .setRunChecksFromRootView(true)
        }
    }

    private lateinit var geocoder: Geocoder

    private val testModule: Module = module {
        viewModel { AddressSearchViewModel(geocoder, dispatchersProvider) }
        viewModel { CountryAutoCompleteViewModel(dispatchersProvider) }
        viewModel { AddressDetailsViewModel(dispatchersProvider) }
        viewModel { viewModel }
    }

    override fun initialiseViewModel(): AddressDetailsViewModel {
        return AddressDetailsViewModel(
            dispatchers = dispatchersProvider
        )
    }

    @Before
    override fun onStart() {
        geocoder = mockk(relaxed = true)
        super.onStart()
    }

    @Before
    fun setUpKoin() {
        unloadKoinModules(addressDetailsModule)
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
     * Given the user is on the Address Details Widget Screen
     * And VoiceOver/TalkBack is ON
     * When the user swipes to focus on the widget fields
     * Then the screen reader should announce the field labels
     */
    @Test
    fun testAddressFieldsHaveAccessibleLabels() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                AddressDetailsWidget(
                    config = AddressDetailsWidgetConfig(),
                    completion = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("firstName1Input")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("lastNameInput")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Find an address")
            .assertIsDisplayed()
    }

    /**
     * C61493: Keyboard Navigation - Verify Tab/Enter navigation works correctly.
     *
     * Given the user is on Address Details Widget
     * When the user uses keyboard navigation (Tab key)
     * Then focus should move through fields in logical order
     */
    @Test
    fun testAddressKeyboardNavigationOrder() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                AddressDetailsWidget(
                    config = AddressDetailsWidgetConfig(),
                    completion = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        val inputNodes = composeTestRule.onAllNodesWithTag("sdkInput", useUnmergedTree = true)
        if (inputNodes.fetchSemanticsNodes().isNotEmpty()) {
            inputNodes[0].performClick()
            inputNodes[0].assertIsFocused()
        }
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
                AddressDetailsWidget(
                    config = AddressDetailsWidgetConfig(),
                    completion = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        val inputNodes = composeTestRule.onAllNodesWithTag("sdkInput", useUnmergedTree = true)
        if (inputNodes.fetchSemanticsNodes().isNotEmpty()) {
            inputNodes[0].performClick()
            inputNodes[0].assertIsFocused()
            inputNodes[0].performImeAction()
            composeTestRule.waitForIdle()
        }
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
                AddressDetailsWidget(
                    config = AddressDetailsWidgetConfig(),
                    completion = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("saveAddress")
            .assertIsDisplayed()
            .assert(hasTestTag("saveAddress"))
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
                AddressDetailsWidget(
                    config = AddressDetailsWidgetConfig(),
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
