package com.paydock.sample.feature.style.ui.components.properties.afterpay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.afterpay.android.view.AfterpayWidgetStyle
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.StringDropdown

@Composable
fun ColorSchemeDropdown(
    modifier: Modifier = Modifier,
    currentStyle: AfterpayWidgetStyle,
    onAfterpayStyleChange: (AfterpayWidgetStyle) -> Unit,
) {
    val options = remember {
        listOf(
            "Black on Mint" to AfterpayWidgetStyle.Default,
            "Mint on Black" to AfterpayWidgetStyle.Alt,
            "White on Black" to AfterpayWidgetStyle.MonochromeDark,
            "Black on White" to AfterpayWidgetStyle.MonochromeLight
        )
    }

    val selectedOptionString = remember(currentStyle) {
        options.find { it.second == currentStyle }?.first
            ?: AfterpayWidgetStyle.Default.name
    }

    StringDropdown(
        modifier = modifier,
        title = stringResource(R.string.label_afterpay_color_scheme),
        options = options.map { it.first },
        selectedOption = selectedOptionString,
        onOptionSelected = { newValueString ->
            val newStyle =
                options.find { it.first == newValueString }?.second
                    ?: AfterpayWidgetStyle.Default
            onAfterpayStyleChange(newStyle)
        }
    )
}
