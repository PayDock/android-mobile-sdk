package com.paydock.sample.feature.style.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.designsystems.components.link.LinkTextAppearance
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.SectionContainer
import com.paydock.sample.designsystems.components.fields.TextField

@Composable
fun StyleLinkTextSection(
    currentAppearance: LinkTextAppearance,
    onAppearanceChange: (LinkTextAppearance) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        SectionContainer(title = stringResource(R.string.label_more_options)) {
            TextField(
                label = stringResource(R.string.label_clickable_description),
                value = currentAppearance.clickableDescription ?: "",
                onValueChange = { newDesc ->
                    onAppearanceChange(currentAppearance.copy(clickableDescription = newDesc.takeIf { it.isNotEmpty() }))
                }
            )
        }
    }
}
