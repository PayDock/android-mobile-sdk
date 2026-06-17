package com.paydock.feature.threeDS.standalone.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.style.TextAlign
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.OverlayLoaderAppearance
import com.paydock.designsystems.components.loader.OverlayLoaderAppearanceDefaults
import com.paydock.designsystems.components.text.TextAppearanceDefaults
import com.paydock.feature.threeDS.standalone.presentation.Standalone3DSWidget

/**
 * Represents the appearance configuration for the Standalone 3DS widget.
 *
 * @property loader The [OverlayLoaderAppearance] configuration for the loader shown within the widget.
 */
@Immutable
class StandaloneThreeDSWidgetAppearance(val loader: OverlayLoaderAppearance) {

    /**
     * Creates a copy of the [StandaloneThreeDSWidgetAppearance] with optionally updated properties.
     *
     * @param loader The [LoaderAppearance] to use for the copy. Defaults to the current loader.
     * @return A new [StandaloneThreeDSWidgetAppearance] instance with the specified properties.
     */
    fun copy(loader: OverlayLoaderAppearance = this.loader): StandaloneThreeDSWidgetAppearance =
        StandaloneThreeDSWidgetAppearance(
            loader = loader.copy()
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StandaloneThreeDSWidgetAppearance

        return loader == other.loader
    }

    override fun hashCode(): Int {
        return loader.hashCode()
    }
}

/**
 * Default appearance settings for the 3DS widget.
 *
 * This object provides a default [StandaloneThreeDSWidgetAppearance] which can be used
 * when a specific appearance is not provided for the [Standalone3DSWidget].
 */
object StandaloneThreeDSWidgetAppearanceDefaults {

    /**
     * Creates a default appearance configuration for the Standalone 3DS widget.
     *
     * @return The default [StandaloneThreeDSWidgetAppearance].
     */
    @Composable
    fun appearance(): StandaloneThreeDSWidgetAppearance = StandaloneThreeDSWidgetAppearance(
        loader = OverlayLoaderAppearanceDefaults.appearance().copy(
            loaderText = "Processing payment...",
            // Centre the loader text horizontally so it stays centred when it wraps onto
            // multiple lines (default TextAppearance uses TextAlign.Start).
            loaderTextAppearance = TextAppearanceDefaults.appearance().copy(
                textAlign = TextAlign.Center
            )
        )
    )
}