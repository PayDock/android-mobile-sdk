package com.paydock.sample.feature.style.ui.components.properties.afterpay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.afterpay.android.view.AfterpayColorScheme
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.StringDropdown

@Composable
fun ColorSchemeDropdown(
    modifier: Modifier = Modifier,
    currentColorScheme: AfterpayColorScheme,
    onAfterpayColorSchemeChange: (AfterpayColorScheme) -> Unit,
) {
    val options = remember {
        listOf(
            "Black on White" to AfterpayColorScheme.BLACK_ON_WHITE,
            "Black on Mint" to AfterpayColorScheme.BLACK_ON_MINT,
            "Mint on Black" to AfterpayColorScheme.MINT_ON_BLACK,
            "White on Black" to AfterpayColorScheme.WHITE_ON_BLACK
        )
    }

    val selectedOptionString = remember(currentColorScheme) {
        options.find { it.second == currentColorScheme }?.first
            ?: AfterpayColorScheme.BLACK_ON_MINT.name
    }

    StringDropdown(
        modifier = modifier,
        title = stringResource(R.string.label_afterpay_color_scheme),
        options = options.map { it.first },
        selectedOption = selectedOptionString,
        onOptionSelected = { newValueString ->
            val newButtonText =
                options.find { it.first == newValueString }?.second
                    ?: AfterpayColorScheme.BLACK_ON_MINT
            onAfterpayColorSchemeChange(newButtonText)
        }
    )
}