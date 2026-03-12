package com.paydock.sample.feature.config.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.list.ListRowItem

@Composable
fun CheckoutConfigScreen(
    modifier: Modifier = Modifier,
    onPaymentProcessorSelected: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Checkout Configuration",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Configure checkout flow settings and preferences.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Payment Processor row
        ListRowItem(
            title = "Payment Processor",
            description = "Configure payment processor preferences",
            iconResource = R.drawable.ic_config
        ) {
            onPaymentProcessorSelected()
        }
    }
}

