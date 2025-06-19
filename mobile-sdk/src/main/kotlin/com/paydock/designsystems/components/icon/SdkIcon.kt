package com.paydock.designsystems.components.icon

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.takeOrElse

/**
 * A composable function that displays an icon using a provided [Painter].
 *
 * This function wraps the standard Compose [Icon] composable, providing a
 * simplified interface and applying default theming through [IconAppearance].
 *
 * @param modifier Modifier to be applied to the icon.
 * @param painter The [Painter] instance to draw the icon.
 * @param contentDescription Text used by accessibility services to describe what this icon represents.
 *        This should always be provided unless this icon is used for decorative purposes only;
 *        this ensures accessibility for screen readers.
 * @param appearance The [IconAppearance] defining the visual properties of the icon, such as tint.
 *                   Defaults to [IconAppearanceDefaults.appearance()].
 */
@Composable
fun SdkIcon(
    modifier: Modifier = Modifier,
    painter: Painter,
    contentDescription: String? = null,
    appearance: IconAppearance = IconAppearanceDefaults.appearance()
) {
    Icon(
        modifier = modifier.size(appearance.size),
        painter = painter,
        contentDescription = contentDescription,
        tint = appearance.tint
    )
}

/**
 * A composable function that displays an icon using the provided [ImageVector].
 *
 * This function provides a simplified way to create an icon with customizable
 * appearance using [IconAppearance]. It wraps the standard [Icon] composable.
 *
 * @param modifier The modifier to be applied to the icon.
 * @param imageVector The [ImageVector] to be displayed.
 * @param contentDescription The content description for the icon. This is used for accessibility.
 *  If `null`, no content description is provided.
 * @param appearance The [IconAppearance] to be applied to the icon. This controls properties like the tint.
 *  Defaults to [IconAppearanceDefaults.appearance()].
 */
@Composable
fun SdkIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String? = null,
    appearance: IconAppearance = IconAppearanceDefaults.appearance()
) {
    Icon(
        modifier = modifier.size(appearance.size),
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = appearance.tint
    )
}

/**
 * Represents the appearance of an icon, primarily defined by its tint color.
 *
 * This class is immutable, ensuring that once an instance is created, its properties cannot be changed.
 * This is particularly useful in Compose, where immutability helps optimize recomposition.
 *
 * @property tint The color to tint the icon. If not specified, the icon will be rendered without any tint.
 */
@Immutable
class IconAppearance(
    val size: Dp,
    val tint: Color
) {
    /**
     * Creates a new [IconAppearance] instance with the same properties as this one, but with optionally overridden values.
     *
     * @param tint The color to use for the icon's tint. If null or unspecified, the tint from this [IconAppearance] instance will be used.
     * @return A new [IconAppearance] instance with the specified properties.
     */
    fun copy(
        size: Dp = this.size,
        tint: Color = this.tint
    ): IconAppearance = IconAppearance(
        size = size.takeOrElse { this.size },
        tint = tint.takeOrElse { this.tint }
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IconAppearance

        if (size != other.size) return false
        if (tint != other.tint) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size.hashCode()
        result = 31 * result + tint.hashCode()
        return result
    }
}

/**
 * [IconAppearanceDefaults] provides default values for [IconAppearance].
 *
 * This object contains functions that return pre-configured [IconAppearance] instances with
 * sensible default values. These defaults can be used directly or as a basis for further
 * customization.
 */
object IconAppearanceDefaults {

    /**
     * Defines the default visual appearance for an icon.
     *
     * This function provides a [IconAppearance] with a default tint based on the current
     * [LocalContentColor]. This ensures that the icon's color automatically adapts to the
     * surrounding content's color theme, maintaining visual consistency.
     *
     * @return An [IconAppearance] instance with the tint set to the current [LocalContentColor].
     */
    @Composable
    fun appearance(): IconAppearance = IconAppearance(
        size = Dp.Unspecified,
        tint = Color.Unspecified
    )
}