package com.paydock.feature.paypal.checkout.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.core.presentation.extensions.alpha40
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.designsystems.components.button.SdkIconButton
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.LoaderAppearanceDefaults
import com.paydock.designsystems.theme.PayPal
import com.paydock.feature.paypal.vault.domain.model.integration.ButtonIcon

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
    SdkIconButton(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("payPalButton"),
        buttonIcon = ButtonIcon.DrawableRes(R.drawable.ic_paypal_button),
        contentDescription = stringResource(id = R.string.content_desc_paypal_button_icon),
        appearance = ButtonAppearanceDefaults.imageButtonAppearance().copy(
            colors = ButtonDefaults.buttonColors(
                containerColor = PayPal,
                disabledContainerColor = PayPal.alpha40
            ),
            shape = shape,
            loaderAppearance = loaderAppearance
        ),
        enabled = isEnabled,
        isLoading = isLoading,
        onClick = onClick
    )
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