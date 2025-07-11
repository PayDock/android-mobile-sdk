package com.paydock.feature.address.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.search.SearchDropdownAppearance
import com.paydock.feature.address.presentation.state.AddressDetailsFormState

/**
 * Displays a manual address entry form with an animated slide-down effect when visible.
 *
 * @param isManualAddressVisible Controls the visibility of the manual address entry form.
 * When `true`, the form is displayed with a slide-down animation; when `false`, it is hidden.
 * @param addressInputState The current state of the address form, containing input values and validation status.
 * @param onAddressLine1Change Callback invoked when the address line 1 input changes.
 * @param onAddressLine2Change Callback invoked when the address line 2 input changes.
 * @param onCityChange Callback invoked when the city input changes.
 * @param onStateChange Callback invoked when the state input changes.
 * @param onPostalCodeChange Callback invoked when the postal code input changes.
 * @param onCountryChange Callback invoked when the country input changes.
 * @param verticalSpacing The vertical spacing between form elements.
 * @param textFieldAppearance The appearance configuration for the text fields.
 * @param searchAppearance The appearance configuration for the search dropdown (used for country selection).
 *
 * This function uses [AnimatedVisibility] to handle the visibility and animation of the
 * manual address entry form. The animations include:
 * - Slide-down effect from the top ([expandVertically]).
 * - Fade-in effect ([fadeIn]) with an initial alpha of 0.3.
 *
 * Note:
 * - The animation duration is determined by [MobileSDKConstants.General.EXPANSION_TRANSITION_DURATION].
 * - The [ManualAddress] composable handles the input fields for address entry and passes updates via the respective callbacks.
 */
@Composable
internal fun ManualAddressEntry(
    isManualAddressVisible: Boolean,
    addressInputState: AddressDetailsFormState,
    onAddressLine1Change: (String) -> Unit,
    onAddressLine2Change: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onStateChange: (String) -> Unit,
    onPostalCodeChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    verticalSpacing: Dp,
    textFieldAppearance: TextFieldAppearance,
    searchAppearance: SearchDropdownAppearance,
) {
    // Slide-down animation for the ManualAddress component
    AnimatedVisibility(
        visible = isManualAddressVisible,
        enter = expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = tween(MobileSDKConstants.General.EXPANSION_TRANSITION_DURATION)
        ) + fadeIn(
            initialAlpha = 0.3f,
            animationSpec = tween(MobileSDKConstants.General.EXPANSION_TRANSITION_DURATION)
        )
    ) {
        ManualAddress(
            addressInputState = addressInputState,
            onAddressLine1Change = onAddressLine1Change,
            onAddressLine2Change = onAddressLine2Change,
            onCityChange = onCityChange,
            onStateChange = onStateChange,
            onPostalCodeChange = onPostalCodeChange,
            onCountryChange = onCountryChange,
            verticalSpacing = verticalSpacing,
            textFieldAppearance = textFieldAppearance,
            searchAppearance = searchAppearance
        )
    }
}
