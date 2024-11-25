package com.paydock

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paydock.ThemeColors.ThemeColor
import com.paydock.designsystems.theme.SdkBackgroundDark
import com.paydock.designsystems.theme.SdkBackgroundLight
import com.paydock.designsystems.theme.SdkErrorDark
import com.paydock.designsystems.theme.SdkErrorLight
import com.paydock.designsystems.theme.SdkOnPrimaryDark
import com.paydock.designsystems.theme.SdkOnPrimaryLight
import com.paydock.designsystems.theme.SdkOutlineDark
import com.paydock.designsystems.theme.SdkOutlineLight
import com.paydock.designsystems.theme.SdkPlaceholderDark
import com.paydock.designsystems.theme.SdkPlaceholderLight
import com.paydock.designsystems.theme.SdkPrimaryDark
import com.paydock.designsystems.theme.SdkPrimaryLight
import com.paydock.designsystems.theme.SdkSuccessDark
import com.paydock.designsystems.theme.SdkSuccessLight
import com.paydock.designsystems.theme.SdkTextDark
import com.paydock.designsystems.theme.SdkTextLight
import com.paydock.designsystems.theme.typography.AcidGroteskFontList
import com.paydock.designsystems.theme.typography.ArialFontList

/**
 * MobileSDKTheme is a customizable theme for the SDK, providing access to color, dimension, and font values
 * that are applied throughout the SDK UI components.
 *
 * @param colours The color theme containing both light and dark modes.
 * @param dimensions The dimension values for various UI elements (e.g., corner radii, spacing).
 * @param font The font theme specifying the font family to be used.
 */
class MobileSDKTheme(
    val colours: ThemeColors = Colours.themeColours(),
    val dimensions: ThemeDimensions = Dimensions.themeDimensions(),
    val font: ThemeFont = FontName.themeFont(),
) {
    /**
     * A computed property that returns the appropriate color theme based on the system's current theme mode.
     */
    val colorTheme: ThemeColor
        @Composable get() {
            return if (isDarkMode) colours.dark else colours.light
        }

    /**
     * A private computed property that checks whether the current theme mode is dark.
     */
    private val isDarkMode: Boolean
        @Composable get() = isSystemInDarkTheme()

    /**
     * Retrieves the light color theme.
     *
     * @return The [ThemeColor] instance representing the light mode colors.
     */
    fun getLightColorTheme(): ThemeColor {
        return colours.light
    }

    /**
     * Retrieves the dark color theme.
     *
     * @return The [ThemeColor] instance representing the dark mode colors.
     */
    fun getDarkColorTheme(): ThemeColor {
        return colours.dark
    }

    override fun toString(): String {
        return "MobileSDkTheme(colours=$colours, dimensions=$dimensions, font=$font)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MobileSDKTheme

        if (colours != other.colours) return false
        if (dimensions != other.dimensions) return false
        return font == other.font
    }

    override fun hashCode(): Int {
        var result = colours.hashCode()
        result = 31 * result + dimensions.hashCode()
        result = 31 * result + font.hashCode()
        return result
    }

    /**
     * Colours object contains factory methods for creating theme color sets.
     */
    object Colours {
        /**
         * Creates a [ThemeColors] instance with specified light and dark color themes.
         *
         * @param light The [ThemeColor] instance for light mode colors.
         * @param dark The [ThemeColor] instance for dark mode colors.
         * @return A [ThemeColors] instance containing both light and dark color themes.
         */
        fun themeColours(
            light: ThemeColor = lightThemeColors(),
            dark: ThemeColor = darkThemeColors(),
        ): ThemeColors = ThemeColors(light, dark)

        /**
         * Creates a [ThemeColor] instance for the light mode color theme.
         *
         * @return A [ThemeColor] instance containing color values for light mode.
         */
        fun lightThemeColors(
            primary: Color = SdkPrimaryLight,
            onPrimary: Color = SdkOnPrimaryLight,
            text: Color = SdkTextLight,
            placeholder: Color = SdkPlaceholderLight,
            success: Color = SdkSuccessLight,
            error: Color = SdkErrorLight,
            background: Color = SdkBackgroundLight,
            outline: Color = SdkOutlineLight,
        ): ThemeColor = ThemeColor(
            primary = primary,
            onPrimary = onPrimary,
            text = text,
            placeholder = placeholder,
            success = success,
            error = error,
            background = background,
            outline = outline
        )

        /**
         * Creates a [ThemeColor] instance for the dark mode color theme.
         *
         * @return A [ThemeColor] instance containing color values for dark mode.
         */
        fun darkThemeColors(
            primary: Color = SdkPrimaryDark,
            onPrimary: Color = SdkOnPrimaryDark,
            text: Color = SdkTextDark,
            placeholder: Color = SdkPlaceholderDark,
            success: Color = SdkSuccessDark,
            error: Color = SdkErrorDark,
            background: Color = SdkBackgroundDark,
            outline: Color = SdkOutlineDark,
        ): ThemeColor = ThemeColor(
            primary = primary,
            onPrimary = onPrimary,
            text = text,
            placeholder = placeholder,
            success = success,
            error = error,
            background = background,
            outline = outline
        )
    }

    /**
     * Dimensions object contains factory methods for creating dimension values for various UI elements.
     */
    object Dimensions {
        /**
         * Creates a [ThemeDimensions] instance containing dimension values for UI elements.
         *
         * @param textFieldCornerRadius The corner radius for text fields.
         * @param buttonCornerRadius The corner radius for buttons.
         * @param shadow The shadow size.
         * @param borderWidth The border width.
         * @param spacing The default spacing.
         * @return A [ThemeDimensions] instance with the specified dimension values.
         */
        fun themeDimensions(
            textFieldCornerRadius: Int = 4,
            buttonCornerRadius: Int = 4,
            shadow: Int = 0,
            borderWidth: Int = 1,
            spacing: Int = 16,
        ): ThemeDimensions = ThemeDimensions(
            textFieldCornerRadius = textFieldCornerRadius.dp,
            buttonCornerRadius = buttonCornerRadius.dp,
            shadow = shadow.dp,
            borderWidth = borderWidth.dp,
            spacing = spacing.dp
        )
    }

    /**
     * FontName object contains a method for creating a theme font configuration.
     */
    object FontName {
        /**
         * Creates a [ThemeFont] instance specifying a list of fonts to be used within the theme.
         *
         * @param fonts A list of [Font] instances representing the font family.
         * @return A [ThemeFont] instance with the specified fonts.
         */
        fun themeFont(fonts: List<Font> = AcidGroteskFontList + ArialFontList): ThemeFont =
            ThemeFont(
                familyName = FontFamily(fonts)
            )
    }
}

/**
 * Represents a set of colors for different UI components in the theme.
 *
 * @param light The light mode color configuration.
 * @param dark The dark mode color configuration.
 */
@Immutable
class ThemeColors(
    val light: ThemeColor,
    val dark: ThemeColor,
) {

    /**
     * Represents a color set for UI components.
     *
     * @param primary The primary color.
     * @param onPrimary The color for elements placed on top of the primary color.
     * @param text The text color.
     * @param placeholder The placeholder text color.
     * @param success The success state color.
     * @param error The error state color.
     * @param background The background color.
     * @param outline The outline color.
     */
    @Suppress("LongParameterList")
    data class ThemeColor internal constructor(
        val primary: Color,
        val onPrimary: Color,
        val text: Color,
        val placeholder: Color,
        val success: Color,
        val error: Color,
        val background: Color,
        val outline: Color,
    ) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ThemeColor

            if (primary != other.primary) return false
            if (onPrimary != other.onPrimary) return false
            if (text != other.text) return false
            if (placeholder != other.placeholder) return false
            if (success != other.success) return false
            if (error != other.error) return false
            if (background != other.background) return false
            return outline == other.outline
        }

        override fun hashCode(): Int {
            var result = primary.hashCode()
            result = 31 * result + onPrimary.hashCode()
            result = 31 * result + text.hashCode()
            result = 31 * result + placeholder.hashCode()
            result = 31 * result + success.hashCode()
            result = 31 * result + error.hashCode()
            result = 31 * result + background.hashCode()
            result = 31 * result + outline.hashCode()
            return result
        }

        @Suppress("MaxLineLength")
        override fun toString(): String {
            return "ThemeColor(primary=$primary, onPrimary=$onPrimary, text=$text, placeholder=$placeholder, success=$success, error=$error, background=$background, outline=$outline)"
        }
    }
}

/**
 * Represents a set of dimension values for various UI elements in the theme.
 *
 * @param textFieldCornerRadius The corner radius for text fields.
 * @param buttonCornerRadius The corner radius for buttons.
 * @param shadow The shadow size.
 * @param borderWidth The border width.
 * @param spacing The default spacing value.
 */
@Immutable
data class ThemeDimensions internal constructor(
    val textFieldCornerRadius: Dp,
    val buttonCornerRadius: Dp,
    val shadow: Dp,
    val borderWidth: Dp,
    val spacing: Dp,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ThemeDimensions

        if (textFieldCornerRadius != other.textFieldCornerRadius) return false
        if (buttonCornerRadius != other.buttonCornerRadius) return false
        if (shadow != other.shadow) return false
        if (borderWidth != other.borderWidth) return false
        return spacing == other.spacing
    }

    override fun hashCode(): Int {
        var result = textFieldCornerRadius.hashCode()
        result = 31 * result + buttonCornerRadius.hashCode()
        result = 31 * result + shadow.hashCode()
        result = 31 * result + borderWidth.hashCode()
        result = 31 * result + spacing.hashCode()
        return result
    }

    @Suppress("MaxLineLength")
    override fun toString(): String {
        return "ThemeDimensions(textFieldCornerRadius=$textFieldCornerRadius, buttonCornerRadius=$buttonCornerRadius, shadow=$shadow, borderWidth=$borderWidth, spacing=$spacing)"
    }
}

/**
 * Represents a font configuration to be applied in the theme.
 *
 * @param familyName The font family to use.
 */
@Immutable
data class ThemeFont internal constructor(
    val familyName: FontFamily,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ThemeFont

        return familyName == other.familyName
    }

    override fun hashCode(): Int {
        return familyName.hashCode()
    }

    override fun toString(): String {
        return "ThemeFont(familyName=$familyName)"
    }
}