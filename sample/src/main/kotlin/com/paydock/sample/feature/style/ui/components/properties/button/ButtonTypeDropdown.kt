package com.paydock.sample.feature.style.ui.components.properties.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.paydock.sample.feature.style.ui.components.core.dropdown.DropdownSelector
import com.paydock.sample.feature.style.ui.components.section.EditableButtonType

@Composable
fun ButtonTypeDropdown(
    modifier: Modifier = Modifier,
    currentButtonType: EditableButtonType,
    onButtonTypeChange: (EditableButtonType) -> Unit,
) {
    val options = remember { EditableButtonType.entries.map { it.displayName } }

    val selectedButtonType = remember(currentButtonType) {
        options.find { it == currentButtonType.displayName }
            ?: EditableButtonType.FILLED.displayName
    }

    DropdownSelector(
        modifier = modifier,
        title = "Button Style",
        options = options,
        selectedOption = selectedButtonType,
        onOptionSelected = { displayName ->
            EditableButtonType.entries.find { it.displayName == displayName }?.let { newType ->
                onButtonTypeChange(newType)
            }
        }
    )
}