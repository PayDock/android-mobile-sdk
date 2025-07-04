package com.paydock.sample.feature.style.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.designsystems.components.toggle.ToggleAppearance
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.SectionContainer
import com.paydock.sample.feature.style.ui.components.core.color.ColorPickerField

@Composable
fun StyleToggleSection(
    currentAppearance: ToggleAppearance,
    onAppearanceChange: (ToggleAppearance) -> Unit
) {
    // Derived states remain essential for UDF
    val currentCheckedThumbColor = currentAppearance.colors.checkedThumbColor
    val currentCheckedIconColor = currentAppearance.colors.checkedIconColor
    val currentCheckedTrackColor = currentAppearance.colors.checkedTrackColor
    val currentCheckedBorderColor = currentAppearance.colors.checkedBorderColor

    val currentUncheckedThumbColor = currentAppearance.colors.uncheckedThumbColor
    val currentUncheckedIconColor = currentAppearance.colors.uncheckedIconColor
    val currentUncheckedTrackColor = currentAppearance.colors.uncheckedTrackColor
    val currentUncheckedBorderColor = currentAppearance.colors.uncheckedBorderColor

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        SectionContainer(title = stringResource(R.string.label_toggle_colours)) {
            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_checked_thumb_colour),
                currentColor = currentCheckedThumbColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                checkedThumbColor = newColor
                            )
                        )
                    )
                }
            )

            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_checked_icon_colour),
                currentColor = currentCheckedIconColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                checkedIconColor = newColor
                            )
                        )
                    )
                }
            )

            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_checked_track_colour),
                currentColor = currentCheckedTrackColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                checkedTrackColor = newColor
                            )
                        )
                    )
                }
            )

            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_checked_border_colour),
                currentColor = currentCheckedBorderColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                checkedBorderColor = newColor
                            )
                        )
                    )
                }
            )

            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_unchecked_thumb_colour),
                currentColor = currentUncheckedThumbColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                uncheckedThumbColor = newColor
                            )
                        )
                    )
                }
            )

            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_unchecked_icon_colour),
                currentColor = currentUncheckedIconColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                uncheckedIconColor = newColor
                            )
                        )
                    )
                }
            )

            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_unchecked_track_colour),
                currentColor = currentUncheckedTrackColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                uncheckedTrackColor = newColor
                            )
                        )
                    )
                }
            )

            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_unchecked_border_colour),
                currentColor = currentUncheckedBorderColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                uncheckedBorderColor = newColor
                            )
                        )
                    )
                }
            )
        }
    }
}