package com.paydock.sample.feature.style.ui.components.core.color

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.dp
import com.paydock.sample.feature.style.models.ColourTheme
import com.paydock.sample.feature.style.ui.components.core.color.picker.ColourPicker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerField(
    label: String,
    currentColor: Color, // This is the source of truth from outside (can be Unspecified)
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val localContentColor = LocalContentColor.current

    // This state is for what the picker itself is currently manipulating.
    // It's initialized based on currentColor and localContentColor.
    // This will be the color the picker UI is bound to.
    var colorBeingPicked by remember(currentColor, localContentColor) {
        // Determine the initial state for 'colorBeingPicked' when the composable launches
        // or if currentColor/localContentColor changes. This state will persist across
        // bottom sheet open/close, reflecting the last picked color or initial state.
        val initialValue = if (currentColor.isUnspecified) {
            localContentColor
        } else {
            currentColor
        }
        mutableStateOf(initialValue)
    }

    ColourSelectionField(
        modifier = modifier,
        // ColourSelectionField gets the original currentColor.
        // Its internal logic will again resolve Unspecified to LocalContentColor.current for its own display.
        // This is fine and ensures its display is consistent.
        rawColourTheme = ColourTheme(label, currentColor),
        onItemClicked = {
            // *** Determine the picker's starting point when the button is clicked ***
            // This logic now correctly uses the 'localContentColor' value captured above.
            val pickerStartingColor = if (currentColor.isUnspecified) {
                localContentColor // Use the resolved local content color
            } else if (currentColor.alpha == 0f && currentColor != Color.Transparent) {
                // If the original was transparent non-black (e.g. transparent red),
                // start picker with an opaque version for better UX, but retain hue.
                currentColor.copy(alpha = 1f)
            } else if (currentColor == Color.Transparent) {
                // If specifically Color.Transparent (transparent black).
                // If the original was Color.Unspecified, localContentColor is used.
                // If original was Color.Transparent, then we want to make it opaque.
                // If localContentColor itself happened to be transparent (rare), make it opaque black.
                val baseForTransparent =
                    if (currentColor.isUnspecified) localContentColor else currentColor

                if (baseForTransparent.alpha == 0f) {
                    baseForTransparent.copy(alpha = 1f).takeOrElse { Color.Black }
                } else {
                    baseForTransparent // Should already be opaque if derived from localContentColor
                }
            } else {
                currentColor // Use the original color if it's not Unspecified and not fully transparent
            }

            colorBeingPicked = pickerStartingColor // Set the state for the picker
            showBottomSheet = true
        }
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets(bottom = 30.dp) }
        ) {
            ColourPicker(
                color = colorBeingPicked, // Picker now gets a concrete, sensible starting color
                onColourUpdated = { newColor ->
                    colorBeingPicked = newColor // Update the state being picked
                    onColorChange(newColor)   // Propagate the change up
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showBottomSheet = false
                    }
                },
                onCanceled = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showBottomSheet = false
                    }
                }
            )
        }
    }
}