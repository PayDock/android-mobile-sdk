package com.paydock.sample.feature.style.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.list.ListRowItem
import com.paydock.sample.designsystems.theme.AppTheme
import com.paydock.sample.feature.style.models.StyleAppearanceComponent
import com.paydock.sample.feature.style.mapper.mapWidgetTypeToAppearanceComponents
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun StyleComponentListScreen(
    widgetType: WidgetType,
    onThemeSelected: (AppTheme) -> Unit,
    onStyleComponentClicked: (StyleAppearanceComponent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, end = 16.dp, start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1.0f),
                text = "Mode",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            )
            Row(modifier = Modifier.padding(vertical = 24.dp)) {
                Icon(
                    modifier = Modifier
                        .clickable(role = Role.Button) {
                            onThemeSelected(AppTheme.LIGHT)
                        },
                    painter = painterResource(id = R.drawable.ic_light),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(24.dp))
                Icon(
                    modifier = Modifier
                        .clickable(role = Role.Button) {
                            onThemeSelected(AppTheme.DARK)
                        },
                    painter = painterResource(id = R.drawable.ic_dark),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(R.string.label_style_screen_caption), // TODO: Need to implement hyperlink for Settings screen navigation
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
        )

        Spacer(modifier = Modifier.height(18.dp))

        val styleItems = remember { widgetType.mapWidgetTypeToAppearanceComponents().sortedBy { it.name } }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(styleItems) { item ->
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