package com.paydock.feature.card.presentation.utils.style

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

// /**
// * Object to represent the overall styling options for the Sdk Text Field.
// */
object SdkTextFieldDefaults {

    val MinHeight = OutlinedTextFieldDefaults.MinHeight
    val MinWidth = OutlinedTextFieldDefaults.MinWidth
    val UnfocusedBorderThickness = OutlinedTextFieldDefaults.UnfocusedBorderThickness
    val FocusedBorderThickness = OutlinedTextFieldDefaults.FocusedBorderThickness
    val ContentPadding: PaddingValues = contentPadding()
    val shape: Shape
        @Composable get() = OutlinedTextFieldDefaults.shape

    /**
     * Composable function to provide default styles for the SdkTextFieldColors.
     *
     * @param backgroundColor The background color of the TextField.
     * @param textColor The color of the text in the TextField.
     * @param textStyle The style of the text in the TextField.
     * @param shape The shape of the TextField.
     * @param padding The padding around the TextField content.
     * @param minWidth The minimum width applied for all TextFields.
     * @param minHeight The minimum height applied for all TextFields.
     */
    @Composable
    fun textFieldDefaults(
        colors: OutlinedTextFieldDefaults = OutlinedTextFieldDefaults,
        shape: Shape = OutlinedTextFieldDefaults.shape,
        focusedBorderThickness: Dp = FocusedBorderThickness,
        unfocusedBorderThickness: Dp = UnfocusedBorderThickness,
    ): SdkTextFieldDefaultStyle = SdkTextFieldDefaultStyle(
        colors, shape, focusedBorderThickness, unfocusedBorderThickness
    )
}

@Immutable
class SdkTextFieldDefaultStyle(
    val colors: OutlinedTextFieldDefaults,
    val shape: Shape,
    val focusedBorderThickness: Dp,
    val unfocusedBorderThickness: Dp
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SdkTextFieldDefaultStyle

        if (colors != other.colors) return false
        if (shape != other.shape) return false
        if (focusedBorderThickness != other.focusedBorderThickness) return false
        if (unfocusedBorderThickness != other.unfocusedBorderThickness) return false

        return true
    }

    override fun hashCode(): Int {
        var result = colors.hashCode()
        result = 31 * result + shape.hashCode()
        result = 31 * result + focusedBorderThickness.hashCode()
        result = 31 * result + unfocusedBorderThickness.hashCode()
        return result
    }
}

/**
 * Object to represent the overall styling options for the Sdk Button.
 */
object SdkButtonDefaults {

    val MinWidth: Dp = ButtonDefaults.MinWidth
    val MinHeight: Dp = ButtonDefaults.MinWidth
    val ContentPadding: PaddingValues = ButtonDefaults.ContentPadding
    val shape: Shape @Composable get() = ButtonDefaults.shape
    val colors: ButtonColors @Composable get() = ButtonDefaults.buttonColors()

    @Composable
    fun buttonDefaults(
        shape: Shape = ButtonDefaults.shape,
        colors: ButtonColors = ButtonDefaults.buttonColors(),
        elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
        border: BorderStroke? = null,
        contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    ): SdkButtonDefaultStyle = SdkButtonDefaultStyle(
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding
    )

}

@Immutable
class SdkButtonDefaultStyle(
    val shape: Shape,
    val colors: ButtonColors,
    val elevation: ButtonElevation?,
    val border: BorderStroke?,
    val contentPadding: PaddingValues
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SdkButtonDefaultStyle

        if (shape != other.shape) return false
        if (colors != other.colors) return false
        if (elevation != other.elevation) return false
        if (border != other.border) return false
        if (contentPadding != other.contentPadding) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shape.hashCode()
        result = 31 * result + colors.hashCode()
        result = 31 * result + (elevation?.hashCode() ?: 0)
        result = 31 * result + (border?.hashCode() ?: 0)
        result = 31 * result + contentPadding.hashCode()
        return result
    }
}

/**
 * Data class to hold GiftCardTextFieldColors and GiftCardButtonColors.
 */
data class GiftCardStyle(
    val textFieldDefaults: SdkTextFieldDefaultStyle,
    val buttonDefaults: SdkButtonDefaultStyle
)

/**
 * Function to retrieve the current GiftCardWidgetStyle from the composition.
 *
 * @return The current GiftCardWidgetStyle.
 */
@Composable
fun rememberGiftCardWidgetColors(
    textFieldDefaults: SdkTextFieldDefaultStyle = SdkTextFieldDefaults.textFieldDefaults(),
    buttonDefaults: SdkButtonDefaultStyle = SdkButtonDefaults.buttonDefaults()
): GiftCardStyle {
    return remember(textFieldDefaults, buttonDefaults) {
        GiftCardStyle(textFieldDefaults, buttonDefaults)
    }
}