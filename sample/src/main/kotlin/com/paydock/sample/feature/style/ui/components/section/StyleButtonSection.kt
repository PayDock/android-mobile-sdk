package com.paydock.sample.feature.style.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.designsystems.components.button.ButtonAppearance
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.sample.feature.style.ui.components.properties.button.ButtonTypeDropdown

// Enum to represent the types of ButtonAppearance we can edit
enum class EditableButtonType(val displayName: String) {
    FILLED("Filled"),
    OUTLINE("Outline"),
    TEXT("Text"),
//    ICON("Icon")
}

@Composable
fun ButtonAppearanceStyleEditor(
    currentAppearance: ButtonAppearance,
    onAppearanceChange: (ButtonAppearance) -> Unit,
    // Provide default instances for each type, makes resetting easier
    defaultFilledAppearance: ButtonAppearance.FilledButtonAppearance = ButtonAppearanceDefaults.filledButtonAppearance(),
    defaultOutlineAppearance: ButtonAppearance.OutlineButtonAppearance = ButtonAppearanceDefaults.outlineButtonAppearance(),
    defaultTextAppearance: ButtonAppearance.TextButtonAppearance = ButtonAppearanceDefaults.textButtonAppearance(),
//    defaultIconAppearance: ButtonAppearance.IconButtonAppearance = ButtonAppearanceDefaults.imageButtonAppearance()
) {
    var selectedButtonType by remember(currentAppearance) {
        mutableStateOf(
            when (currentAppearance) {
                is ButtonAppearance.FilledButtonAppearance -> EditableButtonType.FILLED
                is ButtonAppearance.OutlineButtonAppearance -> EditableButtonType.OUTLINE
                is ButtonAppearance.TextButtonAppearance -> EditableButtonType.TEXT
//                is ButtonAppearance.IconButtonAppearance -> EditableButtonType.ICON
                // Add is ImageButtonAppearance if needed
                else -> EditableButtonType.FILLED // Default fallback
            }
        )
    }

    // This derived state helps ensure we are always working with the correct type
    // and simplifies the `onAppearanceChange` calls from sub-editors.
    // When the type matches, return as-is (don't create new instance).
    // When type doesn't match, create default for new type with preserved text/icon.
    val appearanceForEditing =
        remember(currentAppearance, selectedButtonType) {
            when (selectedButtonType) {
                EditableButtonType.FILLED -> {
                    currentAppearance as? ButtonAppearance.FilledButtonAppearance
                        ?: defaultFilledAppearance.copy(
                            text = currentAppearance.text,
                            icon = currentAppearance.icon
                        )
                }

                EditableButtonType.OUTLINE -> {
                    currentAppearance as? ButtonAppearance.OutlineButtonAppearance
                        ?: defaultOutlineAppearance.copy(
                            text = currentAppearance.text,
                            icon = currentAppearance.icon
                        )
                }

                EditableButtonType.TEXT -> {
                    currentAppearance as? ButtonAppearance.TextButtonAppearance
                        ?: defaultTextAppearance.copy(
                            text = currentAppearance.text,
                            icon = currentAppearance.icon
                        )
                }

//                EditableButtonType.ICON ->
//                    if (currentAppearance is ButtonAppearance.IconButtonAppearance) {
//                        currentAppearance
//                    } else {
//                        defaultIconAppearance.copy(icon = iconToPreserve)
//                    }
            }
        }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        ButtonTypeDropdown(
            modifier = Modifier.fillMaxWidth(),
            currentButtonType = selectedButtonType
        ) { newType ->
            if (selectedButtonType != newType) {
                selectedButtonType = newType

                // Immediately persist the type change by creating the new typed appearance
                // and preserving the current text and icon
                val newAppearance = when (newType) {
                    EditableButtonType.FILLED -> defaultFilledAppearance.copy(
                        text = currentAppearance.text,
                        icon = currentAppearance.icon
                    )

                    EditableButtonType.OUTLINE -> defaultOutlineAppearance.copy(
                        text = currentAppearance.text,
                        icon = currentAppearance.icon
                    )

                    EditableButtonType.TEXT -> defaultTextAppearance.copy(
                        text = currentAppearance.text,
                        icon = currentAppearance.icon
                    )
                }
                onAppearanceChange(newAppearance)
            }
        }

        HorizontalDivider()

        when (appearanceForEditing) {
            is ButtonAppearance.FilledButtonAppearance -> FilledButtonAppearanceStyleEditor(
                currentAppearance = appearanceForEditing,
                onAppearanceChange = onAppearanceChange
            )

            is ButtonAppearance.OutlineButtonAppearance -> OutlineButtonAppearanceStyleEditor(
                currentAppearance = appearanceForEditing,
                onAppearanceChange = onAppearanceChange
            )

            is ButtonAppearance.TextButtonAppearance -> TextButtonAppearanceStyleEditor(
                currentAppearance = appearanceForEditing,
                onAppearanceChange = onAppearanceChange
            )

            is ButtonAppearance.IconButtonAppearance -> IconButtonAppearanceStyleEditor(
                currentAppearance = appearanceForEditing,
                onAppearanceChange = onAppearanceChange
            )

        }
    }
}