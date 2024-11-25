package com.paydock.designsystems.components.button

/**
 * Represents the different types of buttons available in the app.
 *
 * This enum defines the visual style of a button, which can be one of the following:
 * - [Filled]: A button with a solid background color.
 * - [Outlined]: A button with a border and transparent background.
 * - [Text]: A button with no background or border, typically just text.
 */
internal enum class AppButtonType {
    /** A button with a solid background color. */
    Filled,

    /** A button with a border and transparent background. */
    Outlined,

    /** A button with no background or border, typically just text. */
    Text
}
