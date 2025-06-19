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
import com.paydock.designsystems.components.text.TextAppearance
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.SectionContainer
import com.paydock.sample.designsystems.theme.appFontFamily
import com.paydock.sample.feature.style.ui.components.core.color.ColorPickerField
import com.paydock.sample.feature.style.ui.components.core.counter.NumberCounter
import com.paydock.sample.feature.style.ui.components.core.toggle.StyleableToggleRow
import com.paydock.sample.feature.style.ui.components.properties.font.FontFamilyDropdown
import com.paydock.sample.feature.style.ui.components.properties.text.TextOverflowDropdown
import com.paydock.sample.feature.style.utils.FontHelper
import com.paydock.sample.feature.style.utils.LocalFontHelper

@Composable
fun StyleTextSection(
    currentAppearance: TextAppearance,
    onAppearanceChange: (TextAppearance) -> Unit
) {
    val fontHelper = LocalFontHelper.current

    // Derived states remain essential for UDF
    val currentTextColor = currentAppearance.style.color
    val currentFontSize = currentAppearance.style.fontSize.value.toInt()
    val currentFontFamily = currentAppearance.style.fontFamily // Pass this to FontFamilyDropdown
    val currentTextOverflow = currentAppearance.overflow
    val currentSoftWrap = currentAppearance.softWrap
    val currentMaxLines = currentAppearance.maxLines
    val currentMinLines = currentAppearance.minLines

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

        TextOverflowDropdown(
            currentOverflow = currentTextOverflow,
            onOverflowChange = { newOverflow ->
                onAppearanceChange(currentAppearance.copy(overflow = newOverflow))
            },
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        StyleableToggleRow(
            label = stringResource(R.string.label_soft_wrap),
            isChecked = currentSoftWrap,
            onCheckedChange = { newCheckedState ->
                onAppearanceChange(currentAppearance.copy(softWrap = newCheckedState))
            }
        )

        HorizontalDivider()

        NumberCounter(
            title = stringResource(R.string.label_max_lines),
            value = currentMaxLines,
            onValueChange = { newValue ->
                onAppearanceChange(currentAppearance.copy(maxLines = newValue))
            }
        )

        HorizontalDivider()

        NumberCounter(
            title = stringResource(R.string.label_min_lines),
            value = currentMinLines,
            onValueChange = { newValue ->
                onAppearanceChange(currentAppearance.copy(minLines = newValue))
            }
        )
    }
}
