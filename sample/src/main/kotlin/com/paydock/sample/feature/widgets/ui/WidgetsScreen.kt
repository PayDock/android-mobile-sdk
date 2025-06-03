package com.paydock.sample.feature.widgets.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.list.ListScreen
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun WidgetsScreen(onWidgetSelected: (WidgetType) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider()
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, start = 16.dp, top = 16.dp),
            text = stringResource(R.string.label_select_widget),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
        )
        Spacer(modifier = Modifier.height(14.dp))
        ListScreen(
            items = WidgetType.entries.sortedBy { it.displayName() },
            displaySubTitle = true,
            onItemClick = onWidgetSelected
        )
    }
}

@PreviewLightDark
@Composable
internal fun PreviewWidgetsScreen() {
    SampleTheme {
        WidgetsScreen() {}
    }
}