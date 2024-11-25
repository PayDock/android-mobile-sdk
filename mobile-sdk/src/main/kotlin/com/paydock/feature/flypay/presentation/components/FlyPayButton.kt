package com.paydock.feature.flypay.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.unit.sp
import com.paydock.R
import com.paydock.core.presentation.extensions.alpha40
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.components.loader.SdkButtonLoader
import com.paydock.designsystems.theme.FlyPayBlue
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.designsystems.theme.typography.ArialFontFamily

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
    // Button to initiate FlyPay transaction
    Button(
        onClick = onClick,
        modifier = Modifier
            .testTag("flypayButton")
            .fillMaxWidth()
            .height(Theme.dimensions.buttonHeight),
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = FlyPayBlue,
            disabledContainerColor = FlyPayBlue.alpha40
        ),
        shape = Theme.buttonShapes.small
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                Theme.dimensions.buttonSpacing,
                Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Display button text
            Text(
                modifier = Modifier.fillMaxHeight(),
                text = stringResource(R.string.button_pay_with),
                style = Theme.typography.button.copy(
                    fontFamily = ArialFontFamily,
                    fontSize = 22.sp,
                    lineHeight = 48.sp,
                    color = Color.White,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
            // Display FlyPay icon
            Image(
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

@LightDarkPreview
@Composable
private fun PreviewFlyPayButton() {
    SdkTheme {
        FlyPayButton(isEnabled = true, isLoading = false) {}
    }
}
