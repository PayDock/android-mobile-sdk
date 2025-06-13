package com.paydock.sample.feature.style.ui.components.properties.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.paydock.sample.R
import com.paydock.sample.feature.style.ui.components.core.dropdown.DropdownSelector

@Composable
fun TextOverflowDropdown(
    currentOverflow: TextOverflow,
    onOverflowChange: (TextOverflow) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = remember {
        listOf(
            "Clip" to TextOverflow.Clip,
            "Ellipsis" to TextOverflow.Ellipsis,
            "Visible" to TextOverflow.Visible
        )
    }

    val selectedOptionString = remember(currentOverflow) {
        options.find { it.second == currentOverflow }?.first ?: TextOverflow.Ellipsis.toString()
    }

    DropdownSelector(
        modifier = modifier,
        title = stringResource(R.string.label_text_overflow),
        options = options.map { it.first },
        selectedOption = selectedOptionString,
        onOptionSelected = { newValueString ->
            val newOverflow =
                options.find { it.first == newValueString }?.second ?: TextOverflow.Ellipsis
            onOverflowChange(newOverflow)
        }
    )
}