package com.paydock.sample.feature.style.ui.components.properties.shape

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.SectionContainer
import com.paydock.sample.feature.style.ui.components.core.counter.NumberCounter
import com.paydock.sample.feature.style.ui.components.core.dropdown.DropdownSelector

enum class CornerUnit {
    DP,
    PERCENTAGE
}

enum class ConfigurableShapeType(val displayName: String) {
    RECTANGLE("Rectangle"),
    ROUNDED_RECTANGLE("Rounded")
}

data class EnhancedShapeConfig(
    val type: ConfigurableShapeType = ConfigurableShapeType.RECTANGLE,
    val cornerUnit: CornerUnit = CornerUnit.DP,
    val cornerRadiusDp: Dp = 8.dp, // MaterialTheme.shapes.small
    val cornerRadiusPercent: Int = 50 // Pill Shape
) {
    val composeShape: Shape
        get() = when (type) {
            ConfigurableShapeType.RECTANGLE -> RectangleShape
            ConfigurableShapeType.ROUNDED_RECTANGLE -> {
                when (cornerUnit) {
                    CornerUnit.DP -> RoundedCornerShape(cornerRadiusDp.coerceAtLeast(0.dp))
                    CornerUnit.PERCENTAGE -> RoundedCornerShape(
                        percent = cornerRadiusPercent.coerceIn(
                            0,
                            100
                        ) // Allow 0-100 for RoundedCornerShape(percent=)
                        // but visually, 50 is max for "roundness"
                    )
                }
            }
        }
}

@Composable
fun ShapeDropdown(
    modifier: Modifier = Modifier,
    containerLabel: String = stringResource(R.string.label_shape),
    defaultUnit: CornerUnit = CornerUnit.DP,
    defaultRadiusDP: Dp = 8.dp,
    defaultRadiusPercent: Int = 50,
    currentShape: Shape,
    onShapeChange: (Shape) -> Unit
) {
    val configurableShapeTypeOptions = remember { ConfigurableShapeType.entries.toTypedArray() }
    val cornerUnitOptions = remember { CornerUnit.entries.toTypedArray() }

    // This is the internal, mutable state of the ShapeDropdown's UI.
    // It should only be initialized once based on the initial currentShape,
    // and then updated by user interactions or significant changes from currentShape prop.
    var internalConfig by remember {
        mutableStateOf(
            // Initial deduction based on the VERY FIRST currentShape
            when (currentShape) {
                RectangleShape -> EnhancedShapeConfig(
                    type = ConfigurableShapeType.RECTANGLE,
                    cornerUnit = defaultUnit,
                    cornerRadiusDp = defaultRadiusDP,
                    cornerRadiusPercent = defaultRadiusPercent
                )
                is RoundedCornerShape -> EnhancedShapeConfig(
                    type = ConfigurableShapeType.ROUNDED_RECTANGLE,
                    // For a RoundedCornerShape, we cannot reliably read its creation Dp/Percent.
                    cornerUnit = defaultUnit,
                    cornerRadiusDp = defaultRadiusDP,
                    cornerRadiusPercent = defaultRadiusPercent
                )
                else -> EnhancedShapeConfig(
                    type = ConfigurableShapeType.RECTANGLE,
                    cornerUnit = defaultUnit,
                    cornerRadiusDp = defaultRadiusDP,
                    cornerRadiusPercent = defaultRadiusPercent
                )
            }
        )
    }

    // Effect to react to changes in the currentShape PROP
    LaunchedEffect(currentShape) {
        val newPropShapeType = when (currentShape) {
            RectangleShape -> ConfigurableShapeType.RECTANGLE
            is RoundedCornerShape -> ConfigurableShapeType.ROUNDED_RECTANGLE
            else -> ConfigurableShapeType.RECTANGLE
        }

        if (internalConfig.type != newPropShapeType) {
            // If the fundamental type of the incoming currentShape prop has changed
            // (e.g., from Rectangle to Rounded or vice-versa),
            // then we MUST reset the internalConfig to reflect this new fundamental type,
            // using the defaults for that type.
            internalConfig = when (newPropShapeType) {
                ConfigurableShapeType.RECTANGLE -> EnhancedShapeConfig(
                    type = ConfigurableShapeType.RECTANGLE,
                    cornerUnit = defaultUnit,
                    cornerRadiusDp = defaultRadiusDP,
                    cornerRadiusPercent = defaultRadiusPercent
                )
                ConfigurableShapeType.ROUNDED_RECTANGLE -> EnhancedShapeConfig(
                    type = ConfigurableShapeType.ROUNDED_RECTANGLE,
                    cornerUnit = defaultUnit,       // Reset to default unit
                    cornerRadiusDp = defaultRadiusDP, // Reset to default Dp
                    cornerRadiusPercent = defaultRadiusPercent // Reset to default Percent
                )
            }
            // Note: We don't call onShapeChange here as this is reacting to an incoming prop,
            // not a user interaction within this component.
        } else if (newPropShapeType == ConfigurableShapeType.ROUNDED_RECTANGLE) {
            // The incoming currentShape prop is still a RoundedCornerShape,
            // and our internalConfig.type is already ROUNDED_RECTANGLE.
            // In this scenario, WE DO NOT want to reset internalConfig's cornerRadiusDp,
            // cornerRadiusPercent, or cornerUnit to defaults, because the user might have
            // already modified them via the UI. The internalConfig holds the user's choices.
            // We cannot reliably read Dp/Percent from the incoming currentShape to update
            // internalConfig, so we preserve the existing internalConfig's radius/unit.
        }
    }

    SectionContainer(modifier = modifier, title = containerLabel, subTitle = stringResource(R.string.disclaimer_rounded_shape_defaults)) {
        DropdownSelector(
            options = configurableShapeTypeOptions.map { it.displayName },
            selectedOption = internalConfig.type.displayName,
            onOptionSelected = { selectedDisplayName ->
                configurableShapeTypeOptions.find { it.displayName == selectedDisplayName }
                    ?.let { newType ->
                        if (newType != internalConfig.type) {
                            // User changed the type within this component
                            val newConfig = internalConfig.copy(
                                type = newType,
                                // When user changes type, reset unit/values to component defaults
                                cornerUnit = defaultUnit,
                                cornerRadiusDp = defaultRadiusDP,
                                cornerRadiusPercent = defaultRadiusPercent
                            )
                            internalConfig = newConfig
                            onShapeChange(newConfig.composeShape)
                        }
                    }
            }
        )

        if (internalConfig.type == ConfigurableShapeType.ROUNDED_RECTANGLE) {
            // Dropdown for CornerUnit (DP vs PERCENTAGE)
            DropdownSelector(
                title = stringResource(R.string.label_corner_unit),
                options = cornerUnitOptions.map { it.name },
                selectedOption = internalConfig.cornerUnit.name,
                onOptionSelected = { selectedUnitName ->
                    cornerUnitOptions.find { it.name == selectedUnitName }?.let { newUnit ->
                        if (newUnit != internalConfig.cornerUnit) {
                            // User changed the unit
                            val newConfig = internalConfig.copy(cornerUnit = newUnit)
                            // Note: When changing unit, we keep the existing Dp/Percent values.
                            // The user will then adjust the relevant one.
                            internalConfig = newConfig
                            onShapeChange(newConfig.composeShape)
                        }
                    }
                }
            )
            when (internalConfig.cornerUnit) {
                CornerUnit.DP -> {
                    NumberCounter(
                        title = stringResource(id = R.string.label_corner_radius_dp),
                        value = internalConfig.cornerRadiusDp.value.toInt(),
                        onValueChange = { newIntValue ->
                            val newRadiusDp = newIntValue.dp.coerceAtLeast(0.dp)
                            if (newRadiusDp != internalConfig.cornerRadiusDp) {
                                val newConfig = internalConfig.copy(cornerRadiusDp = newRadiusDp)
                                internalConfig = newConfig
                                onShapeChange(newConfig.composeShape)
                            }
                        }
                    )
                }
                CornerUnit.PERCENTAGE -> {
                    NumberCounter(
                        title = stringResource(id = R.string.label_corner_radius_percent),
                        value = internalConfig.cornerRadiusPercent,
                        onValueChange = { newIntValue ->
                            val newPercent = newIntValue.coerceIn(0, 100)
                            if (newPercent != internalConfig.cornerRadiusPercent) {
                                val newConfig = internalConfig.copy(cornerRadiusPercent = newPercent)
                                internalConfig = newConfig
                                onShapeChange(newConfig.composeShape)
                            }
                        }
                    )
                }
            }
        }
    }
}