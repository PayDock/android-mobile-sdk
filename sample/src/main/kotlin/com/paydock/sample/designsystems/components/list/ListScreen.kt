package com.paydock.sample.designsystems.components.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.theme.Theme

@Composable
fun <T : DisplayableListItem> ListScreen(items: List<T>, onItemClick: (T) -> Unit) {
    HorizontalDivider(color = Theme.colors.outlineVariant)
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(items) { item ->
            ListRow(
                title = item.displayName(),
                description = item.displayDescription(),
            ) {
                onItemClick.invoke(item)
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}