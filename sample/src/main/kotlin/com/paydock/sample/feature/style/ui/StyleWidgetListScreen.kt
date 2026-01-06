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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("style_widget_list_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, start = 16.dp)
                .testTag("style_mode_row"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1.0f)
                    .testTag("style_mode_title"),
                text = "Mode",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            )
            Row(
                modifier = Modifier
                    .padding(vertical = 17.dp)
                    .testTag("style_theme_buttons")
            ) {
                Icon(
                    modifier = Modifier
                        .clickable(role = Role.Button) {
                            onThemeSelected(AppTheme.LIGHT)
                        }
                        .testTag("style_theme_light_button"),
                    painter = painterResource(id = R.drawable.ic_light),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(24.dp))
                Icon(
                    modifier = Modifier
                        .clickable(role = Role.Button) {
                            onThemeSelected(AppTheme.DARK)
                        }
                        .testTag("style_theme_dark_button"),
                    painter = painterResource(id = R.drawable.ic_dark),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("style_caption_text"),
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
                .padding(top = 16.dp, end = 16.dp, start = 16.dp)
                .testTag("style_select_widget_title"),
            text = stringResource(R.string.label_select_widget),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
        )
        Spacer(modifier = Modifier.height(14.dp))
        ListScreen(
            items = remember { WidgetType.entries.sortedBy { it.displayName() } },
            displayIcon = true,
            onItemClick = onWidgetSelected,
            modifier = Modifier.testTag("style_widget_list")
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