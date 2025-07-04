package com.paydock.sample.feature.style.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.SectionContainer
import com.paydock.sample.designsystems.theme.appFontFamily
import com.paydock.sample.feature.style.ui.components.core.color.ColorPickerField
import com.paydock.sample.feature.style.ui.components.core.counter.NumberCounter
import com.paydock.sample.feature.style.ui.components.core.toggle.StyleableToggleRow
import com.paydock.sample.feature.style.ui.components.properties.font.FontFamilyDropdown
import com.paydock.sample.feature.style.ui.components.properties.shape.ShapeDropdown
import com.paydock.sample.feature.style.utils.FontHelper
import com.paydock.sample.feature.style.utils.LocalFontHelper

@Composable
fun StyleTextFieldSection(
    currentAppearance: TextFieldAppearance,
    onAppearanceChange: (TextFieldAppearance) -> Unit
) {
    val fontHelper = LocalFontHelper.current

    // Derived states remain essential for UDF
    val currentTextColor = currentAppearance.style.color
    val currentFontSize = currentAppearance.style.fontSize.value.toInt()
    val currentFontFamily = currentAppearance.style.fontFamily
    val currentSingleLine = currentAppearance.singleLine
    val currentCursorColor = currentAppearance.colors.cursorColor
    val currentFocusedBorder = currentAppearance.colors.focusedIndicatorColor
    val currentUnfocusedBorder = currentAppearance.colors.unfocusedIndicatorColor
    val currentFocusedTextColor = currentAppearance.colors.focusedTextColor
    val currentUnfocusedTextColor = currentAppearance.colors.unfocusedTextColor
    val currentFocusedLabelColor = currentAppearance.colors.focusedLabelColor
    val currentUnfocusedLabelColor = currentAppearance.colors.unfocusedLabelColor
    val currentFocusedPlaceholderColor = currentAppearance.colors.focusedPlaceholderColor
    val currentUnfocusedPlaceholderColor = currentAppearance.colors.unfocusedPlaceholderColor
    val currentErrorTextColor = currentAppearance.colors.errorTextColor
    val currentShape = currentAppearance.shape

    // State for available font names for the dropdown
    val systemFontDetailsList = remember { mutableStateListOf<FontHelper.FontInfo>() }
    var isLoadingSystemFonts by remember { mutableStateOf(true) }
    LaunchedEffect(fontHelper) {
        isLoadingSystemFonts = true
        val fontInfos = fontHelper.getSystemFontFileDetails() // NEW function call
        systemFontDetailsList.clear()
        systemFontDetailsList.addAll(fontInfos)
        isLoadingSystemFonts = false
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        SectionContainer(title = stringResource(R.string.label_text_style)) {
            ColorPickerField(
                label = stringResource(R.string.label_text_colour),
                currentColor = currentTextColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(style = currentAppearance.style.copy(color = newColor))
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            FontFamilyDropdown(
                modifier = Modifier.fillMaxWidth(),
                selectedFontFamily = currentFontFamily,
                systemFontDetails = systemFontDetailsList,
                isLoadingFonts = isLoadingSystemFonts,
                defaultAppFontDisplayName = FontHelper.DEFAULT_APP_FONT_DISPLAY_NAME,
                appDefaultFontFamily = appFontFamily,
                onFontFamilySelected = { newFontFamily ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            style = currentAppearance.style.copy(fontFamily = newFontFamily)
                        )
                    )
                },
                fontHelper = fontHelper
            )

            NumberCounter(
                title = stringResource(R.string.label_font_size),
                value = currentFontSize,
                onValueChange = { newValue ->
                    onAppearanceChange(
                        currentAppearance.copy(style = currentAppearance.style.copy(fontSize = newValue.sp))
                    )
                }
            )
        }

        HorizontalDivider()

        SectionContainer(title = stringResource(R.string.label_text_field_colours)) {
            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_cursor_colour),
                currentColor = currentCursorColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                cursorColor = newColor
                            )
                        )
                    )
                }
            )

            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_focused_border_colour),
                currentColor = currentFocusedBorder,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                focusedIndicatorColor = newColor
                            )
                        )
                    )
                }
            )

            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_unfocused_border_colour),
                currentColor = currentUnfocusedBorder,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                unfocusedIndicatorColor = newColor
                            )
                        )
                    )
                }
            )

            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_focused_text_colour),
                currentColor = currentFocusedTextColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                focusedTextColor = newColor
                            )
                        )
                    )
                }
            )

            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_unfocused_text_colour),
                currentColor = currentUnfocusedTextColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                unfocusedTextColor = newColor
                            )
                        )
                    )
                }
            )

            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_focused_label_colour),
                currentColor = currentFocusedLabelColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                focusedLabelColor = newColor
                            )
                        )
                    )
                }
            )

            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_unfocused_label_colour),
                currentColor = currentUnfocusedLabelColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                unfocusedLabelColor = newColor
                            )
                        )
                    )
                }
            )

            ////////

            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_focused_placeholder_colour),
                currentColor = currentFocusedPlaceholderColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                focusedPlaceholderColor = newColor
                            )
                        )
                    )
                }
            )

            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_unfocused_placeholder_colour),
                currentColor = currentUnfocusedPlaceholderColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                unfocusedPlaceholderColor = newColor
                            )
                        )
                    )
                }
            )

            ////////

            ColorPickerField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.label_error_label_colour),
                currentColor = currentErrorTextColor,
                onColorChange = { newColor ->
                    onAppearanceChange(
                        currentAppearance.copy(
                            colors = currentAppearance.colors.copy(
                                errorLabelColor = newColor
                            )
                        )
                    )
                }
            )
        }

        HorizontalDivider()

        StyleableToggleRow(
            label = stringResource(R.string.label_single_line),
            isChecked = currentSingleLine,
            onCheckedChange = { newCheckedState ->
                onAppearanceChange(currentAppearance.copy(singleLine = newCheckedState))
            }
        )

        HorizontalDivider()

        ShapeDropdown(
            modifier = Modifier.fillMaxWidth(),
            currentShape = currentShape,
            onShapeChange = { newShape ->
                onAppearanceChange(currentAppearance.copy(shape = newShape))
            },
        )
    }
}
