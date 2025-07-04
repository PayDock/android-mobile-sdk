package com.paydock.sample.feature.style.ui.components.properties.loader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import com.paydock.sample.R
import com.paydock.sample.feature.style.ui.components.core.dropdown.DropdownSelector

@Composable
fun StrokeCapDropdown(
    modifier: Modifier = Modifier,
    currentStrokeCap: StrokeCap,
    onStrokeCapChange: (StrokeCap) -> Unit
) {
    val options = remember {
        listOf(
            "Butt" to StrokeCap.Butt,
            "Round" to StrokeCap.Round,
            "Square" to StrokeCap.Square
        )
    }

    val selectedOptionString = remember(currentStrokeCap) {
        options.find { it.second == currentStrokeCap }?.first ?: StrokeCap.Round.toString()
    }

    DropdownSelector(
        modifier = modifier,
        title = stringResource(R.string.label_text_overflow),
        options = options.map { it.first },
        selectedOption = selectedOptionString,
        onOptionSelected = { newValueString ->
            val newStrokeCap =
                options.find { it.first == newValueString }?.second ?: StrokeCap.Round
            onStrokeCapChange(newStrokeCap)
        }
    )
}