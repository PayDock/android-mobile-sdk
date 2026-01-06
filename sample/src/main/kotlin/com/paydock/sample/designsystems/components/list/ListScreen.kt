package com.paydock.sample.designsystems.components.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun <T : DisplayableListItem> ListScreen(
    items: List<T>,
    displaySubTitle: Boolean = false,
    displayIcon: Boolean = false,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = items,
            key = { it.displayName() } // Add key for better recomposition performance
        ) { item ->
            ListRowItem(
                title = item.displayName(),
                description = if (displaySubTitle) item.displayDescription() else null,
                iconResource = if (displayIcon) item.displayIcon() else null
            ) {
                onItemClick.invoke(item)
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}