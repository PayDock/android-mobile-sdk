package com.paydock.core.extensions

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillTree
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree

/**
 * A custom modifier that enables autofill support for a Compose component.
 *
 * @param autofillTypes A list of [AutofillType] indicating the types of data that can be autofilled (e.g., passwords, emails).
 * @param onFill A callback function invoked when an autofill value is provided. The callback receives the autofilled string.
 * @return A [Modifier] that provides autofill support for the component it is applied to.
 *
 * This modifier handles autofill by:
 * - Registering an [AutofillNode] with the local [AutofillTree].
 * - Setting the bounding box of the autofillable area.
 * - Requesting autofill when the component gains focus.
 * - Cancelling autofill when the component loses focus.
 */
@OptIn(ExperimentalComposeUiApi::class)
internal fun Modifier.autofill(
    autofillTypes: List<AutofillType>,
    onFill: ((String) -> Unit),
) = composed {
    // Retrieve the current autofill manager from the local context
    val autofill = LocalAutofill.current

    // Create an AutofillNode that represents this component's autofillable area
    val autofillNode = AutofillNode(onFill = onFill, autofillTypes = autofillTypes)

    // Register the AutofillNode with the current AutofillTree to enable autofill
    LocalAutofillTree.current += autofillNode

    // Apply modifier to handle autofill events and manage the AutofillNode's position
    this
        .onGloballyPositioned {
            // Update the AutofillNode's bounding box whenever the component's position changes
            autofillNode.boundingBox = it.boundsInWindow()
        }
        .onFocusChanged { focusState ->
            // Handle focus changes to request or cancel autofill
            autofill?.run {
                if (focusState.isFocused) {
                    // Request autofill when the component gains focus
                    requestAutofillForNode(autofillNode)
                } else {
                    // Cancel autofill when the component loses focus
                    cancelAutofillForNode(autofillNode)
                }
            }
        }
}