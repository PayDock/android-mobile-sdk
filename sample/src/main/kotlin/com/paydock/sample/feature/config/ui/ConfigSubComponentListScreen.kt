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
fun ConfigSubComponentListScreen(
    subComponents: List<ConfigComponent>,
    onConfigComponentClicked: (ConfigComponent) -> Unit,
    header: (@Composable () -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            header?.let { headerContent ->
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        headerContent()
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
            items(subComponents) { item ->
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



