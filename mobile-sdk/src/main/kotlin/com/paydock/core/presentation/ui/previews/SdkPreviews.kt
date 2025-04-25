package com.paydock.core.presentation.ui.previews

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

/**
 *  Composable preview annotation for displaying a composable in both light and dark modes.
 *  It generates two previews: one with the name "Light Mode" and a white background, and another with
 *  the name "Dark Mode" with dark mode enabled and a white background.
 *
 *  Example Usage:
 *
 *  ```
 *  @SdkLightDarkPreviews
 *  @Composable
 *  fun MyComposable() {
 *      // Your composable content
 *      Text("Hello, Compose!")
 *  }
 *  ```
 *  This will render two previews in the IDE's preview pane, showing how "MyComposable" looks in light and dark themes.
 */
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION
)
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
annotation class SdkLightDarkPreviews

/**
 *  [SdkFontScalePreviews] is an annotation class that provides a set of previews for different font scales.
 *  It is used to preview composables with different font scale settings, simulating how they would appear
 *  to users with different accessibility needs or personal preferences.
 *
 *  The annotation itself applies a series of `@Preview` annotations, each configuring a specific font scale:
 *
 *  - "85%": fontScale = 0.85f
 *  - "100%": fontScale = 1.0f (default)
 *  - "115%": fontScale = 1.15f
 *  - "130%": fontScale = 1.3f
 *  - "150%": fontScale = 1.5f
 *  - "180%": fontScale = 1.8f
 *  - "200%": fontScale = 2.0f
 *
 *  Each preview also sets `showBackground = true` to ensure that the composable is rendered with a background,
 *  improving visibility and making it easier to assess the layout and text rendering at different scales.
 *
 *  Usage:
 *  To use [SdkFontScalePreviews], simply annotate a composable function with `@SdkFontScalePreviews`.  When
 *  previewing in Android Studio, you will see multiple previews of the composable, each with a different
 *  font scale applied.
 *
 *  Example:
 *  ```kotlin
 *  @SdkFontScalePreviews
 *  @Composable
 *  fun MyComposable() {
 *      Text("Hello, world!")
 *  }
 *  ```
 *
 *  This will generate previews showing "Hello, world!" rendered at 85%, 100%, 115%, 130%, 150%, 180%, and 200% font scales.
 */
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION
)
@Preview(name = "85%", fontScale = 0.85f, showBackground = true)
@Preview(name = "100%", fontScale = 1.0f, showBackground = true)
@Preview(name = "115%", fontScale = 1.15f, showBackground = true)
@Preview(name = "130%", fontScale = 1.3f, showBackground = true)
@Preview(name = "150%", fontScale = 1.5f, showBackground = true)
@Preview(name = "180%", fontScale = 1.8f, showBackground = true)
@Preview(name = "200%", fontScale = 2f, showBackground = true)
annotation class SdkFontScalePreviews

/**
 *  This annotation class serves as a convenient way to apply a set of predefined @Preview annotations
 *  for testing and visualizing composables in different screen orientations and device configurations.
 *
 *  It bundles two @Preview annotations:
 *
 *  - "Landscape Mode":  Simulates a landscape orientation using `Devices.AUTOMOTIVE_1024p`, a width of 640dp,
 *                     and enabling the background display.
 *  - "Portrait Mode": Simulates a portrait orientation using `Devices.PIXEL_4` and enabling the background display.
 *
 *  Usage:
 *  Simply annotate a composable function with `@SdkOrientationPreviews` to automatically generate previews
 *  for both landscape and portrait orientations within Android Studio's preview pane.
 *
 *  Example:
 *
 *  ```kotlin
 *  @Composable
 *  @SdkOrientationPreviews
 *  fun MyComposable() {
 *      // ... your composable content ...
 *  }
 *  ```
 *
 *  This will render two previews of `MyComposable`: one in landscape mode and the other in portrait mode.
 */
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION
)
@Preview(
    name = "Landscape Mode",
    showBackground = true,
    device = Devices.AUTOMOTIVE_1024p,
    widthDp = 640
)
@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_4)
annotation class SdkOrientationPreviews