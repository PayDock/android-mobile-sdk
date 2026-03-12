package com.paydock.sample.feature.style.ui.components.properties.font

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.toFontFamily
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.StringDropdown
import com.paydock.sample.feature.style.utils.FontHelper

@Composable
fun FontFamilyDropdown(
    modifier: Modifier = Modifier,
    selectedFontFamily: FontFamily?,
    systemFontDetails: List<FontHelper.FontInfo>,
    isLoadingFonts: Boolean,
    defaultAppFontDisplayName: String?,
    appDefaultFontFamily: FontFamily?,
    onFontFamilySelected: (FontFamily) -> Unit,
    placeholderText: String = stringResource(R.string.placeholder_select_font),
    loadingText: String = stringResource(R.string.placeholder_loading_fonts),
    noFontsAvailableText: String = stringResource(R.string.placeholder_no_fonts_available),
    fontHelper: FontHelper
) {
    val currentDisplaySelectedName = fontHelper.getFontFamilyDisplayName(
        fontFamily = selectedFontFamily,
        systemFontDetails = systemFontDetails
    ) ?: placeholderText

    val dropdownOptions by remember(
        systemFontDetails,
        defaultAppFontDisplayName,
        isLoadingFonts,
        loadingText,
        noFontsAvailableText
    ) {
        derivedStateOf {
            val options = mutableListOf<String>()
            if (isLoadingFonts) {
                options.add(loadingText)
            } else {
                defaultAppFontDisplayName?.let { options.add(it) }
                options.addAll(systemFontDetails.map { it.displayName }
                    .filterNot { name ->
                        // Filter out the default font and its variants (e.g., "Roboto", "Roboto Regular", "Roboto Bold")
                        name == defaultAppFontDisplayName ||
                                (defaultAppFontDisplayName != null && name.startsWith("$defaultAppFontDisplayName "))
                    })
                if (options.isEmpty()) {
                    options.add(noFontsAvailableText)
                }
            }
            val finalOptions =
                if (isLoadingFonts || (options.size == 1 && (options.first() == loadingText || options.first() == noFontsAvailableText))) {
                    options
                } else {
                    options.distinct().sorted()
                }
            finalOptions
        }
    }

    StringDropdown(
        modifier = modifier,
        title = stringResource(R.string.label_font_family),
        options = dropdownOptions,
        enabled = !isLoadingFonts,
        selectedOption = currentDisplaySelectedName,
        onOptionSelected = { selectedDisplayName ->
            if (selectedDisplayName == loadingText || selectedDisplayName == noFontsAvailableText) {
                return@StringDropdown
            }

            val newFontToSelect: FontFamily? =
                if (selectedDisplayName == defaultAppFontDisplayName && appDefaultFontFamily != null) {
                    appDefaultFontFamily
                } else {
                    // Find the FontInfo object matching the selected display name
                    val selectedFontInfo =
                        systemFontDetails.find { it.displayName == selectedDisplayName }
                    // Use its fileName to get the font
                    selectedFontInfo?.let {
                        val font = fontHelper.findSystemFontByFileName(it.fileName)?.toFontFamily()
                        font
                    }
                }

            if (newFontToSelect != null) {
                if (newFontToSelect != selectedFontFamily) {
                    onFontFamilySelected(newFontToSelect)
                }
            } else if (appDefaultFontFamily != null) {
                if (appDefaultFontFamily != selectedFontFamily) {
                    onFontFamilySelected(appDefaultFontFamily)
                }
            }
        }
    )
}