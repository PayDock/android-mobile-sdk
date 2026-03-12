package com.paydock.feature.threeDS.common.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.LoaderAppearanceDefaults
import com.paydock.feature.threeDS.standalone.presentation.Standalone3DSWidget

/**
 * Represents the appearance configuration for the 3DS widget.
 *
 * @property loader The [LoaderAppearance] configuration for the loader shown within the widget.
 */
@Immutable
class ThreeDSWidgetAppearance(val loader: LoaderAppearance) {

    /**
     * Creates a copy of the [ThreeDSWidgetAppearance] with optionally updated properties.
     *
     * @param loader The [LoaderAppearance] to use for the copy. Defaults to the current loader.
     * @return A new [ThreeDSWidgetAppearance] instance with the specified properties.
     */
    fun copy(loader: LoaderAppearance = this.loader): ThreeDSWidgetAppearance =
        ThreeDSWidgetAppearance(
            loader = loader.copy()
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ThreeDSWidgetAppearance

        return loader == other.loader
    }

    override fun hashCode(): Int {
        return loader.hashCode()
    }
}

/**
 * Default appearance settings for the 3DS widget.
 *
 * This object provides a default [ThreeDSWidgetAppearance] which can be used
 * when a specific appearance is not provided for the [MPGS3dsWidget] and [Standalone3DSWidget].
 */
object ThreeDSAppearanceDefaults {

    /**
     * Creates a default appearance configuration for the 3DS widget.
     *
     * @return The default [ThreeDSWidgetAppearance].
     */
    @Composable
    fun appearance(): ThreeDSWidgetAppearance = ThreeDSWidgetAppearance(
        loader = LoaderAppearanceDefaults.appearance()
    )
}