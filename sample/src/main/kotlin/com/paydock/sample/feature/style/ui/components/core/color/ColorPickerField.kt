package com.paydock.sample.feature.style.ui.components.core.color

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import com.paydock.sample.feature.style.models.ColourTheme
import com.paydock.sample.feature.style.ui.components.core.color.picker.ColourPicker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerField(
    label: String,
    currentColor: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var colorForPicker by remember(currentColor, showBottomSheet) { mutableStateOf(currentColor) }

    ColourSelectionField(
        modifier = modifier,
        rawColourTheme = ColourTheme(
            label,
            colorForPicker
        ),
        onItemClicked = {
            scope.launch {
                colorForPicker = currentColor
                showBottomSheet = true
            }
        }
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets(bottom = 30.dp) }
        ) {
            ColourPicker(
                color = colorForPicker,
                onColourUpdated = { newColor ->
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