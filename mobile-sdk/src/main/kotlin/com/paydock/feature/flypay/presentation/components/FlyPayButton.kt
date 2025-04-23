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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.R
import com.paydock.core.presentation.extensions.alpha40
import com.paydock.core.presentation.extensions.scaled
import com.paydock.designsystems.components.loader.SdkButtonLoader
import com.paydock.designsystems.theme.ArialFontFamily
import com.paydock.designsystems.theme.BUTTON_HEIGHT
import com.paydock.designsystems.theme.BUTTON_SPACING
import com.paydock.designsystems.theme.FlyPayBlue

/**
 * A composable function that displays a customizable FlyPay button.
 *
 * This button can be used to initiate a FlyPay transaction. It includes text, an icon, and an optional
 * loading state. The button is enabled or disabled based on the provided parameters and handles the
 * click event via the `onClick` lambda.
 *
 * @param onClick The callback to be invoked when the button is clicked.
 * @param isEnabled Controls whether the button is enabled or disabled.
 * @param isLoading Determines whether to show a loading spinner inside the button, indicating that
 * a FlyPay transaction is in progress.
 */
@Composable
internal fun FlyPayButton(
    isEnabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale
    // Calculate adjusted height using the extension function
    val adjustedButtonHeight = BUTTON_HEIGHT.scaled(fontScale)

    // Calculate the adjusted image size based on the font scale and a base image size.
    val baseImageSize = BUTTON_HEIGHT
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
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                BUTTON_SPACING,
                Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Display button text
            Text(
                modifier = Modifier.fillMaxHeight(),
                text = stringResource(R.string.button_pay_with),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = ArialFontFamily,
                    fontSize = 22.sp,
                    lineHeight = 48.sp,
                    color = Color.White,
                    platformStyle = PlatformTextStyle(includeFontPadding = true)
                )
            )
            // Display FlyPay icon
            Image(
                modifier = Modifier.size(adjustedImageSize),
                painter = painterResource(id = R.drawable.ic_flypay_button),
                contentDescription = stringResource(id = R.string.content_desc_flypay_button_icon)
            )
            // Show a progress indicator if the FlyPay wallet is loading
            if (isLoading) {
                SdkButtonLoader()
            }
        }
    }
}

@PreviewLightDark
@Composable
internal fun PreviewFlyPayButton() {
    FlyPayButton(isEnabled = true, isLoading = false) {}
}
