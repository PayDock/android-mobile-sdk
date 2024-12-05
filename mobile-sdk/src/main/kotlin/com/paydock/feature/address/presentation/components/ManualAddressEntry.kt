package com.paydock.feature.address.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.paydock.core.MobileSDKConstants
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.feature.address.presentation.state.AddressDetailsInputState

/**
 * Displays a manual address entry form with an animated slide-down effect when visible.
 *
 * @param isManualAddressVisible Controls the visibility of the manual address entry form.
 * When `true`, the form is displayed with a slide-down animation; when `false`, it is hidden.
 * @param address The initial [BillingAddress] to populate the form. Passed to the [ManualAddress] composable.
 * @param onAddressUpdated A callback invoked when the address details are updated.
 * The updated address details are provided as [AddressDetailsInputState].
 *
 * This function uses [AnimatedVisibility] to handle the visibility and animation of the
 * manual address entry form. The animations include:
 * - Slide-down effect from the top ([expandVertically]).
 * - Fade-in effect ([fadeIn]) with an initial alpha of 0.3.
 *
 * Example:
 * ```
 * ManualAddressEntry(
 *     isManualAddressVisible = true,
 *     address = currentAddress,
 *     onAddressUpdated = { updatedAddress ->
 *         println("Updated Address: $updatedAddress")
 *     }
 * )
 * ```
 *
 * Note:
 * - The animation duration is determined by [MobileSDKConstants.General.EXPANSION_TRANSITION_DURATION].
 * - The [ManualAddress] composable handles the input fields for address entry and passes updates via the callback.
 */
@Composable
internal fun ManualAddressEntry(
    isManualAddressVisible: Boolean,
    address: BillingAddress,
    onAddressUpdated: (AddressDetailsInputState) -> Unit,
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
            modifier = Modifier.testTag("manualAddress"),
            savedAddress = address,
            onAddressUpdated = onAddressUpdated
        )
    }
}
