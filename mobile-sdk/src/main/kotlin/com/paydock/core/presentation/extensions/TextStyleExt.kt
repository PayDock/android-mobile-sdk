package com.paydock.core.presentation.extensions

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Extension property for [TextStyle] that returns a new [TextStyle] instance with bold font weight.
 */
internal val TextStyle.bold: TextStyle
    get() = copy(fontWeight = FontWeight.Bold)

/**
 * Extension property for [TextStyle] that returns a new [TextStyle] instance with normal font weight.
 */
internal val TextStyle.normal: TextStyle
    get() = copy(fontWeight = FontWeight.Normal)

/**
 * Extension property for [TextStyle] that returns a new [TextStyle] instance with light font weight.
 */
internal val TextStyle.light: TextStyle
    get() = copy(fontWeight = FontWeight.Light)
