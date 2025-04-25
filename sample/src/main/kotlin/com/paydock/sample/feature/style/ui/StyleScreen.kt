package com.paydock.sample.feature.style.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun StyleScreen(isDarkMode: Boolean = isSystemInDarkTheme()) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // This manages the current theme (button state)
    var currentTheme by remember {
        mutableStateOf(MaterialTheme)
    }

//    var lightTheme by remember {
//        mutableStateOf(MaterialTheme.colorScheme)
//    }
//    var darkTheme by remember {
//        mutableStateOf(MobileSDK.getInstance().sdkTheme.getDarkColorTheme())
//    }

//    var dimensionsTheme by remember {
//        mutableStateOf(MobileSDK.getInstance().sdkTheme.dimensions)
//    }

//    var fontTheme by remember {
//        mutableStateOf(MobileSDK.getInstance().sdkTheme.font)
//    }

//    val hasChanges = lightTheme != currentTheme.colours.light ||
//            darkTheme != currentTheme.colours.dark ||
//            dimensionsTheme != currentTheme.dimensions ||
//            fontTheme != currentTheme.font
//
//    Scaffold(modifier = Modifier.padding(
//        start = 16.dp,
//        top = 0.dp,
//        end = 16.dp,
//        bottom = 32.dp
//    ),
//        bottomBar = {
//            AppButton(
//                modifier = Modifier.fillMaxWidth(),
//                enabled = hasChanges,
//                text = stringResource(R.string.label_button_update_styles)
//            ) {
//
//                currentTheme = MobileSDKTheme(
//                    colours = ThemeColors(
//                        light = lightTheme,
//                        dark = darkTheme
//                    ),
//                    dimensions = dimensionsTheme,
//                    font = fontTheme
//                )
//                MobileSDK.getInstance().updateTheme(currentTheme)
//                Toast.makeText(context, "Sdk Theme has been updated!", Toast.LENGTH_SHORT).show()
//            }
//        }
//    ) { innerPadding ->
//        ColumnWithSeparators(
//            modifier = Modifier
//                .padding(paddingValues = PaddingValues(bottom = innerPadding.calculateBottomPadding()))
//                .verticalScroll(scrollState),
//            content = {
//                ColoursSection(if (isDarkMode) darkTheme else lightTheme) { theme ->
//                    if (isDarkMode) {
//                        darkTheme = theme
//                    } else {
//                        lightTheme = theme
//                    }
//                }
//                FontSection(fontTheme) { fontFamily ->
//                    fontTheme = MobileSDKTheme.FontName.themeFont().with(fontFamily)
//                }
//                DesignSection(dimensionsTheme) { themeDimensions ->
//                    dimensionsTheme = themeDimensions
//                }
//            })
//    }
//    HorizontalDivider()
}

@PreviewLightDark
@Composable
internal fun PreviewStyleScreen() {
    SampleTheme {
        StyleScreen()
    }
}