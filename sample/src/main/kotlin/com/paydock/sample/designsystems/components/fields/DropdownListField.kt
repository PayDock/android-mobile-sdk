package com.paydock.sample.designsystems.components.fields

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
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.theme.Theme
import kotlinx.coroutines.flow.filter

/**
 * A basic implementation of the Exposed Dropdown Menu component
 *
 * @see https://material.io/components/menus#exposed-dropdown-menu
 */
@Composable
fun DropdownListField(
    modifier: Modifier = Modifier,
    items: List<String>,
    selected: String? = items.firstOrNull(),
    onItemSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions
            .filter { it is PressInteraction.Press }
            .collect {
                expanded = !expanded
            }
    }
    DropdownListFieldStack(
        textField = {
            TextField(
                modifier = modifier,
                value = selected ?: "",
                onValueChange = {},
                interactionSource = interactionSource,
                readOnly = true,
                textStyle = Theme.typography.body,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Theme.colors.onBackground,
                    unfocusedTextColor = Theme.colors.onBackground,
                    disabledTextColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                trailingIcon = {
                    val rotation by animateFloatAsState(if (expanded) 180F else 0F)
                    Icon(
                        rememberVectorPainter(Icons.Default.ArrowDropDown),
                        contentDescription = "Dropdown Arrow",
                        Modifier.rotate(rotation),
                    )
                }
            )
        },
        dropdownMenu = { boxWidth, itemHeight ->
            Box(
                Modifier
                    .width(boxWidth)
                    .wrapContentSize(Alignment.TopStart)
            ) {
                DropdownMenu(
                    modifier = Modifier.background(Color.White),
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // This is to improve performance on large list
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
                                        Text(
                                            text = item,
                                            style = Theme.typography.body,
                                            color = Theme.colors.onPrimary
                                        )
                                    },
                                    modifier = Modifier
                                        .height(itemHeight)
                                        .width(boxWidth)
                                        .background(if (item == selected) Theme.colors.primary else Color.White),
                                    onClick = {
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

@Composable
private fun DropdownListFieldStack(
    textField: @Composable () -> Unit,
    dropdownMenu: @Composable (boxWidth: Dp, itemHeight: Dp) -> Unit,
) {
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

private enum class ExposedDropdownMenuSlot { TextField, Dropdown }