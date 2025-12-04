package com.paydock.sample.feature.style.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.designsystems.components.search.DropdownAppearance
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.NumberCounter
import com.paydock.sample.feature.style.ui.components.properties.padding.PaddingEditor
import com.paydock.sample.feature.style.ui.components.properties.shape.ShapeDropdown

@Composable
fun StyleDropdownMiscSection(
    currentAppearance: DropdownAppearance,
    onAppearanceChange: (DropdownAppearance) -> Unit
) {
    // Derived states remain essential for UDF
    val currentShape = currentAppearance.shape
    val currentItemPadding = currentAppearance.itemPadding
    val currentItemHeight = currentAppearance.itemHeight

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        NumberCounter(
            title = stringResource(R.string.label_height),
            value = currentItemHeight.value.toInt(),
            onValueChange = { newValue ->
                onAppearanceChange(currentAppearance.copy(itemHeight = newValue.dp))
            }
        )

        HorizontalDivider()

        PaddingEditor(
            title = stringResource(R.string.label_item_padding),
            currentPaddingValues = currentItemPadding,
            onPaddingChange = { newPaddingValues ->
                onAppearanceChange(
                    currentAppearance.copy(itemPadding = newPaddingValues)
                )
            }
        )

        HorizontalDivider()

        ShapeDropdown(
            modifier = Modifier.fillMaxWidth(),
            currentShape = currentShape,
            onShapeChange = { newShape ->
                onAppearanceChange(currentAppearance.copy(shape = newShape))
            },
        )
    }
}
