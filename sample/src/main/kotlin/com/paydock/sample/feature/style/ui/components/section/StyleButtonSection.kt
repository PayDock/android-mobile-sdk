package com.paydock.sample.feature.style.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    // However, direct modification through sub-editors will still need to use
    // the `copy()` methods of the specific type.
    val appearanceForEditing =
        remember(currentAppearance, selectedButtonType) {
            // If the currentAppearance's type doesn't match selectedButtonType,
            // it means the user just changed the dropdown. We should reset to the default for that type.
            when (selectedButtonType) {
                EditableButtonType.FILLED ->
                    if (currentAppearance is ButtonAppearance.FilledButtonAppearance && selectedButtonType == EditableButtonType.FILLED) currentAppearance
                    else defaultFilledAppearance

                EditableButtonType.OUTLINE ->
                    if (currentAppearance is ButtonAppearance.OutlineButtonAppearance && selectedButtonType == EditableButtonType.OUTLINE) currentAppearance
                    else defaultOutlineAppearance

                EditableButtonType.TEXT ->
                    if (currentAppearance is ButtonAppearance.TextButtonAppearance && selectedButtonType == EditableButtonType.TEXT) currentAppearance
                    else defaultTextAppearance

//                EditableButtonType.ICON ->
//                    if (currentAppearance is ButtonAppearance.IconButtonAppearance && selectedButtonType == EditableButtonType.ICON) currentAppearance
//                    else defaultIconAppearance
            }
        }

    // This LaunchedEffect is now less about "fixing" a type mismatch after the fact,
    // and more about a general safeguard or if an external change to 'currentAppearance'
    // somehow didn't align with 'selectedButtonType'. For direct dropdown changes,
    // the immediate call to onAppearanceChange in the dropdown's lambda is more direct.
    // You might even find this effect becomes redundant if the dropdown logic is robust.
    LaunchedEffect(appearanceForEditing, currentAppearance) {
        if (currentAppearance::class != appearanceForEditing::class) {
            onAppearanceChange(appearanceForEditing)
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
                selectedButtonType = newType // Update local state for the dropdown
                // Immediately update the parent/ViewModel with the correct default for the new type
                val newDefaultAppearance = when (newType) {
                    EditableButtonType.FILLED -> defaultFilledAppearance
                    EditableButtonType.OUTLINE -> defaultOutlineAppearance
                    EditableButtonType.TEXT -> defaultTextAppearance
                    // EditableButtonType.ICON -> defaultIconAppearance
                }
                onAppearanceChange(newDefaultAppearance)
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

            else -> Text("$appearanceForEditing not implemented!")
        }
    }
}