package com.paydock.sample.feature.style.ui.components.properties.zip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paydock.feature.zip.presentation.utils.ZipButtonStyle
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.StringDropdown

@Composable
fun ButtonStyleDropdown(
    modifier: Modifier = Modifier,
    currentButtonStyle: ZipButtonStyle,
    onButtonStyleChange: (ZipButtonStyle) -> Unit,
) {
    val options = remember {
        listOf(
            "White on Black" to ZipButtonStyle.WHITE_ON_BLACK,
            "Black on White" to ZipButtonStyle.BLACK_ON_WHITE,
        )
    }

    val selectedOptionString = remember(currentButtonStyle) {
        options.find { it.second == currentButtonStyle }?.first
            ?: "White on Black"
    }

    StringDropdown(
        modifier = modifier,
        title = stringResource(R.string.label_zip_button_style),
        options = options.map { it.first },
        selectedOption = selectedOptionString,
        onOptionSelected = { newValueString ->
            val newButtonStyle =
                options.find { it.first == newValueString }?.second
                    ?: ZipButtonStyle.WHITE_ON_BLACK
            onButtonStyleChange(newButtonStyle)
        }
    )
}

