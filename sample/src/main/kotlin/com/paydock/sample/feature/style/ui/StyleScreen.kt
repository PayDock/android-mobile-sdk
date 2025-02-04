package com.paydock.sample.feature.style.ui

import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paydock.MobileSDK
import com.paydock.MobileSDKTheme
import com.paydock.ThemeColors
import com.paydock.sample.R
import com.paydock.sample.core.presentation.ui.layout.ColumnWithSeparators
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.designsystems.theme.Theme
import com.paydock.sample.feature.style.ui.components.ColoursSection
import com.paydock.sample.feature.style.ui.components.DesignSection
import com.paydock.sample.feature.style.ui.components.FontSection

@Composable
fun StyleScreen(isDarkMode: Boolean = isSystemInDarkTheme()) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // This manages the current theme (button state)
    var currentTheme by remember {
        mutableStateOf(MobileSDK.getInstance().sdkTheme)
    }

    var lightTheme by remember {
        mutableStateOf(MobileSDK.getInstance().sdkTheme.getLightColorTheme())
    }
    var darkTheme by remember {
        mutableStateOf(MobileSDK.getInstance().sdkTheme.getDarkColorTheme())
    }

    var dimensionsTheme by remember {
        mutableStateOf(MobileSDK.getInstance().sdkTheme.dimensions)
    }

    var fontTheme by remember {
        mutableStateOf(MobileSDK.getInstance().sdkTheme.font)
    }

    val hasChanges = lightTheme != currentTheme.colours.light ||
            darkTheme != currentTheme.colours.dark ||
            dimensionsTheme != currentTheme.dimensions ||
            fontTheme != currentTheme.font

    Scaffold(modifier = Modifier.padding(
        start = 16.dp,
        top = 0.dp,
        end = 16.dp,
        bottom = 32.dp
    ),
        bottomBar = {
            AppButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = hasChanges,
                text = stringResource(R.string.label_button_update_styles)
            ) {

                currentTheme = MobileSDKTheme(
                    colours = ThemeColors(
                        light = lightTheme,
                        dark = darkTheme
                    ),
                    dimensions = dimensionsTheme,
                    font = fontTheme
                )
                MobileSDK.getInstance().updateTheme(currentTheme)
                Toast.makeText(context, "Sdk Theme has been updated!", Toast.LENGTH_SHORT).show()
            }
        }
    ) { innerPadding ->
        ColumnWithSeparators(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(scrollState),
            content = {
                ColoursSection(if (isDarkMode) darkTheme else lightTheme) { theme ->
                    if (isDarkMode) {
                        darkTheme = theme
                    } else {
                        lightTheme = theme
                    }
                }
                FontSection(fontTheme) { fontFamily ->
                    fontTheme = MobileSDKTheme.FontName.themeFont().copy(fontFamily)
                }
                DesignSection(dimensionsTheme) { themeDimensions ->
                    dimensionsTheme = themeDimensions
                }
            })
    }
    HorizontalDivider(color = Theme.colors.outlineVariant)
}

@Preview
@Composable
private fun PreviewStyleScreen() {
    SampleTheme {
        StyleScreen()
    }
}