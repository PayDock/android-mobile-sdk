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
 * MobileSDKTheme is a customizable theme for the SDK that provides color, dimension, and font values.
 *
 * @param colours The color theme containing light and dark mode.
 * @param dimensions The dimension values for various UI elements.
 * @param font The font theme containing the font family to be used within the theme.
 */
class MobileSDKTheme(
    val colours: ThemeColors = Colours.themeColours(),
    val dimensions: ThemeDimensions = Dimensions.themeDimensions(),
    val font: ThemeFont = FontName.themeFont()
) {
    /**
     * A computed property that returns the appropriate color theme based on the current mode.
     */
    val colorTheme: ThemeColor
        @Composable
        get() {
            return if (isDarkMode) colours.dark else colours.light
        }

    /**
     * A private computed property that checks whether the current theme mode is dark.
     */
    private val isDarkMode: Boolean
        @Composable
        get() = isSystemInDarkTheme()

    /**
     * Retrieve the color theme for light mode.
     */
    fun getLightColorTheme(): ThemeColor {
        return colours.light
    }

    /**
     * Retrieve the color theme for dark mode.
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

    object Colours {
        fun themeColours(
            light: ThemeColor = lightThemeColors(),
            dark: ThemeColor = darkThemeColors()
        ): ThemeColors = ThemeColors(light, dark)

        fun lightThemeColors(
            primary: Color = SdkPrimaryLight,
            onPrimary: Color = SdkOnPrimaryLight,
            text: Color = SdkTextLight,
            placeholder: Color = SdkPlaceholderLight,
            success: Color = SdkSuccessLight,
            error: Color = SdkErrorLight,
            background: Color = SdkBackgroundLight,
            outline: Color = SdkOutlineLight
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

        fun darkThemeColors(
            primary: Color = SdkPrimaryDark,
            onPrimary: Color = SdkOnPrimaryDark,
            text: Color = SdkTextDark,
            placeholder: Color = SdkPlaceholderDark,
            success: Color = SdkSuccessDark,
            error: Color = SdkErrorDark,
            background: Color = SdkBackgroundDark,
            outline: Color = SdkOutlineDark
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
     * ThemeDimensions represents a set of dimension values for various UI elements in the theme.
     */
    object Dimensions {
        fun themeDimensions(
            cornerRadius: Int = 4,
            shadow: Int = 0,
            borderWidth: Int = 1,
            spacing: Int = 16
        ): ThemeDimensions = ThemeDimensions(
            cornerRadius = cornerRadius.dp,
            shadow = shadow.dp,
            borderWidth = borderWidth.dp,
            spacing = spacing.dp
        )
    }

    object FontName {
        fun themeFont(fonts: List<Font> = AcidGroteskFontList + ArialFontList): ThemeFont =
            ThemeFont(
                familyName = FontFamily(fonts)
            )

    }
}

/**
 * ThemeColors represents a set of color values for different UI components in the theme.
 */
@Immutable
class ThemeColors(
    val light: ThemeColor,
    val dark: ThemeColor
) {

    @Suppress("LongParameterList")
    data class ThemeColor internal constructor(
        val primary: Color,
        val onPrimary: Color,
        val text: Color,
        val placeholder: Color,
        val success: Color,
        val error: Color,
        val background: Color,
        val outline: Color
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

        override fun toString(): String {
            return "ThemeColor(primary=$primary, " +
                "onPrimary=$onPrimary, " +
                "text=$text, " +
                "placeholder=$placeholder," +
                "success=$success, " +
                "error=$error, " +
                "background=$background," +
                "outline=$outline)"
        }
    }
}

/**
 * ThemeDimensions represents a set of dimension values for various UI elements in the theme.
 */
@Immutable
data class ThemeDimensions internal constructor(
    val cornerRadius: Dp,
    val shadow: Dp,
    val borderWidth: Dp,
    val spacing: Dp
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ThemeDimensions

        if (cornerRadius != other.cornerRadius) return false
        if (shadow != other.shadow) return false
        if (borderWidth != other.borderWidth) return false
        return spacing == other.spacing
    }

    override fun hashCode(): Int {
        var result = cornerRadius.hashCode()
        result = 31 * result + shadow.hashCode()
        result = 31 * result + borderWidth.hashCode()
        result = 31 * result + spacing.hashCode()
        return result
    }

    override fun toString(): String {
        return "ThemeDimensions(cornerRadius=$cornerRadius, shadow=$shadow, borderWidth=$borderWidth, spacing=$spacing)"
    }
}

/**
 * ThemeFont represents the font family to be used within the theme.
 */
@Immutable
data class ThemeFont internal constructor(
    val familyName: FontFamily

) {
    override fun toString(): String {
        return "ThemeFont(familyName=$familyName)"
    }
}