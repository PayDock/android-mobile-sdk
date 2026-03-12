package com.paydock.sample.designsystems.components.fields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Reusable string dropdown component with a label.
 * Used in Style screens.
 */
@Composable
fun StringDropdown(
    modifier: Modifier = Modifier,
    title: String? = null,
    options: List<String>,
    enabled: Boolean = true,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    itemContent: (@Composable (label: String, isSelected: Boolean) -> Unit)? = null,
    selectedContent: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(
            4.dp,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.Start
    ) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )
        }
        DropdownListField(
            modifier = Modifier.fillMaxWidth(),
            items = options,
            enabled = enabled,
            selected = selectedOption,
            onItemSelected = onOptionSelected,
            itemContent = itemContent,
            selectedContent = selectedContent
        )
    }
}


