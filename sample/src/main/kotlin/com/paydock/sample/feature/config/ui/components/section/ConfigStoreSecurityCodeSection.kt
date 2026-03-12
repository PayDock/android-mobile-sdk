package com.paydock.sample.feature.config.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BooleanField

@Composable
fun ConfigStoreSecurityCodeSection(
    currentStoreSecurityCode: Boolean?,
    onStoreSecurityCodeChange: (Boolean?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Enable Store Security Code Toggle
        BooleanField(
            label = stringResource(R.string.label_enable_store_security_code),
            value = currentStoreSecurityCode != null,
            onValueChange = { isEnabled ->
                onStoreSecurityCodeChange(
                    if (isEnabled) {
                        // Default to true when enabled
                        true
                    } else {
                        null
                    }
                )
            }
        )

        // Only show the value toggle if Store Security Code is enabled
        if (currentStoreSecurityCode != null) {
            HorizontalDivider()

            // Store Security Code Value Toggle (true/false)
            BooleanField(
                label = stringResource(R.string.label_store_security_code),
                value = currentStoreSecurityCode,
                onValueChange = { newValue ->
                    onStoreSecurityCodeChange(newValue)
                }
            )
        }
    }
}
