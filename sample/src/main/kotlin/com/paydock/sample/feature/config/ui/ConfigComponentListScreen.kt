package com.paydock.sample.feature.config.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.components.list.ListRowItem
import com.paydock.sample.feature.config.models.ConfigComponent

@Composable
fun ConfigComponentListScreen(
    configItems: List<ConfigComponent>,
    onConfigComponentClicked: (ConfigComponent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider()
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(
                items = configItems,
                key = { it.name } // Add key for better recomposition performance
            ) { item ->
                ListRowItem(
                    title = item.displayName()
                ) {
                    onConfigComponentClicked(item)
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}



