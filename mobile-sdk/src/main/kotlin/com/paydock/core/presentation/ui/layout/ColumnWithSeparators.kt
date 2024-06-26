package com.paydock.core.presentation.ui.layout

import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.tooling.preview.Preview

/**
 * A composable that arranges its children in a column with separators between them.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param separator The composable used to render the separator between items (default is [Divider]).
 * @param content The composable block representing the content of the column.
 */
@Composable
internal fun ColumnWithSeparators(
    modifier: Modifier = Modifier,
    separator: @Composable () -> Unit = { HorizontalDivider() },
    content: @Composable () -> Unit,
) {
    // Use SubcomposeLayout to handle the custom layout logic
    SubcomposeLayout(modifier) { constraints ->
        // Subcompose the content and separators
        val contentsMeasurebles = subcompose("content", content)
        val separators = contentsMeasurebles.map { subcompose("separator$it", separator).first() }
        val separatorsPlaceables = separators.map { it.measure(constraints) }
        val contentPlaceables = contentsMeasurebles.map { it.measure(constraints) }

        // Calculate the total height of the layout
        val totalHeight =
            listOf(separatorsPlaceables, contentPlaceables).flatten().sumOf { it.height }

        // Perform the layout
        layout(constraints.maxWidth, totalHeight) {
            // Determine the height of the dividers
            val dividerHeight = separatorsPlaceables.maxOfOrNull { it.height } ?: 0
            var y = 0

            // Place the content and calculate the heights of each item
            val itemHeights = contentPlaceables.mapIndexed { index, placeable ->
                if (index != 0) y += dividerHeight

                placeable.place(x = 0, y = y)
                y += placeable.height
                ContentChild(y)
            }

            // Place the separators at the appropriate positions
            separatorsPlaceables.forEachIndexed { index, placeable ->
                if (index != contentPlaceables.size - 1) {
                    placeable.place(x = 0, y = itemHeights[index].height)
                }
            }
        }
    }
}

/**
 * A data class representing the height of a child in the [ColumnWithSeparators].
 *
 * @param height The height of the child.
 */
private data class ContentChild(val height: Int)

@Preview(name = "Column With Separators", showBackground = true)
@Composable
private fun PreviewColumnWithSeparators() {
    ColumnWithSeparators {
        repeat(times = 10) { Text(text = "Hello $it") }
    }
}
