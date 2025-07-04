package com.paydock.sample.feature.style.ui.components.core.toggle

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.paydock.sample.designsystems.components.containers.SectionTitle

@Composable
fun StyleableToggleRow(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionTitle(Modifier.weight(1.0f), label)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}