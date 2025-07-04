@file:Suppress("TooManyFunctions")

package com.paydock.designsystems.components.button

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import com.paydock.R
import com.paydock.core.presentation.extensions.scaled
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.icon.IconAppearance
import com.paydock.designsystems.components.icon.IconAppearanceDefaults
import com.paydock.designsystems.components.icon.SdkIcon
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.LoaderAppearanceDefaults
import com.paydock.designsystems.components.loader.SdkButtonLoader
import com.paydock.designsystems.components.text.SdkText
import com.paydock.designsystems.components.text.TextAppearance
import com.paydock.designsystems.components.text.TextAppearanceDefaults
import com.paydock.feature.paypal.vault.domain.model.integration.ButtonIcon

/**
 * Renders a button based on the provided [ButtonAppearance].
 *
 * This function acts as a factory for different button types (Filled, Outline, Text, Image)
 * based on the specific implementation of the [ButtonAppearance] sealed class.
 *
 * @param modifier Modifier to be applied to the button.
 * @param text The text content of the button. Required for Filled, Outline, and Text appearances.
 * @param buttonIcon The icon to display on the button. Required for Image appearances, optional otherwise.
 * @param contentDescription The content description for the button icon, primarily for accessibility.
 * @param enabled Controls the enabled state of the button. Defaults to `true`.
 * @param isLoading Indicates if the button is in a loading state. If true, a loader is shown and
 * the button is disabled. Defaults to `false`.
 * @param onClick The action to perform when the button is clicked.
 * @throws IllegalArgumentException if the required content (text or icon) is missing for a specific button type.
 */
@Composable
internal fun ButtonAppearance.RenderButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    buttonIcon: ButtonIcon? = null,
    contentDescription: String? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    when (this) {
        is ButtonAppearance.FilledButtonAppearance -> {
            require(text != null) { "FilledButtonAppearance requires a text" }
            SdkButton(
                modifier = modifier,
                text = text,
                buttonIcon = buttonIcon,
                enabled = enabled,
                isLoading = isLoading,
                appearance = this,
                onClick = onClick
            )
        }
        // Apply similar logic for Outline and Text buttons
        is ButtonAppearance.OutlineButtonAppearance -> {
            require(text != null) { "OutlineButtonAppearance requires a text" }
            SdkOutlineButton(
                modifier = modifier,
                text = text,
                buttonIcon = buttonIcon,
                enabled = enabled,
                isLoading = isLoading,
                appearance = this,
                onClick = onClick
            )
        }
        is ButtonAppearance.TextButtonAppearance -> {
            require(text != null) { "TextButtonAppearance requires a text" }
            SdkTextButton(
                modifier = modifier,
                text = text,
                buttonIcon = buttonIcon,
                enabled = enabled,
                isLoading = isLoading,
                appearance = this,
                onClick = onClick
            )
        }
        is ButtonAppearance.IconButtonAppearance -> {
            require(buttonIcon != null) { "ImageButtonAppearance requires a text" }
            SdkIconButton(
                modifier = modifier,
                enabled = enabled,
                isLoading = isLoading,
                appearance = this,
                buttonIcon = buttonIcon,
                contentDescription = contentDescription,
                onClick = onClick
            )
        }
    }
}

/**
 * Represents the visual appearance of a button.
 *
 * This sealed class defines the common properties that dictate the style and
 * layout of a button, including its shape, colors, size, spacing, and the
 * appearance of its content (icon, text, and loader). Different types of
 * buttons (e.g., filled, outlined, text) can be represented by subclasses of
 * this class, allowing for consistent theming and styling.
 *
 * @property height The default height of the button.
 * @property contentSpacing The spacing between the button's content elements (e.g., icon and text).
 * @property shape The shape of the button's container.
 * @property colors The colors used to render the button's background and content.
 * @property elevation The elevation of the button, used to create a shadow effect.
 * @property border The border stroke applied to the button.
 * @property contentPadding The padding around the button's content.
 * @property iconAppearance The appearance of any icon displayed on the button.
 * @property textAppearance The appearance of the text displayed on the button.
 * @property loaderAppearance The appearance of a loader (e.g., a spinner) displayed on the button.
 */
sealed class ButtonAppearance(
    // Button Appearance
    open val height: Dp,
    open val contentSpacing: Dp,
    open val shape: Shape,
    open val colors: ButtonColors,
    open val elevation: ButtonElevation?,
    open val border: BorderStroke?,
    open val contentPadding: PaddingValues,
    // Content Appearance
    open val iconAppearance: IconAppearance?,
    open val textAppearance: TextAppearance?,
    open val loaderAppearance: LoaderAppearance,
) {

    /**
     * Defines the visual appearance of a filled button.
     *
     * This class encapsulates the styling attributes that control the look and feel of a button
     * that is filled with a solid color. It extends [ButtonAppearance] and provides concrete
     * values for properties such as shape, colors, elevation, border, content padding, icon
     * appearance, text appearance, and loader appearance.
     *
     * @property height The default height of the filled button.
     * @property contentSpacing The spacing between the icon (if any), text and loader (if any) within the button.
     * @property shape The shape of the filled button's background.
     * @property colors The colors used to paint different parts of the filled button.
     * @property elevation The elevation to be applied to the filled button. `null` means no elevation.
     * @property border The border to be drawn around the filled button. `null` means no border.
     * @property contentPadding The padding values for the button's content.
     * @property iconAppearance The appearance of the icon displayed within the button.
     * @property textAppearance The appearance of the text displayed within the button.
     * @property loaderAppearance The appearance of the loader displayed within the button during a loading state.
     */
    @Immutable
    data class FilledButtonAppearance(
        override val height: Dp,
        override val contentSpacing: Dp,
        override val shape: Shape,
        override val colors: ButtonColors,
        override val elevation: ButtonElevation?,
        override val border: BorderStroke?,
        override val contentPadding: PaddingValues,
        override val iconAppearance: IconAppearance,
        override val textAppearance: TextAppearance,
        override val loaderAppearance: LoaderAppearance,
    ) : ButtonAppearance(
        height,
        contentSpacing,
        shape,
        colors,
        elevation,
        border?.copy(),
        contentPadding,
        iconAppearance,
        textAppearance,
        loaderAppearance
    ) {
        fun copy(
            height: Dp = this.height,
            contentSpacing: Dp = this.contentSpacing,
            shape: Shape = this.shape,
            colors: ButtonColors = this.colors,
            elevation: ButtonElevation? = this.elevation,
            border: BorderStroke? = this.border,
            contentPadding: PaddingValues = this.contentPadding
        ): FilledButtonAppearance = FilledButtonAppearance(
            height = height.takeOrElse { this.height },
            contentSpacing = contentSpacing.takeOrElse { this.contentSpacing },
            shape = shape,
            colors = colors.copy(),
            elevation = elevation,
            border = border,
            contentPadding = contentPadding,
            iconAppearance = iconAppearance.copy(),
            textAppearance = textAppearance.copy(),
            loaderAppearance = loaderAppearance.copy()
        )
    }

    /**
     * Represents the appearance configuration for an outlined button.
     *
     * This class defines the visual properties of an outlined button, such as its height,
     * spacing, shape, colors, elevation, border, padding, icon appearance, text appearance, and loader appearance.
     *
     * It extends [ButtonAppearance] and provides specific default values suitable for outlined button styles.
     *
     * @property height The default height of the outlined button.
     * @property contentSpacing The spacing between the button's content elements (icon, text).
     * @property shape The shape of the button's background.
     * @property colors The color configuration for different button states (e.g., enabled, disabled, pressed).
     * @property elevation The elevation configuration for different button states. Can be null for no elevation.
     * @property border The border configuration for the outlined button. Can be null for no border.
     * @property contentPadding The padding between the button's border and its content.
     * @property iconAppearance The appearance configuration for any icons displayed within the button.
     * @property textAppearance The appearance configuration for the text displayed within the button.
     * @property loaderAppearance The appearance configuration for any loader/spinner displayed within
     * the button (e.g., during loading states).
     *
     * @see ButtonAppearance
     * @see androidx.compose.material3.OutlinedButton
     */
    @Immutable
    data class OutlineButtonAppearance(
        override val height: Dp,
        override val contentSpacing: Dp,
        override val shape: Shape,
        override val colors: ButtonColors,
        override val elevation: ButtonElevation?,
        override val border: BorderStroke?,
        override val contentPadding: PaddingValues,
        override val iconAppearance: IconAppearance,
        override val textAppearance: TextAppearance,
        override val loaderAppearance: LoaderAppearance,
    ) : ButtonAppearance(
        height,
        contentSpacing,
        shape,
        colors,
        elevation,
        border,
        contentPadding,
        iconAppearance,
        textAppearance,
        loaderAppearance
    ) {
        fun copy(
            height: Dp = this.height,
            contentSpacing: Dp = this.contentSpacing,
            shape: Shape = this.shape,
            colors: ButtonColors = this.colors,
            elevation: ButtonElevation? = this.elevation,
            border: BorderStroke? = this.border,
            contentPadding: PaddingValues = this.contentPadding
        ): OutlineButtonAppearance = OutlineButtonAppearance(
            height = height.takeOrElse { this.height },
            contentSpacing = contentSpacing.takeOrElse { this.contentSpacing },
            shape = shape,
            colors = colors.copy(),
            elevation = elevation,
            border = border?.copy(),
            contentPadding = contentPadding,
            iconAppearance = iconAppearance.copy(),
            textAppearance = textAppearance.copy(),
            loaderAppearance = loaderAppearance.copy()
        )
    }

    /**
     * Represents the visual appearance of a text button.
     *
     * This class defines the styling attributes for a button that primarily displays text,
     * optionally with an icon. It includes properties for height, spacing, shape, colors,
     * elevation, border, content padding, icon appearance, text appearance, and loader appearance.
     *
     * @property height The default height of the button.
     * @property contentSpacing The spacing between the text and the optional icon within the button.
     * @property shape The shape of the button's background.
     * @property colors The color scheme for the button's various states (e.g., enabled, disabled, pressed).
     * @property elevation The elevation (shadow) of the button. May be null if no elevation is desired.
     * @property border The border of the button. May be null if no border is desired.
     * @property contentPadding The padding between the button's edges and its content.
     * @property iconAppearance The appearance settings for the optional icon within the button.
     * @property textAppearance The appearance settings for the text within the button.
     * @property loaderAppearance The appearance settings for the loader, shown when the button is in a loading state.
     *
     * This class is immutable, ensuring that once an instance is created, its properties cannot be changed.
     * This is beneficial for performance and thread safety in Compose.
     */
    @Immutable
    data class TextButtonAppearance(
        override val height: Dp,
        override val contentSpacing: Dp,
        override val shape: Shape,
        override val colors: ButtonColors,
        override val elevation: ButtonElevation?,
        override val border: BorderStroke?,
        override val contentPadding: PaddingValues,
        override val iconAppearance: IconAppearance,
        override val textAppearance: TextAppearance,
        override val loaderAppearance: LoaderAppearance,
    ) : ButtonAppearance(
        height,
        contentSpacing,
        shape,
        colors,
        elevation,
        border,
        contentPadding,
        iconAppearance,
        textAppearance,
        loaderAppearance
    ) {
        fun copy(
            height: Dp = this.height,
            contentSpacing: Dp = this.contentSpacing,
            shape: Shape = this.shape,
            colors: ButtonColors = this.colors,
            elevation: ButtonElevation? = this.elevation,
            border: BorderStroke? = this.border,
            contentPadding: PaddingValues = this.contentPadding
        ): TextButtonAppearance = TextButtonAppearance(
            height = height.takeOrElse { this.height },
            contentSpacing = contentSpacing.takeOrElse { this.contentSpacing },
            shape = shape,
            colors = colors.copy(),
            elevation = elevation,
            border = border?.copy(),
            contentPadding = contentPadding,
            iconAppearance = iconAppearance.copy(),
            textAppearance = textAppearance.copy(),
            loaderAppearance = loaderAppearance.copy()
        )
    }

    /**
     * Represents the visual appearance configuration for an image button.
     *
     * This data class defines the properties that control the look and feel of a button
     * that primarily displays an image (icon). It extends [ButtonAppearance] and provides
     * values for attributes such as height, spacing, shape, colors, elevation, border,
     * content padding, and the appearance of an optional loader. Note that for an
     * ImageButtonAppearance, the `iconAppearance` and `textAppearance` are typically null
     * as the main content is the image itself, though a loader is still supported.
     *
     * @property height The default height of the image button.
     * @property contentSpacing The spacing within the button. This is typically not relevant
     *   for a single-image button but is included for consistency with the base class.
     * @property shape The shape of the image button's container.
     * @property colors The colors used to render the button's background and content.
     * @property elevation The elevation of the button, used to create a shadow effect.
     * @property border The border stroke applied to the button.
     * @property contentPadding The padding around the button's content (the image).
     * @property iconAppearance This property is usually null for [IconButtonAppearance] as
     *   the main content is the button's image itself, not an icon within other content.
     * @property textAppearance This property is usually null for [IconButtonAppearance] as
     *   the button does not typically display text.
     * @property loaderAppearance The appearance configuration for a loader (e.g., a spinner)
     *   displayed on the button when it is in a loading state.
     *
     * This class is immutable, ensuring that once an instance is created, its properties cannot be changed.
     * This is beneficial for performance and thread safety in Compose.
     */
    @Immutable
    data class IconButtonAppearance(
        override val height: Dp,
        override val contentSpacing: Dp,
        override val shape: Shape,
        override val colors: ButtonColors,
        override val elevation: ButtonElevation?,
        override val border: BorderStroke?,
        override val contentPadding: PaddingValues,
        override val iconAppearance: IconAppearance?,
        override val textAppearance: TextAppearance?,
        override val loaderAppearance: LoaderAppearance,
    ) : ButtonAppearance(
        height,
        contentSpacing,
        shape,
        colors,
        elevation,
        border,
        contentPadding,
        iconAppearance,
        textAppearance,
        loaderAppearance
    ) {
        fun copy(
            height: Dp = this.height,
            contentSpacing: Dp = this.contentSpacing,
            shape: Shape = this.shape,
            colors: ButtonColors = this.colors,
            elevation: ButtonElevation? = this.elevation,
            border: BorderStroke? = this.border,
            contentPadding: PaddingValues = this.contentPadding
        ): IconButtonAppearance = IconButtonAppearance(
            height = height.takeOrElse { this.height },
            contentSpacing = contentSpacing.takeOrElse { this.contentSpacing },
            shape = shape,
            colors = colors.copy(),
            elevation = elevation,
            border = border?.copy(),
            contentPadding = contentPadding,
            iconAppearance = iconAppearance?.copy(),
            textAppearance = textAppearance?.copy(),
            loaderAppearance = loaderAppearance.copy()
        )
    }
}

/**
 * Provides default appearances for different types of buttons.
 *
 * This object encapsulates the default styling and behavior configurations for filled, outlined,
 * and text buttons within the application. It leverages Material Design 3 principles and provides
 * consistent visual and interaction patterns across all button instances.
 */
object ButtonAppearanceDefaults {

    internal val ButtonCornerRadius = 4.dp
    internal val ButtonSpacing = 8.dp
    internal val ButtonIconSize = 20.dp
    internal val ButtonLoaderSize = 22.dp
    internal val ButtonLoaderWidth = 2.dp
    internal val ButtonHeight = 48.dp

    /**
     * Creates the appearance configuration for a filled button.
     *
     * This function defines the visual and interactive characteristics of a filled button, including
     * its dimensions, colors, shape, elevation, and content styling. It allows customization based on
     * the button's enabled state.
     *
     * @return A [ButtonAppearance.FilledButtonAppearance] object representing the configured appearance.
     *
     * @see ButtonAppearance
     * @see ButtonDefaults
     * @see IconAppearanceDefaults
     * @see LoaderAppearanceDefaults
     * @see TextAppearanceDefaults
     */
    @Composable
    fun filledButtonAppearance(): ButtonAppearance.FilledButtonAppearance =
        ButtonAppearance.FilledButtonAppearance(
            height = ButtonHeight,
            contentSpacing = ButtonSpacing,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(),
            elevation = ButtonDefaults.buttonElevation(),
            border = null,
            contentPadding = ButtonDefaults.ContentPadding,
            iconAppearance = IconAppearanceDefaults.appearance()
                .copy(
                    size = ButtonIconSize,
                    tint = ButtonDefaults.buttonColors().contentColor
                ),
            textAppearance = TextAppearanceDefaults.appearance().copy(
                style = LocalTextStyle.current.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            ),
            loaderAppearance = LoaderAppearanceDefaults.appearance().copy(
                color = ButtonDefaults.buttonColors().containerColor,
                strokeWidth = ButtonLoaderWidth
            )
        )

    /**
     * Creates a default appearance for an outlined button.
     *
     * This function defines the visual properties of an outlined button, including its height,
     * content spacing, shape, colors, border, content padding, icon appearance, text appearance,
     * and loader appearance. It allows customization based on the enabled state of the button.
     *
     * @return A [ButtonAppearance.OutlineButtonAppearance] object representing the configured
     *   appearance of an outlined button.
     */
    @Composable
    fun outlineButtonAppearance(): ButtonAppearance.OutlineButtonAppearance =
        ButtonAppearance.OutlineButtonAppearance(
            height = ButtonHeight,
            contentSpacing = ButtonSpacing,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.outlinedButtonColors(),
            elevation = null,
            border = BorderStroke(
                width = ButtonDefaults.outlinedButtonBorder().width,
                color = ButtonDefaults.buttonColors().containerColor
            ),
            contentPadding = ButtonDefaults.ContentPadding,
            iconAppearance = IconAppearanceDefaults.appearance()
                .copy(
                    size = ButtonIconSize,
                    tint = ButtonDefaults.buttonColors().containerColor
                ),
            textAppearance = TextAppearanceDefaults.appearance().copy(
                style = LocalTextStyle.current.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            ),
            loaderAppearance = LoaderAppearanceDefaults.appearance().copy(
                color = ButtonDefaults.buttonColors().containerColor,
                strokeWidth = ButtonLoaderWidth
            )
        )

    /**
     * Creates the appearance configuration for a text button.
     *
     * This function defines the visual attributes of a text button, including its size,
     * shape, colors, padding, and the appearance of its icon, text, and loader elements.
     * It provides a consistent styling for text buttons across the application.
     *
     * @return A [ButtonAppearance.TextButtonAppearance] object that encapsulates the styling
     *         configuration for the text button.
     */
    @Composable
    fun textButtonAppearance(): ButtonAppearance.TextButtonAppearance =
        ButtonAppearance.TextButtonAppearance(
            height = ButtonHeight,
            contentSpacing = ButtonSpacing,
            shape = ButtonDefaults.textShape,
            colors = ButtonDefaults.textButtonColors(),
            elevation = null,
            border = null,
            contentPadding = ButtonDefaults.TextButtonContentPadding,
            iconAppearance = IconAppearanceDefaults.appearance()
                .copy(
                    size = ButtonIconSize,
                    tint = ButtonDefaults.buttonColors().containerColor
                ),
            textAppearance = TextAppearanceDefaults.appearance().copy(
                style = LocalTextStyle.current.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            ),
            loaderAppearance = LoaderAppearanceDefaults.appearance().copy(
                color = ButtonDefaults.buttonColors().containerColor,
                strokeWidth = ButtonLoaderWidth
            )
        )

    @Composable
    fun imageButtonAppearance(): ButtonAppearance.IconButtonAppearance =
        ButtonAppearance.IconButtonAppearance(
            height = ButtonHeight,
            contentSpacing = ButtonSpacing,
            shape = ButtonDefaults.textShape,
            colors = ButtonDefaults.textButtonColors(),
            elevation = null,
            border = null,
            contentPadding = ButtonDefaults.ContentPadding,
            iconAppearance = null,
            textAppearance = null,
            loaderAppearance = LoaderAppearanceDefaults.appearance().copy(
                color = ButtonDefaults.buttonColors().containerColor,
                strokeWidth = ButtonLoaderWidth
            )
        )
}

/**
 * A customizable filled button component designed for the SDK.
 *
 * This composable function creates a filled button with configurable appearance, text, optional icon,
 * loading state, and click behavior. It adapts to different screen sizes and font scales.
 *
 * @param modifier Modifier to be applied to the button.
 * @param text The text to be displayed on the button.
 * @param enabled Controls whether the button is enabled for user interaction. Defaults to true.
 * @param isLoading Indicates if the button is in a loading state. If true, the button will show a loader
 *                  instead of the text and icon, and it will be disabled. Defaults to false.
 * @param buttonIcon Optional icon to be displayed alongside the text. Defaults to null.
 * @param appearance Defines the visual appearance of the button, including colors, shape, elevation, etc.
 *                   Defaults to a filled button appearance based on the enabled/isLoading state using
 *                   [ButtonAppearanceDefaults.filledButtonAppearance].
 * @param onClick Callback function invoked when the button is clicked.
 */
@Composable
internal fun SdkButton(
    modifier: Modifier = Modifier,
    text: String?,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    buttonIcon: ButtonIcon? = null,
    appearance: ButtonAppearance.FilledButtonAppearance = ButtonAppearanceDefaults.filledButtonAppearance(),
    onClick: () -> Unit,
) {
    // The actual enabled state for the underlying Button composable and for styling
    val isEnabled = enabled && !isLoading
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale
    // Calculate adjusted height with a scaling factor
    val adjustedButtonHeight = appearance.height.scaled(fontScale)

    val disabledIconOrLoaderTint = ButtonDefaults.buttonColors().disabledContentColor
    val runtimeAppearance = remember(appearance, isEnabled) {
        if (isEnabled) {
            appearance
        } else {
            appearance.copy(
                iconAppearance = appearance.iconAppearance.copy( // Assuming iconAppearance is not null
                    tint = disabledIconOrLoaderTint
                ),
                loaderAppearance = appearance.loaderAppearance.copy(
                    color = disabledIconOrLoaderTint
                )
            )
        }
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(adjustedButtonHeight),
        enabled = isEnabled,
        content = {
            ButtonContent(
                text = text ?: "",
                buttonIcon = buttonIcon,
                isLoading = isLoading,
                horizontalSpacing = runtimeAppearance.contentSpacing,
                iconAppearance = runtimeAppearance.iconAppearance,
                textAppearance = runtimeAppearance.textAppearance,
                loaderAppearance = runtimeAppearance.loaderAppearance
            )
        },
        colors = runtimeAppearance.colors,
        shape = runtimeAppearance.shape,
        elevation = runtimeAppearance.elevation,
        border = runtimeAppearance.border,
        contentPadding = if (buttonIcon != null && isLoading) {
            ButtonDefaults.ButtonWithIconContentPadding
        } else {
            runtimeAppearance.contentPadding
        }
    )
}

/**
 * A customizable outlined button composable.
 *
 * This function creates an outlined button with various customization options for text,
 * icon, loading state, and appearance. It leverages Material Design 3's OutlinedButton
 * and provides an interface for styling and handling interactions.
 *
 * @param modifier The modifier to be applied to the button.
 * @param text The text to be displayed on the button.
 * @param enabled Controls the enabled state of the button. Defaults to `true`.
 * @param isLoading Controls the loading state of the button. If `true`, a loader will be displayed
 * instead of the text and the button will be disabled. Defaults to `false`.
 * @param buttonIcon Optional icon to be displayed alongside the text. Defaults to `null`.
 * @param appearance The appearance settings for the button, including colors, shape, typography, etc.
 * Defaults to a default outlined button appearance based on the effective enabled state (i.e., `enabled && !isLoading`).
 * The parameter is a composable function that takes the effective enabled state and returns a [ButtonAppearance.OutlineButtonAppearance].
 * @param onClick The callback to be invoked when the button is clicked.
 *
 * @see ButtonAppearance
 * @see ButtonAppearanceDefaults
 * @see ButtonIcon
 * @see OutlinedButton
 * @see ButtonContent
 */
@Composable
internal fun SdkOutlineButton(
    modifier: Modifier = Modifier,
    text: String?,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    buttonIcon: ButtonIcon? = null,
    appearance: ButtonAppearance.OutlineButtonAppearance = ButtonAppearanceDefaults.outlineButtonAppearance(),
    onClick: () -> Unit,
) {
    // The actual enabled state for the underlying Button composable and for styling
    val isEnabled = enabled && !isLoading
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale
    // Calculate adjusted height with a scaling factor
    val adjustedButtonHeight = appearance.height.scaled(fontScale)

    val disabledBorderWidth = ButtonDefaults.outlinedButtonBorder(enabled).width
    val disabledBorderColor = ButtonDefaults.buttonColors().disabledContentColor
    val disabledIconOrLoaderTint = ButtonDefaults.buttonColors().disabledContentColor
    val runtimeAppearance = remember(appearance, isEnabled) {
        if (isEnabled) {
            appearance
        } else {
            appearance.copy(
                border = appearance.border?.copy(
                    width = disabledBorderWidth,
                    brush = SolidColor(disabledBorderColor)
                ),
                iconAppearance = appearance.iconAppearance.copy( // Assuming iconAppearance is not null
                    tint = disabledIconOrLoaderTint
                ),
                loaderAppearance = appearance.loaderAppearance.copy(
                    color = disabledIconOrLoaderTint
                )
            )
        }
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(adjustedButtonHeight),
        enabled = isEnabled,
        content = {
            ButtonContent(
                text = text ?: "",
                buttonIcon = buttonIcon,
                isLoading = isLoading,
                horizontalSpacing = runtimeAppearance.contentSpacing,
                iconAppearance = runtimeAppearance.iconAppearance,
                textAppearance = runtimeAppearance.textAppearance,
                loaderAppearance = runtimeAppearance.loaderAppearance
            )
        },
        colors = runtimeAppearance.colors,
        shape = runtimeAppearance.shape,
        elevation = runtimeAppearance.elevation,
        border = runtimeAppearance.border,
        contentPadding = if (buttonIcon != null && isLoading) {
            ButtonDefaults.ButtonWithIconContentPadding
        } else {
            runtimeAppearance.contentPadding
        }
    )
}

/**
 * A composable function that renders a customizable text button.
 *
 * This button provides options for enabling/disabling, displaying a loading state,
 * including an icon, and customizing its appearance through [ButtonAppearance.TextButtonAppearance].
 * It dynamically adjusts its height based on the device's font scale settings.
 *
 * @param modifier The modifier to be applied to the button.
 * @param text The text to be displayed on the button.
 * @param enabled Controls the enabled state of the button. Defaults to true.
 * @param isLoading Controls whether a loading indicator should be displayed instead of the text. Defaults to false.
 * @param buttonIcon Optional [ButtonIcon] to be displayed alongside the text. Defaults to null.
 * @param appearance The [ButtonAppearance.TextButtonAppearance] configuration provider for the button's styling.
 * Defaults to a text button appearance based on the effective enabled state (i.e., `enabled && !isLoading`).
 * The parameter is a composable function that takes the effective enabled state and returns a [ButtonAppearance.TextButtonAppearance].
 * @param onClick The callback to be invoked when the button is clicked.
 *
 * @see ButtonAppearance
 * @see ButtonAppearanceDefaults
 * @see ButtonIcon
 * @see TextButton
 * @see ButtonContent
 */
@Composable
internal fun SdkTextButton(
    modifier: Modifier = Modifier,
    text: String?,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    buttonIcon: ButtonIcon? = null,
    appearance: ButtonAppearance.TextButtonAppearance = ButtonAppearanceDefaults.textButtonAppearance(),
    onClick: () -> Unit,
) {
    // The actual enabled state for the underlying Button composable and for styling
    val isEnabled = enabled && !isLoading
    // Get the appearance by invoking the provider with the current effective enabled state
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale
    // Calculate adjusted height with a scaling factor
    val adjustedButtonHeight = appearance.height.scaled(fontScale)

    val disabledIconOrLoaderTint = ButtonDefaults.buttonColors().disabledContentColor
    val runtimeAppearance = remember(appearance, isEnabled) {
        if (isEnabled) {
            appearance
        } else {
            appearance.copy(
                iconAppearance = appearance.iconAppearance.copy( // Assuming iconAppearance is not null
                    tint = disabledIconOrLoaderTint
                ),
                loaderAppearance = appearance.loaderAppearance.copy(
                    color = disabledIconOrLoaderTint
                )
            )
        }
    }

    TextButton(
        onClick = onClick,
        modifier = modifier.height(adjustedButtonHeight),
        enabled = isEnabled,
        content = {
            ButtonContent(
                text = text ?: "",
                buttonIcon = buttonIcon,
                isLoading = isLoading,
                horizontalSpacing = runtimeAppearance.contentSpacing,
                iconAppearance = runtimeAppearance.iconAppearance,
                textAppearance = runtimeAppearance.textAppearance,
                loaderAppearance = runtimeAppearance.loaderAppearance
            )
        },
        colors = runtimeAppearance.colors,
        elevation = runtimeAppearance.elevation,
        border = runtimeAppearance.border,
        contentPadding = if (buttonIcon != null && isLoading) {
            ButtonDefaults.ButtonWithIconContentPadding
        } else {
            runtimeAppearance.contentPadding
        }
    )
}

@Composable
internal fun SdkIconButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    appearance: ButtonAppearance.IconButtonAppearance = ButtonAppearanceDefaults.imageButtonAppearance(),
    buttonIcon: ButtonIcon,
    contentDescription: String? = null,
    onClick: () -> Unit
) {
    // The actual enabled state for the underlying Button composable and for styling
    val isEnabled = enabled && !isLoading
    // Get the appearance by invoking the provider with the current effective enabled state
    val disabledIconOrLoaderTint = ButtonDefaults.buttonColors().disabledContentColor
    val runtimeAppearance = remember(appearance, isEnabled) {
        if (isEnabled) {
            appearance
        } else {
            appearance.copy(
                iconAppearance = appearance.iconAppearance?.copy(
                    tint = disabledIconOrLoaderTint
                ),
                loaderAppearance = appearance.loaderAppearance.copy(
                    color = disabledIconOrLoaderTint
                )
            )
        }
    }
    Button(
        modifier = modifier.heightIn(min = ButtonAppearanceDefaults.ButtonHeight),
        onClick = onClick,
        enabled = isEnabled,
        colors = runtimeAppearance.colors,
        shape = runtimeAppearance.shape,
        elevation = runtimeAppearance.elevation,
        border = runtimeAppearance.border,
        contentPadding = runtimeAppearance.contentPadding
    ) {
        // Animate visibility of the loader
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center),
            label = "loaderVisibilityAnimation"
        ) {
            SdkButtonLoader(appearance = runtimeAppearance.loaderAppearance)
        }

        // Animate visibility of the icon
        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center),
            label = "iconVisibilityAnimation"
        ) {
            when (buttonIcon) {
                is ButtonIcon.Vector -> SdkIcon(
                    imageVector = buttonIcon.icon,
                    contentDescription = contentDescription
                )
                is ButtonIcon.DrawableRes -> SdkIcon(
                    painter = painterResource(buttonIcon.drawable),
                    contentDescription = contentDescription
                )
            }
        }
    }
}

/**
 * Composable function to display the content of the button.
 *
 * @param text Text displayed on the button.
 * @param buttonIcon Icon to display on the button, defined as a `ButtonIcon`. This allows for custom vector
 * or drawable resources.
 * @param isLoading Flag to determine if the button is in a loading state.
 */
@Composable
private fun ButtonContent(
    text: String,
    buttonIcon: ButtonIcon? = null,
    isLoading: Boolean,
    horizontalSpacing: Dp,
    iconAppearance: IconAppearance,
    textAppearance: TextAppearance,
    loaderAppearance: LoaderAppearance
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        // Animate visibility of the loader
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center),
            label = "loaderVisibilityAnimation"
        ) {
            SdkButtonLoader(appearance = loaderAppearance)
        }

        // Animate visibility of the icon and text when not loading
        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center),
            label = "iconAndTextVisibilityAnimation"
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    horizontalSpacing,
                    Alignment.CenterHorizontally
                )
            ) {
                // Display the icon (if any)
                when (buttonIcon) {
                    is ButtonIcon.Vector -> SdkIcon(
                        imageVector = buttonIcon.icon,
                        contentDescription = null,
                        appearance = iconAppearance
                    )
                    is ButtonIcon.DrawableRes -> SdkIcon(
                        painter = painterResource(buttonIcon.drawable),
                        contentDescription = null,
                        appearance = iconAppearance
                    )

                    else -> Unit
                }

                // Display the text
                SdkText(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = text,
                    appearance = textAppearance,
                )
            }
        }
    }
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkButtonEnabled() {
    SdkButton(
        text = "Filled Enabled",
        onClick = {},
        modifier = Modifier.padding(8.dp)
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkButtonDisabled() {
    SdkButton(
        text = "Filled Disabled",
        onClick = {},
        enabled = false,
        modifier = Modifier.padding(8.dp)
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkButtonLoading() {
    SdkButton(
        text = "Filled Loading",
        onClick = {},
        isLoading = true,
        modifier = Modifier.padding(8.dp)
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkButtonWithIcon() {
    SdkButton(
        text = "Filled With Icon",
        onClick = {},
        buttonIcon = ButtonIcon.Vector(Icons.Filled.Add),
        modifier = Modifier.padding(8.dp)
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkOutlineButtonEnabled() {
    SdkOutlineButton(
        text = "Outline Enabled",
        onClick = {},
        modifier = Modifier.padding(8.dp)
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkOutlineButtonDisabled() {
    SdkOutlineButton(
        text = "Outline Disabled",
        onClick = {},
        enabled = false,
        modifier = Modifier.padding(8.dp)
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkOutlineButtonLoading() {
    SdkOutlineButton(
        text = "Outline Loading",
        onClick = {},
        isLoading = true,
        modifier = Modifier.padding(8.dp)
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkOutlineButtonWithIcon() {
    SdkOutlineButton(
        text = "Outline With Icon",
        onClick = {},
        buttonIcon = ButtonIcon.Vector(Icons.Filled.Add),
        modifier = Modifier.padding(8.dp)
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkTextButtonEnabled() {
    SdkTextButton(
        text = "Text Enabled",
        onClick = {},
        modifier = Modifier.padding(8.dp)
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkTextButtonDisabled() {
    SdkTextButton(
        text = "Text Disabled",
        onClick = {},
        enabled = false,
        modifier = Modifier.padding(8.dp)
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkTextButtonLoading() {
    SdkTextButton(
        text = "Text Loading",
        onClick = {},
        isLoading = true,
        modifier = Modifier.padding(8.dp)
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkTextButtonWithIcon() {
    SdkTextButton(
        text = "Text With Icon",
        onClick = {},
        buttonIcon = ButtonIcon.Vector(Icons.Filled.Add),
        modifier = Modifier.padding(8.dp)
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkIconButtonEnabled() {
    SdkIconButton(
        buttonIcon = ButtonIcon.DrawableRes(R.drawable.ic_paypal_button),
        onClick = {},
        modifier = Modifier.padding(8.dp)
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkIconButtonDisabled() {
    SdkIconButton(
        buttonIcon = ButtonIcon.DrawableRes(R.drawable.ic_paypal_button),
        enabled = false,
        onClick = {},
        modifier = Modifier.padding(8.dp)
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkIconButtonLoading() {
    SdkIconButton(
        buttonIcon = ButtonIcon.DrawableRes(R.drawable.ic_paypal_button),
        isLoading = true,
        onClick = {},
        modifier = Modifier.padding(8.dp)
    )
}