package com.paydock.sample.feature.style.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.list.ListScreen
import com.paydock.sample.designsystems.theme.AppTheme
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun StyleWidgetListScreen(
    onThemeSelected: (AppTheme) -> Unit,
    onWidgetSelected: (WidgetType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1.0f),
                text = "Mode",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            )
            Row(modifier = Modifier.padding(vertical = 17.dp)) {
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

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, end = 16.dp, start = 16.dp)
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, end = 16.dp, start = 16.dp),
            text = stringResource(R.string.label_select_widget),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
        )
        Spacer(modifier = Modifier.height(14.dp))
        ListScreen(
            items = WidgetType.entries.sortedBy { it.displayName() },
            displayIcon = true,
            onItemClick = onWidgetSelected
        )
    }
}

@PreviewLightDark
@Composable
internal fun PreviewStyleWidgetListScreen() {
    SampleTheme {
        StyleWidgetListScreen({}, {})
    }
}