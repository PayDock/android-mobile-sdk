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
 * It allows customization of its enabled state, checked state, and the callback
 * triggered when the checked state changes.
 *
 * @param modifier Modifier to be applied to the switch.
 * @param enabled Controls the enabled state of the switch. When `false`, the switch
 *   is not interactable and appears visually disabled. Defaults to `true`.
 * @param isChecked The checked state of the switch. When `true`, the switch is in the
 *   "on" position. Defaults to `false`.
 * @param onCheckedChange Callback that is invoked when the checked state of the switch
 *   changes. It receives a boolean parameter representing the new checked state.
 *   Defaults to an empty lambda.
 */
@Composable
internal fun SdkSwitch(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isChecked: Boolean = false,
    appearance: SwitchAppearance = SwitchAppearanceDefaults.appearance(),
    onCheckedChange: (Boolean) -> Unit = {}
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
class SwitchAppearance(
    val colors: SwitchColors
) {
    /**
     * Creates a copy of this [SwitchAppearance] with optional modifications.
     *
     * This function allows you to create a new [SwitchAppearance] instance that is
     * a copy of the current instance. You can optionally provide new values for
     * the properties of the [SwitchAppearance] to customize the copied instance.
     *
     * @param colors The new [SwitchColors] to use for the copied appearance.
     *   Defaults to a copy of the current [colors] if not provided.
     * @return A new [SwitchAppearance] instance with the specified modifications.
     */
    fun copy(
        colors: SwitchColors = this.colors
    ): SwitchAppearance = SwitchAppearance(
        colors = colors.copy()
    )
}

/**
 * Defaults for the appearance of the Switch composable.
 *
 * This object provides a default [SwitchAppearance] configuration, which can be used
 * as a starting point for customizing the visual look of [Switch] composables.
 *
 * It defines a default `appearance` function that creates a [SwitchAppearance]
 * using the standard colors provided by [SwitchDefaults.colors()].
 *
 * You can create custom appearance configurations by defining functions similar to
 * `appearance()` that return [SwitchAppearance] instances with different parameters
 * (e.g., custom colors, track shapes, thumb shapes, etc.).
 */
object SwitchAppearanceDefaults {

    /**
     * Defines the appearance of the Switch composable.
     *
     * This function creates a [SwitchAppearance] object, which encapsulates
     * the visual properties of the switch, such as its colors.
     *
     * By default, it uses the standard colors provided by [SwitchDefaults.colors()].
     * You can customize the appearance by providing a different [SwitchColors] instance
     * to the `colors` parameter of [SwitchAppearance].
     *
     * @return a [SwitchAppearance] object defining the visual appearance of the switch.
     */
    @Composable
    fun appearance(): SwitchAppearance = SwitchAppearance(
        colors = SwitchDefaults.colors()
    )
}

@SdkLightDarkPreviews
@Composable
fun SdkSwitchPreviewUnchecked() {
    SdkSwitch(
        isChecked = false,
        onCheckedChange = { }
    )
}

@SdkLightDarkPreviews
@Composable
fun SdkSwitchPreviewChecked() {
    SdkSwitch(
        isChecked = true,
        onCheckedChange = { }
    )
}

@SdkLightDarkPreviews
@Composable
fun SdkSwitchPreviewDisabledUnchecked() {
    SdkSwitch(
        enabled = false,
        isChecked = false,
        onCheckedChange = { }
    )
}

@SdkLightDarkPreviews
@Composable
fun SdkSwitchPreviewDisabledChecked() {
    SdkSwitch(
        enabled = false,
        isChecked = true,
        onCheckedChange = { }
    )
}