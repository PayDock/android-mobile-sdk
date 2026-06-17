package com.paydock.feature.threeDS.integrated.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.LoaderAppearanceDefaults

/**
 * Represents the appearance configuration for the MPGS 3DS widget.
 *
 * @property loader The [LoaderAppearance] configuration for the loader shown within the widget.
 */
@Immutable
class MPGSThreeDSWidgetAppearance(val loader: LoaderAppearance) {

    /**
     * Creates a copy of the [MPGSThreeDSWidgetAppearance] with optionally updated properties.
     *
     * @param loader The [LoaderAppearance] to use for the copy. Defaults to the current loader.
     * @return A new [MPGSThreeDSWidgetAppearance] instance with the specified properties.
     */
    fun copy(loader: LoaderAppearance = this.loader): MPGSThreeDSWidgetAppearance =
        MPGSThreeDSWidgetAppearance(
            loader = loader.copy()
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MPGSThreeDSWidgetAppearance

        return loader == other.loader
    }

    override fun hashCode(): Int {
        return loader.hashCode()
    }
}

/**
 * Default appearance settings for the 3DS widget.
 *
 * This object provides a default [MPGSThreeDSWidgetAppearance] which can be used
 * when a specific appearance is not provided for the [MPGS3dsWidget].
 */
object MPGSThreeDSWidgetAppearanceDefaults {

    /**
     * Creates a default appearance configuration for the 3DS widget.
     *
     * @return The default [MPGSThreeDSWidgetAppearance].
     */
    @Composable
    fun appearance(): MPGSThreeDSWidgetAppearance = MPGSThreeDSWidgetAppearance(
        loader = LoaderAppearanceDefaults.appearance()
    )
}