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
                initialColor = colorBeingPicked,
                onColourUpdated = { newColor ->
                    colorBeingPicked = newColor
                    onColorChange(newColor)
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