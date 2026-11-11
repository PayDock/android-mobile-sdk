package com.paydock.sample.debug

import androidx.compose.runtime.Composable

/**
 * A debug-only screen the Config hub links to.
 *
 * The screens themselves live in the `debug` source set and are listed by `DebugScreenRegistry`; the `release`
 * source set's registry is empty, so a release build has no such screen, route or Config row.
 *
 * @property route The navigation route of the screen.
 * @property title The screen's title, shown in the top bar and as its Config hub row.
 * @property content The screen's content.
 */
data class DebugScreen(val route: String, val title: String, val content: @Composable () -> Unit)
