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
import com.paydock.feature.card.domain.model.ui.CardCode
import com.paydock.feature.card.domain.model.ui.enums.CodeType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [CardSecurityCodeInput] is built on [com.paydock.designsystems.components.input.SdkTextField],
 * which curates a single accessibility readout via `clearAndSetSemantics`. These tests drive the
 * field as a controlled component (value + cardCode via props, `forceShowErrors` to surface
 * validation without simulated keystrokes) and assert on the field's `contentDescription`.
 */
@RunWith(AndroidJUnit4::class)
internal class CardSecurityCodeInputTest : BaseUITest() {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setField(value: String, cardCode: CardCode?, forceShowErrors: Boolean = false) {
        composeTestRule.setContent {
            var v by remember { mutableStateOf(value) }
            CardSecurityCodeInput(
                value = v,
                cardCode = cardCode,
                forceShowErrors = forceShowErrors,
                onValueChange = { v = it }
            )
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
    fun testValidCvv_AnnouncesValid() {
        setField("123", cardCode = CardCode(CodeType.CVV, 3))
        awaitContentDescriptionContains(getStringRes(R.string.content_desc_valid_icon))
    }

    @Test
    fun testValidCid_AnnouncesValid() {
        setField("1234", cardCode = CardCode(CodeType.CID, 4))
        awaitContentDescriptionContains(getStringRes(R.string.content_desc_valid_icon))
    }

    @Test
    fun testTooShortForCid_AnnouncesError() {
        // CID requires 4 digits; "123" is too short.
        setField("123", cardCode = CardCode(CodeType.CID, 4), forceShowErrors = true)
        awaitContentDescriptionContains("Error: ${getStringRes(R.string.error_security_code)}")
    }

    @Test
    fun testEmptyField_AnnouncesLabel() {
        setField("", cardCode = CardCode(CodeType.CVV, 3))
        composeTestRule.onNodeWithTag("sdkInput")
            .assertContentDescriptionContains(CodeType.CVV.name, substring = true)
            .assertContentDescriptionContains("Edit box", substring = true)
    }
}
