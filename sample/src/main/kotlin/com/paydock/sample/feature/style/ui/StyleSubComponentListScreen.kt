package com.paydock.sample.feature.style.ui

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
import com.paydock.sample.feature.style.models.StyleAppearanceComponent

@Composable
fun StyleSubComponentListScreen(
    subComponents: List<StyleAppearanceComponent>,
    onStyleComponentClicked: (StyleAppearanceComponent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(subComponents) { item ->
                ListRowItem(
                    title = item.displayName()
                ) {
                    onStyleComponentClicked(item)
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}