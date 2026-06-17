package com.paydock.feature.card.presentation.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.R
import com.paydock.core.BaseUITest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [CardHolderNameInput] is built on [com.paydock.designsystems.components.input.SdkTextField], which
 * curates a single accessibility readout via `clearAndSetSemantics`. These tests drive the field as
 * a controlled component (value via prop, `forceShowErrors` to surface validation without simulated
 * keystrokes) and assert on the field's `contentDescription`.
 */
@RunWith(AndroidJUnit4::class)
internal class CardHolderNameInputTest : BaseUITest() {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setField(value: String, forceShowErrors: Boolean = false) {
        composeTestRule.setContent {
            var v by remember { mutableStateOf(value) }
            CardHolderNameInput(value = v, forceShowErrors = forceShowErrors, onValueChange = { v = it })
        }
    }

    /** Waits for the field's contentDescription to contain [substring] (tolerates input debounce). */
    private fun awaitContentDescriptionContains(substring: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag("sdkInput").fetchSemanticsNodes().any { node ->
                node.config.contains(SemanticsProperties.ContentDescription) &&
                    node.config[SemanticsProperties.ContentDescription].any { it.contains(substring) }
            }
        }
    }

    @Test
    fun testValidCardHolderName_AnnouncesValid() {
        setField("John Doe")
        awaitContentDescriptionContains(getStringRes(R.string.content_desc_valid_icon))
    }

    @Test
    fun testInvalidName_AnnouncesError() {
        setField("John123", forceShowErrors = true)
        awaitContentDescriptionContains("Error: ${getStringRes(R.string.error_card_holder_name)}")
    }

    @Test
    fun testCardNumberInNameField_AnnouncesLuhnError() {
        setField("4532015112830366", forceShowErrors = true)
        awaitContentDescriptionContains("Error: ${getStringRes(R.string.error_luhn_card_holder_name)}")
    }

    @Test
    fun testEmptyField_AnnouncesEditBox() {
        setField("")
        composeTestRule.onNodeWithTag("sdkInput")
            .assertContentDescriptionContains("Edit box", substring = true)
    }
}
