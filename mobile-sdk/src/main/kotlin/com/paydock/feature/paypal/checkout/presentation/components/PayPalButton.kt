package com.paydock.feature.paypal.checkout.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.core.presentation.extensions.alpha40
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.LoaderAppearanceDefaults
import com.paydock.designsystems.components.loader.SdkButtonLoader
import com.paydock.designsystems.theme.PayPal

/**
 * A composable function that displays a customizable PayPal button.
 *
 * This button can be used to initiate a PayPal transaction. It includes an icon and an optional
 * loading state. The button is enabled or disabled based on the provided parameters and handles the
 * click event via the `onClick` lambda.
 *
 * @param isEnabled Controls whether the button is enabled or disabled.
 * @param isLoading Determines whether to show a loading spinner inside the button, indicating that
 * a PayPal transaction is in progress.
 * @param onClick The callback to be invoked when the button is clicked.
 */
@Composable
internal fun PayPalButton(
    shape: Shape = MaterialTheme.shapes.small,
    loaderAppearance: LoaderAppearance = LoaderAppearanceDefaults.appearance(),
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .testTag("payPalButton")
            .fillMaxWidth()
            .height(ButtonAppearanceDefaults.ButtonHeight),
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = PayPal,
            disabledContainerColor = PayPal.alpha40
        ),
        shape = shape
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                ButtonAppearanceDefaults.ButtonSpacing,
                Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Display PayPal icon
            Image(
                painter = painterResource(id = R.drawable.ic_paypal_button),
                contentDescription = stringResource(id = R.string.content_desc_paypal_button_icon)
            )
            // Show a progress indicator if the PayPal wallet is loading
            if (isLoading) {
                SdkButtonLoader(loaderAppearance)
            }
        }
    }
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewPayPalButtonDefault() {
    PayPalButton {}
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewPayPalButtonDisabled() {
    PayPalButton(isEnabled = false, isLoading = false) {}
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewPayPalButtonLoading() {
    PayPalButton(isLoading = true) {}
}