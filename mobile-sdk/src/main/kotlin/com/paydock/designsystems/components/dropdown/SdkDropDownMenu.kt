package com.paydock.designsystems.components.dropdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.paydock.core.presentation.extensions.alpha20
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme

/**
 * Composable function to display a dropdown menu with a list of selectable items.
 *
 * @param modifier Modifier for the dropdown menu layout.
 * @param expanded Flag to determine whether the dropdown menu is expanded.
 * @param itemWidth The width of each dropdown menu item.
 * @param items The list of items to be displayed in the dropdown menu.
 * @param dismissOnClickOutside Flag to determine whether clicking outside the dropdown menu dismisses it.
 * @param isClickEnabled Flag to determine whether clicking on the items in the dropdown menu is enabled.
 * @param onItemSelected Callback function triggered when an item is selected from the dropdown menu.
 * @param onDismissed Callback function triggered when the dropdown menu is dismissed.
 */
@Composable
internal fun <String : Any> SdkDropDownMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    itemWidth: Dp,
    items: List<String>,
    dismissOnClickOutside: Boolean = true,
    isClickEnabled: Boolean = true,
    onItemSelected: (String) -> Unit,
    onDismissed: () -> Unit
) {
    // Box composable to contain the dropdown menu
    Box(
        modifier = Modifier
            .width(itemWidth)
            .wrapContentSize(Alignment.TopStart)
    ) {
        // Dropdown menu content
        DropdownMenu(
            modifier = modifier
                .background(Theme.colors.primary.alpha20)
                .testTag("sdkDropDownMenu"),
            properties = PopupProperties(
                dismissOnClickOutside = dismissOnClickOutside
            ),
            expanded = expanded,
            onDismissRequest = {
                onDismissed()
            },
        ) {
            // Iterate through each item in the list and create a dropdown menu item
            items.forEach { item: String ->
                DropdownMenuItem(
                    modifier = Modifier.width(itemWidth),
                    text = {
                        // Display the text of the dropdown menu item
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = AnnotatedString(text = item.toString()),
                            style = Theme.typography.body1,
                            color = Theme.colors.onSurface
                        )
                    },
                    enabled = isClickEnabled,
                    onClick = {
                        // Handle item selection
                        onItemSelected(item)
                    }
                )
            }
        }
    }
}

/**
 * Composable function to preview the expanded state of the SdkDropDownMenu.
 */
@PreviewLightDark
@Composable
internal fun PreviewSdkDropDownMenuExpanded() {
    // Preview SdkDropDownMenu in the expanded state
    SdkTheme {
        SdkDropDownMenu(
            expanded = true,
            itemWidth = 300.dp,
            items = listOf("Item 1", "Item 2", "Item 3"),
            onItemSelected = {},
            onDismissed = {}
        )
    }
}
