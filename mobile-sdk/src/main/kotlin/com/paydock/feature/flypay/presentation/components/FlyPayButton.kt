package com.paydock.feature.flypay.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.R
import com.paydock.core.presentation.extensions.alpha40
import com.paydock.core.presentation.extensions.scaled
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.LoaderAppearanceDefaults
import com.paydock.designsystems.components.loader.SdkButtonLoader
import com.paydock.designsystems.components.text.SdkText
import com.paydock.designsystems.components.text.TextAppearanceDefaults
import com.paydock.designsystems.theme.ArialFontFamily
import com.paydock.designsystems.theme.FlyPayBlue

/**
 * A composable function that displays a customizable FlyPay button.
 *
 * This button is designed to initiate a FlyPay transaction. It includes text, an icon, and an optional
 * loading state. The button's state (enabled/disabled) is controlled by the provided parameters,
 * and click events are handled by the `onClick` lambda.
 *
 * @param shape The shape of the button. Defaults to [MaterialTheme.shapes.small].
 * @param loaderAppearance The appearance of the loader when the button is in a loading state.
 * Defaults to [LoaderAppearanceDefaults.appearance()].
 * @param isEnabled Controls whether the button is enabled or disabled. Defaults to `true`.
 * @param isLoading Determines whether to show a loading spinner inside the button. Defaults to `false`.
 * @param onClick The callback to be invoked when the button is clicked.
 */
@Composable
internal fun FlyPayButton(
    shape: Shape = MaterialTheme.shapes.small,
    loaderAppearance: LoaderAppearance = LoaderAppearanceDefaults.appearance(),
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale
    // Calculate adjusted height using the extension function
    val adjustedButtonHeight = ButtonAppearanceDefaults.ButtonHeight.scaled(fontScale)

    // Calculate the adjusted image size based on the font scale and a base image size.
    val baseImageSize = ButtonAppearanceDefaults.ButtonHeight
    val adjustedImageSize = baseImageSize.value.scaled(fontScale).dp
    // Button to initiate FlyPay transaction
    Button(

        onClick = onClick,
        modifier = Modifier
            .testTag("flypayButton")
            .fillMaxWidth()
            .height(adjustedButtonHeight),
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = FlyPayBlue,
            disabledContainerColor = FlyPayBlue.alpha40
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
            // Show a progress indicator if the FlyPay wallet is loading
            if (isLoading) {
                SdkButtonLoader(appearance = loaderAppearance)
            } else {
                // Display button text
                SdkText(
                    modifier = Modifier.fillMaxHeight(),
                    text = stringResource(R.string.button_pay_with),
                    appearance = TextAppearanceDefaults.appearance().copy(
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = ArialFontFamily,
                            fontSize = 22.sp,
                            lineHeight = 48.sp,
                            color = Color.White,
                            platformStyle = PlatformTextStyle(includeFontPadding = true)
                        )
                    )
                )
                // Display FlyPay icon
                Image(
                    modifier = Modifier.size(adjustedImageSize),
                    painter = painterResource(id = R.drawable.ic_flypay_button),
                    contentDescription = stringResource(id = R.string.content_desc_flypay_button_icon)
                )
            }
        }
    }
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewFlyPayButton() {
    FlyPayButton(isEnabled = true, isLoading = false) {}
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewFlyPayButtonDisabled() {
    FlyPayButton(isEnabled = false, isLoading = false) {}
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewFlyPayButtonLoading() {
    FlyPayButton(isLoading = true) {}
}
