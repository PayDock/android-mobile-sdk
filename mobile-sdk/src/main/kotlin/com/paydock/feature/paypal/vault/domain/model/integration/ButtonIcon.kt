package com.paydock.feature.paypal.vault.domain.model.integration

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents the icon used in a button, encapsulating different types of icon resources.
 *
 * This sealed class allows flexibility in specifying button icons, supporting both vector
 * and drawable resources. It provides a type-safe way to handle various icon types in the UI.
 */
sealed class ButtonIcon {

    /**
     * A vector-based icon, typically used for scalable and resolution-independent graphics.
     *
     * @property icon The `ImageVector` representing the vector graphic.
     */
    data class Vector(val icon: ImageVector) : ButtonIcon()

    /**
     * A drawable resource icon, commonly used for bitmap-based graphics.
     *
     * @property drawable The resource ID of the drawable, annotated with `@DrawableRes` to
     * ensure type safety.
     */
    data class DrawableRes(@androidx.annotation.DrawableRes val drawable: Int) : ButtonIcon()
}