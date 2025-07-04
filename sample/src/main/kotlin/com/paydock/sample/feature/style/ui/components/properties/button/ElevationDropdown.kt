package com.paydock.sample.feature.style.ui.components.properties.button

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.SectionContainer
import com.paydock.sample.feature.style.ui.components.core.dropdown.DropdownSelector

@Composable
fun ElevationDropdown(
    modifier: Modifier = Modifier,
    currentElevation: ButtonElevation?,
    onElevationChange: (ButtonElevation?) -> Unit,
) {
    // Get the map of predefined elevations. This is done once per composition of this scope.
    val predefinedElevations = getPredefinedElevations()

    // Get the specific instance of the "Default" elevation for comparison if needed
    // This is also resolved once per composition of this scope.
    val defaultMaterialButtonElevation = ButtonDefaults.buttonElevation()

    var selectedElevationKey by remember(
        currentElevation,
        predefinedElevations,
        defaultMaterialButtonElevation
    ) {
        // All necessary values (predefinedElevations, defaultMaterialButtonElevation) are now stable inputs.
        mutableStateOf(
            predefinedElevations.entries.find { (_, value) -> value == currentElevation }?.key
                ?: if (currentElevation == defaultMaterialButtonElevation) {
                    predefinedElevations.entries.find { (_, value) -> value == defaultMaterialButtonElevation }?.key
                } else {
                    null
                }
                ?: predefinedElevations.keys.first() // Fallback to the first key
        )
    }

    SectionContainer(
        modifier = modifier,
        title = stringResource(R.string.label_elevation),
        subTitle = stringResource(R.string.disclaimer_elevation_defaults)
    ) {
        DropdownSelector(
            modifier = modifier,
            options = predefinedElevations.keys.toList(),
            selectedOption = selectedElevationKey,
            onOptionSelected = { newKey ->
                selectedElevationKey = newKey
                val newElevation = predefinedElevations[newKey]
                onElevationChange(newElevation)
            }
        )
    }
}

@Composable
fun getPredefinedElevations(): Map<String, ButtonElevation?> {
    return mapOf(
        "None (Flat)" to null,
        "Default" to ButtonDefaults.buttonElevation(),
        "Elevated" to ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            focusedElevation = 6.dp,
            hoveredElevation = 6.dp,
            disabledElevation = 0.dp
        ),
        "Floating" to ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp,
            focusedElevation = 10.dp,
            hoveredElevation = 10.dp,
            disabledElevation = 1.dp
        )
    )
}