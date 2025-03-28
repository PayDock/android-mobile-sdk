package com.paydock.designsystems.components.toggle

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.core.presentation.extensions.alpha20
import com.paydock.core.presentation.extensions.alpha40
import com.paydock.designsystems.theme.Theme

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
@PreviewLightDark
@Composable
internal fun SdkSwitch(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isChecked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Switch(
        modifier = modifier,
        enabled = enabled,
        checked = isChecked,
        colors = SwitchDefaults.colors(
            uncheckedTrackColor = Theme.colors.outlineVariant.alpha20,
            disabledUncheckedThumbColor = Theme.colors.outline.alpha20,
            disabledUncheckedBorderColor = Theme.colors.outline.alpha20,
            disabledUncheckedTrackColor = Theme.colors.outlineVariant.alpha40,
            disabledCheckedThumbColor = Theme.colors.outline.alpha20,
            disabledCheckedBorderColor = Theme.colors.outline.alpha20,
            disabledCheckedTrackColor = Theme.colors.outlineVariant.alpha40,
        ),
        onCheckedChange = onCheckedChange
    )
}