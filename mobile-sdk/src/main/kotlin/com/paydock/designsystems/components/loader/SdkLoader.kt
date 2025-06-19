package com.paydock.designsystems.components.loader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.takeOrElse
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults

/**
 * A composable function that displays a loading indicator (CircularProgressIndicator).
 *
 * This function provides a customizable circular progress indicator for use in SDK-related contexts.
 * It allows modification of the indicator's appearance through the [LoaderAppearance] parameter.
 *

 * @param appearance The [LoaderAppearance] object that defines the visual properties of the loader,
 *                   such as color, stroke width, track color, and stroke cap. Defaults to [LoaderAppearanceDefaults.appearance()].
 *
 * @see CircularProgressIndicator
 * @see LoaderAppearance
 * @see LoaderAppearanceDefaults
 */
@SdkLightDarkPreviews
@Composable
internal fun SdkLoader(
    modifier: Modifier = Modifier,
    appearance: LoaderAppearance = LoaderAppearanceDefaults.appearance()
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = appearance.color,
        strokeWidth = appearance.strokeWidth,
        trackColor = appearance.trackColor,
        strokeCap = appearance.strokeCap
    )
}

/**
 * A composable function that displays a loader specifically designed for use within buttons.
 *
 * This loader is styled with a smaller size and a thinner stroke width to fit seamlessly
 * within a button's UI. It uses the `onPrimary` color from the Material Theme for its color,
 * making it suitable for display on primary-colored backgrounds.
 *
 * @see SdkLoader
 * @see LoaderAppearanceDefaults
 */
@Composable
internal fun SdkButtonLoader(
    appearance: LoaderAppearance = LoaderAppearanceDefaults.appearance()
) {
    SdkLoader(
        modifier = Modifier.size(ButtonAppearanceDefaults.ButtonLoaderSize),
        appearance = LoaderAppearanceDefaults.appearance().copy(
            color = appearance.color,
            strokeWidth = appearance.strokeWidth,
            trackColor = appearance.trackColor,
            strokeCap = appearance.strokeCap
        )
    )
}

/**
 * A composable function that displays a circular progress indicator with customizable appearance.
 *
 * This function wraps a [CircularProgressIndicator] within a [Box] for layout purposes,
 * allowing for easy placement and background management. It's designed to visualize progress
 * in a linear fashion, typically representing the state of a loading operation or task.
 *
 * @param modifier Modifier to be applied to the circular progress indicator.
 * @param appearance Defines the visual style of the progress indicator, including color,
 *        stroke width, track color, and stroke cap. Defaults to [LoaderAppearanceDefaults.progressAppearance()].
 * @param progress A lambda that returns the current progress value as a Float. The value
 *        should be between 0.0 and 1.0, representing 0% to 100% completion respectively.
 *        This lambda will be called on every recomposition to update the indicator.
 *
 * @see CircularProgressIndicator
 * @see LoaderAppearance
 * @see LoaderAppearanceDefaults
 */
@Composable
internal fun SdkProgressLoader(
    modifier: Modifier = Modifier,
    appearance: LoaderAppearance = LoaderAppearanceDefaults.progressAppearance(),
    progress: () -> Float
) {
    CircularProgressIndicator(
        modifier = modifier,
        progress = progress,
        color = appearance.color,
        strokeWidth = appearance.strokeWidth,
        trackColor = appearance.trackColor,
        strokeCap = appearance.strokeCap
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkProgressLoader() {
    SdkProgressLoader(progress = { 0.5f })
}

/**
 * Represents the appearance of a loader.
 *
 * This class defines the visual properties of a loader, including its color, stroke width,
 * track color, and stroke cap. It is designed to be immutable, ensuring that once an
 * appearance is created, it cannot be modified.
 *
 * @property color The color of the loading indicator. This color will be used to draw the
 *   active part of the loader.
 * @property strokeWidth The width of the stroke used to draw the loading indicator, expressed
 *   in [Dp]. This controls the thickness of the visible loading element.
 * @property trackColor The color of the track behind the loading indicator. This color will be
 *   used to draw the background or inactive part of the loader, providing a visual contrast
 *   with the active indicator.
 * @property strokeCap The style of the cap used at the ends of the loading indicator. This
 *   determines how the ends of the loader's stroke are rendered (e.g., rounded, butt, square).
 */
@Immutable
class LoaderAppearance(
    val color: Color,
    val strokeWidth: Dp,
    val trackColor: Color,
    val strokeCap: StrokeCap
) {

    /**
     * Represents the appearance of a loader.
     *
     * @property color The color of the loading indicator.
     * @property strokeWidth The width of the stroke used to draw the loading indicator.
     * @property trackColor The color of the track behind the loading indicator.
     * @property strokeCap The style of the cap used at the ends of the loading indicator.
     */
    fun copy(
        color: Color = this.color,
        strokeWidth: Dp = this.strokeWidth,
        trackColor: Color = this.trackColor,
        strokeCap: StrokeCap = this.strokeCap
    ) = LoaderAppearance(
        color = color.takeOrElse { this.color },
        strokeWidth = strokeWidth.takeOrElse { this.strokeWidth },
        trackColor = trackColor.takeOrElse { this.trackColor },
        strokeCap = strokeCap
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoaderAppearance

        if (color != other.color) return false
        if (strokeWidth != other.strokeWidth) return false
        if (trackColor != other.trackColor) return false
        if (strokeCap != other.strokeCap) return false

        return true
    }

    override fun hashCode(): Int {
        var result = color.hashCode()
        result = 31 * result + strokeWidth.hashCode()
        result = 31 * result + trackColor.hashCode()
        result = 31 * result + strokeCap.hashCode()
        return result
    }
}

/**
 *  [LoaderAppearanceDefaults] provides default appearance configurations for [LoaderAppearance].
 *
 *  This object offers pre-defined styles for loaders, covering common use cases
 *  such as indeterminate and determinate loaders. These defaults leverage the
 *  Material 3 [ProgressIndicatorDefaults] to ensure consistency with the overall
 *  design language.
 */
object LoaderAppearanceDefaults {

    /**
     * Defines the default appearance for a loader (typically a circular progress indicator).
     *
     * This function provides a pre-configured [LoaderAppearance] object with sensible defaults
     * for common use cases. The default values include:
     *
     * - `color`: The primary color of the progress indicator, set to `ProgressIndicatorDefaults.circularColor`.
     * - `strokeWidth`: The thickness of the progress indicator's stroke, set to `ProgressIndicatorDefaults.CircularStrokeWidth`.
     * - `trackColor`: The color of the track (background circle) behind the progress indicator,
     *      set to `ProgressIndicatorDefaults.circularIndeterminateTrackColor`.
     * - `strokeCap`: The shape of the stroke ends, set to `ProgressIndicatorDefaults.CircularIndeterminateStrokeCap`.
     *
     * These defaults align with the Material 3 design system's recommendations for circular progress indicators.
     *
     * @return A [LoaderAppearance] object representing the default visual configuration for a loader.
     */
    @Composable
    fun appearance(): LoaderAppearance = LoaderAppearance(
        color = ProgressIndicatorDefaults.circularColor,
        strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth,
        trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
        strokeCap = ProgressIndicatorDefaults.CircularIndeterminateStrokeCap
    )

    /**
     * Defines the appearance of a circular progress indicator, including its color, stroke width, track color, and stroke cap.
     *
     * This function provides a default appearance for a determinate circular progress indicator, leveraging Material Design 3 defaults.
     *
     * @return A [LoaderAppearance] object configured with the default appearance for a circular progress indicator.
     *
     * @see LoaderAppearance
     * @see ProgressIndicatorDefaults
     */
    @Composable
    fun progressAppearance(): LoaderAppearance = LoaderAppearance(
        color = ProgressIndicatorDefaults.circularDeterminateTrackColor,
        strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth,
        trackColor = ProgressIndicatorDefaults.circularDeterminateTrackColor,
        strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap
    )
}