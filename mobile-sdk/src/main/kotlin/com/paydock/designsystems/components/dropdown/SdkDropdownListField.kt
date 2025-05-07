package com.paydock.designsystems.components.dropdown

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.icon.SdkIcon
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.components.text.SdkText

/**
 * Composable function to display a dropdown list field with a selectable list of items.
 *
 * @param modifier Modifier for the dropdown list field layout.
 * @param label The label displayed above the dropdown list field.
 * @param placeholder Placeholder text displayed when no item is selected.
 * @param items The list of items to be displayed in the dropdown list.
 * @param selected The currently selected item in the dropdown list.
 * @param onItemSelected Callback function triggered when an item is selected from the dropdown list.
 */
@Composable
internal fun SdkDropdownListField(
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String? = null,
    items: List<String>,
    selected: String? = items.firstOrNull(),
    onItemSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Press) {
                expanded = !expanded
            }
        }
    }

    DropdownListFieldStack(
        textField = {
            SdkDropdownTextField(
                modifier,
                label,
                placeholder,
                selected,
                expanded,
                interactionSource
            )
        },
        dropdownMenu = { boxWidth, itemHeight ->
            SdkDropdownMenu(
                items,
                selected,
                expanded,
                boxWidth,
                itemHeight,
                onItemSelected,
                onExpandedChange = { expanded = it }
            )
        }
    )
}

/**
 * Composable function to display the text field part of the dropdown.
 */
@Suppress("MagicNumber")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SdkDropdownTextField(
    modifier: Modifier,
    label: String,
    placeholder: String?,
    selected: String?,
    expanded: Boolean,
    interactionSource: MutableInteractionSource,
) {
    SdkTextField(
        modifier = modifier,
        value = selected ?: "",
        label = label,
        placeholder = placeholder ?: label,
        onValueChange = { },
        interactionSource = interactionSource,
        trailingIcon = {
            val rotation by animateFloatAsState(
                if (expanded) 180F else 0F,
                label = "rotation"
            )
            SdkIcon(
                painter = rememberVectorPainter(Icons.Default.ArrowDropDown),
                contentDescription = null,
                modifier = Modifier.rotate(rotation)
            )
        }
    )
}

/**
 * Composable function to display the dropdown menu and its items.
 */
@Composable
private fun SdkDropdownMenu(
    items: List<String>,
    selected: String?,
    expanded: Boolean,
    boxWidth: Dp,
    itemHeight: Dp,
    onItemSelected: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit
) {
    Box(
        Modifier
            .width(boxWidth)
            .wrapContentSize(Alignment.TopStart)
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            val menuHeight = itemHeight * items.count()
            Box(
                modifier = Modifier.size(
                    width = boxWidth,
                    height = if (menuHeight < 300.dp) menuHeight else 300.dp
                )
            ) {
                LazyColumn {
                    items(items) { item ->
                        DropdownMenuItem(
                            text = {
                                SdkText(text = item)
                            },
                            modifier = Modifier
                                .height(itemHeight)
                                .width(boxWidth)
                                .background(
                                    if (item == selected) MaterialTheme.colorScheme.primary else Color.Unspecified
                                ),
                            onClick = {
                                onExpandedChange(false)
                                onItemSelected(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable function to stack the text field and dropdown menu vertically.
 *
 * @param textField Composable function representing the text field.
 * @param dropdownMenu Composable function representing the dropdown menu.
 */
@Composable
internal fun DropdownListFieldStack(
    textField: @Composable () -> Unit,
    dropdownMenu: @Composable (boxWidth: Dp, itemHeight: Dp) -> Unit
) {
    // Layout to stack the text field and dropdown menu vertically
    SubcomposeLayout { constraints ->
        val textFieldPlaceable =
            subcompose(ExposedDropdownMenuSlot.TextField, textField).first().measure(constraints)
        val dropdownPlaceable = subcompose(ExposedDropdownMenuSlot.Dropdown) {
            dropdownMenu(textFieldPlaceable.width.toDp(), textFieldPlaceable.height.toDp())
        }.first().measure(constraints)
        layout(textFieldPlaceable.width, textFieldPlaceable.height) {
            textFieldPlaceable.placeRelative(0, 0)
            dropdownPlaceable.placeRelative(0, textFieldPlaceable.height)
        }
    }
}

/**
 * Enum representing the slots in the dropdown menu layout.
 */
private enum class ExposedDropdownMenuSlot { TextField, Dropdown }

@SdkLightDarkPreviews
@Composable
internal fun SdkDropdownListFieldPreview() {
    Column {
        SdkDropdownListField(
            label = "Dropdown",
            items = listOf("Item 1", "Item 2", "Item 3"),
            onItemSelected = { }
        )
    }
}

@SdkLightDarkPreviews
@Composable
internal fun SdkDropdownListFieldPreviewWithSelected() {
    Column {
        SdkDropdownListField(
            label = "Dropdown",
            items = listOf("Item 1", "Item 2", "Item 3"),
            selected = "Item 2",
            onItemSelected = { }
        )
    }
}

@SdkLightDarkPreviews
@Composable
internal fun SdkDropdownMenuPreviewWithSelectedItem() {
    val items = listOf("Item 1", "Item 2", "Item 3")
    val selected = "Item 2"
    val expanded = true
    val boxWidth = 240.dp
    val itemHeight = 48.dp

    SdkDropdownMenu(
        items = items,
        selected = selected,
        expanded = expanded,
        boxWidth = boxWidth,
        itemHeight = itemHeight,
        onItemSelected = { },
        onExpandedChange = { }
    )
}