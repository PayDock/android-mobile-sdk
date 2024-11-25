package com.paydock.designsystems.components.dropdown

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.presentation.extensions.alpha20
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.theme.Theme
import kotlinx.coroutines.flow.filter

/**
 * Composable function to display a dropdown list field with a selectable list of items.
 *
 * @param modifier Modifier for the dropdown list field layout.
 * @param label The label displayed above the dropdown list field.
 * @param readOnly Flag to determine if the dropdown list field is read-only.
 * @param placeholder Placeholder text displayed when no item is selected.
 * @param items The list of items to be displayed in the dropdown list.
 * @param selected The currently selected item in the dropdown list.
 * @param onItemSelected Callback function triggered when an item is selected from the dropdown list.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Suppress("MagicNumber", "LongMethod")
@Composable
internal fun SdkDropdownListField(
    modifier: Modifier = Modifier,
    label: String,
    readOnly: Boolean = true,
    placeholder: String? = null,
    items: List<String>,
    selected: String? = items.firstOrNull(),
    onItemSelected: (String) -> Unit,
) {
    // State to track whether the dropdown menu is expanded or collapsed
    var expanded by remember { mutableStateOf(false) }
    // Interaction source to handle click interactions
    val interactionSource = remember { MutableInteractionSource() }
    // Launched effect to toggle the dropdown menu visibility on click
    LaunchedEffect(interactionSource) {
        interactionSource.interactions
            .filter { it is PressInteraction.Press }
            .collect {
                expanded = !expanded
            }
    }

    // Dropdown list field layout
    DropdownListFieldStack(
        textField = {
            // Display the text field with label and trailing dropdown icon
            SdkTextField(
                modifier = modifier,
                value = selected ?: "",
                label = label,
                placeholder = placeholder ?: label,
                onValueChange = {

                },
                interactionSource = interactionSource,
                readOnly = readOnly,
                trailingIcon = {
                    // Dropdown arrow icon with rotation animation
                    val rotation by animateFloatAsState(
                        if (expanded) 180F else 0F,
                        label = "rotation"
                    )
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.ArrowDropDown),
                        contentDescription = stringResource(R.string.content_desc_dropdown_arrow),
                        modifier = Modifier.rotate(rotation)
                    )
                }
            )
        },
        dropdownMenu = { boxWidth, itemHeight ->
            // Dropdown menu containing the list of items
            Box(
                Modifier
                    .width(boxWidth)
                    .wrapContentSize(Alignment.TopStart)
            ) {
                DropdownMenu(
                    modifier = Modifier.background(Theme.colors.primary.alpha20),
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // Ensure dropdown menu height fits within the screen height
                    val menuHeight = itemHeight * items.count()
                    Box(
                        modifier = Modifier.size(
                            width = boxWidth,
                            height = if (menuHeight < 300.dp) menuHeight else 300.dp
                        )
                    ) {
                        // Lazy column to efficiently display the list of items
                        LazyColumn {
                            items(items) { item ->
                                DropdownMenuItem(
                                    text = {
                                        // Display each item as a selectable dropdown menu item
                                        Text(
                                            text = item,
                                            style = Theme.typography.body1,
                                            color = Theme.colors.onPrimary
                                        )
                                    },
                                    modifier = Modifier
                                        .height(itemHeight)
                                        .width(boxWidth)
                                        .background(
                                            if (item == selected) Theme.colors.primary else Color.Unspecified
                                        ),
                                    onClick = {
                                        // Handle item selection
                                        expanded = false
                                        onItemSelected(item)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
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