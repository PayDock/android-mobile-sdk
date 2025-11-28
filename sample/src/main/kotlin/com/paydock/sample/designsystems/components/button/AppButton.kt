package com.paydock.sample.designsystems.components.button

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.theme.SampleTheme

enum class AppButtonVariant { Filled, Outline }
enum class AppButtonShape { Rectangle, Pill }

@Composable
internal fun AppButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    variant: AppButtonVariant = AppButtonVariant.Filled,
    shape: AppButtonShape = AppButtonShape.Rectangle,
    height: Dp = 48.dp,
    leadingIcon: (@Composable (() -> Unit))? = null,
    containerColor: Color? = null,
    contentColor: Color? = null,
    onClick: () -> Unit,
) {
    val targetShape = when (shape) {
        AppButtonShape.Rectangle -> MaterialTheme.shapes.extraSmall
        AppButtonShape.Pill -> RoundedCornerShape(50)
    }

    // Calculate font-scaled height like SdkButton does
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale

    // Only apply height constraint if specified
    val buttonModifier = if (height != Dp.Unspecified) {
        // Scale height based on font scale
        val scaledHeight = height * fontScale
        modifier.heightIn(min = scaledHeight)
    } else {
        modifier
    }

    when (variant) {
        AppButtonVariant.Filled -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !isLoading,
            shape = targetShape,
            colors = if (containerColor != null || contentColor != null) ButtonDefaults.buttonColors(
                containerColor = containerColor ?: MaterialTheme.colorScheme.primary,
                contentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary
            ) else ButtonDefaults.buttonColors()
        ) { ButtonContent(text = text, isLoading = isLoading, leadingIcon = leadingIcon) }

        AppButtonVariant.Outline -> OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !isLoading,
            shape = targetShape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) { ButtonContent(text = text, isLoading = isLoading, leadingIcon = leadingIcon) }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean,
    leadingIcon: (@Composable (() -> Unit))?
) {
    Crossfade(targetState = isLoading, label = "coss_fade") {
        if (it) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 1.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = text,
                    softWrap = true,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
@PreviewLightDark
internal fun PreviewButtonPrimary() {
    SampleTheme {
        AppButton(
            text = "Primary",
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
@PreviewLightDark
internal fun PreviewButtonPrimaryWithLoading() {
    SampleTheme() {
        AppButton(
            text = "Primary",
            onClick = {},
            isLoading = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
@PreviewLightDark
internal fun PreviewButtonFilledRectangle() {
    SampleTheme {
        AppButton(
            text = "Filled Rectangle",
            variant = AppButtonVariant.Filled,
            shape = AppButtonShape.Rectangle,
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
@PreviewLightDark
internal fun PreviewButtonFilledPill() {
    SampleTheme {
        AppButton(
            text = "Filled Pill",
            variant = AppButtonVariant.Filled,
            shape = AppButtonShape.Pill,
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
@PreviewLightDark
internal fun PreviewButtonOutlineRectangle() {
    SampleTheme {
        AppButton(
            text = "Outline Rectangle",
            variant = AppButtonVariant.Outline,
            shape = AppButtonShape.Rectangle,
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
@PreviewLightDark
internal fun PreviewButtonOutlinePill() {
    SampleTheme {
        AppButton(
            text = "Outline Pill",
            variant = AppButtonVariant.Outline,
            shape = AppButtonShape.Pill,
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
