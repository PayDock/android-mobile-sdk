package com.paydock.designsystems.components

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.core.BaseUITest
import com.paydock.designsystems.components.input.SdkTextField
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [SdkTextField] curates a single accessibility readout via `clearAndSetSemantics`, so its internal
 * label/placeholder/icons are intentionally not individual semantics nodes. These tests therefore
 * drive the field as a controlled component (state via props, focus via click) and assert on the
 * field's `contentDescription` / `stateDescription` — the accessibility contract it exposes.
 */
@OptIn(ExperimentalComposeUiApi::class)
@RunWith(AndroidJUnit4::class)
internal class SdkTextFieldTest : BaseUITest() {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun hasStateDescription(value: String) =
        SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)

    @Test
    fun testSdkTextField_DefaultState() {
        composeTestRule.setContent {
            SdkTextField(
                value = "",
                onValueChange = { /* No-op */ },
                placeholder = "Enter your name",
                label = "Name"
            )
        }

        // The label, "Edit box" role and "required" suffix are announced for an unfocused empty field.
        composeTestRule.onNodeWithTag("sdkInput")
            .assertIsNotFocused()
            .assertContentDescriptionContains("Name", substring = true)
            .assertContentDescriptionContains("Edit box", substring = true)
            .assertContentDescriptionContains("required", substring = true)
    }

    @Test
    fun testSdkTextField_DefaultFocusedState() {
        composeTestRule.setContent {
            SdkTextField(
                value = "",
                onValueChange = { /* No-op */ },
                placeholder = "Enter your name",
                label = "Name"
            )
        }

        composeTestRule.onNodeWithTag("sdkInput").performClick()

        // Once focused, the placeholder is offered as an example and the field reports "Editing".
        composeTestRule.onNodeWithTag("sdkInput")
            .assertIsFocused()
            .assertContentDescriptionContains("Example: Enter your name", substring = true)
            .assert(hasStateDescription("Editing"))
    }

    @Test
    fun testSdkTextField_TextState() {
        composeTestRule.setContent {
            SdkTextField(
                value = "John Doe",
                onValueChange = { /* No-op */ },
                placeholder = "Enter your name",
                label = "Name"
            )
        }

        // The value is announced character-by-character (spaced) so it isn't read as one token.
        composeTestRule.onNodeWithTag("sdkInput")
            .assertContentDescriptionContains("Name", substring = true)
            .assertContentDescriptionContains("J o h n", substring = true)
            .assertContentDescriptionContains("Edit box", substring = true)
    }

    @Test
    fun testSdkTextField_ErrorState() {
        val error = "Invalid input"

        composeTestRule.setContent {
            SdkTextField(
                value = "John",
                onValueChange = { /* No-op */ },
                error = error,
                label = "Name"
            )
        }

        // In the error state the error is announced last and the contextual extras are suppressed.
        composeTestRule.onNodeWithTag("sdkInput")
            .assertContentDescriptionContains("Error: $error", substring = true)
    }

    @Test
    fun testSdkTextField_DisabledState() {
        composeTestRule.setContent {
            SdkTextField(
                value = "John Doe",
                onValueChange = { /* No-op */ },
                enabled = false,
                label = "Name"
            )
        }

        // A disabled field reports its disabled state to screen readers.
        composeTestRule.onNodeWithTag("sdkInput")
            .assert(hasStateDescription("Disabled"))
    }
}
