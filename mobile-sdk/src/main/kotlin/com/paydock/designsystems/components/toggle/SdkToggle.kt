package com.paydock.designsystems.components.toggle

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews

/**
 * A customizable switch component for the SDK.
 *
 * This composable provides a styled switch that conforms to the SDK's design system.
 * It allows customization of its enabled state, checked state, the appearance of the toggle,
 * and the callback triggered when the checked state changes.
 *
 * @param modifier Modifier to be applied to the switch.
 * @param enabled Controls the enabled state of the switch. When `false`, the switch
 *   is not interactable and appears visually disabled. Defaults to `true`.
 * @param isChecked The checked state of the switch. When `true`, the switch is in the
 *   "on" position. Defaults to `false`.
 * @param appearance Defines the visual appearance of the toggle, including its colors.
 *   Defaults to the standard SDK toggle appearance.
 * @param onCheckedChange Callback that is invoked when the checked state of the switch
 *   changes. It receives a boolean parameter representing the new checked state.
 *   Defaults to an empty lambda.
 */
@Composable
internal fun SdkToggle(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isChecked: Boolean = false,
    appearance: ToggleAppearance = ToggleAppearanceDefaults.appearance(),
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    Switch(
        modifier = modifier,
        enabled = enabled,
        checked = isChecked,
        colors = appearance.colors,
        onCheckedChange = onCheckedChange
    )
}

/**
 * Represents the visual appearance of a Switch composable.
 *
 * This class holds the [SwitchColors] that define the colors used for the
 * different states of the Switch (e.g., checked, unchecked, disabled).
 *
 * @param colors The [SwitchColors] that define the color scheme for the Switch.
 */
@Immutable
class ToggleAppearance(
    val colors: SwitchColors
) {
    /**
     * Creates a copy of this [ToggleAppearance] with optional modifications.
     *
     * This function allows you to create a new [ToggleAppearance] instance that is
     * a copy of the current instance. You can optionally provide new values for
     * the properties of the [ToggleAppearance] to customize the copied instance.
     *
     * @param colors The new [SwitchColors] to use for the copied appearance.
     *   Defaults to a copy of the current [colors] if not provided.
     * @return A new [ToggleAppearance] instance with the specified modifications.
     */
    fun copy(
        colors: SwitchColors = this.colors
    ): ToggleAppearance = ToggleAppearance(
        colors = colors.copy()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ToggleAppearance

        return colors == other.colors
    }

    override fun hashCode(): Int {
        return colors.hashCode()
    }
}

/**
 * Defaults for the appearance of the Switch composable.
 *
 * This object provides a default [ToggleAppearance] configuration, which can be used
 * as a starting point for customizing the visual look of [Switch] composables.
 *
 * It defines a default `appearance` function that creates a [ToggleAppearance]
 * using the standard colors provided by [SwitchDefaults.colors()].
 *
 * You can create custom appearance configurations by defining functions similar to
 * `appearance()` that return [ToggleAppearance] instances with different parameters
 * (e.g., custom colors, track shapes, thumb shapes, etc.).
 */
object ToggleAppearanceDefaults {

    /**
     * Defines the appearance of the Switch composable.
     *
     * This function creates a [ToggleAppearance] object, which encapsulates
     * the visual properties of the switch, such as its colors.
     *
     * By default, it uses the standard colors provided by [SwitchDefaults.colors()].
     * You can customize the appearance by providing a different [SwitchColors] instance
     * to the `colors` parameter of [ToggleAppearance].
     *
     * @return a [ToggleAppearance] object defining the visual appearance of the switch.
     */
    @Composable
    fun appearance(): ToggleAppearance = ToggleAppearance(
        colors = SwitchDefaults.colors()
    )
}

@SdkLightDarkPreviews
@Composable
fun SdkTogglePreviewUnchecked() {
    SdkToggle(
        isChecked = false,
        onCheckedChange = { }
    )
}

@SdkLightDarkPreviews
@Composable
fun SdkTogglePreviewChecked() {
    SdkToggle(
        isChecked = true,
        onCheckedChange = { }
    )
}

@SdkLightDarkPreviews
@Composable
fun SdkTogglePreviewDisabledUnchecked() {
    SdkToggle(
        enabled = false,
        isChecked = false,
        onCheckedChange = { }
    )
}

@SdkLightDarkPreviews
@Composable
fun SdkTogglePreviewDisabledChecked() {
    SdkToggle(
        enabled = false,
        isChecked = true,
        onCheckedChange = { }
    )
}