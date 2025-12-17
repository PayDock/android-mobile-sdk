package com.paydock.sample.feature.config.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BigDecimalCounter
import com.paydock.sample.feature.config.ConfigViewModel
import java.math.BigDecimal

@Composable
fun CartConfigScreen(
    configViewModel: ConfigViewModel,
    modifier: Modifier = Modifier
) {
    val widgetConfig by configViewModel.widgetConfig.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Cart Configuration",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Configure the cart amount for testing purposes.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Total Amount - using BigDecimal counter
        BigDecimalCounter(
            label = stringResource(R.string.label_total_amount),
            value = widgetConfig.cartAmount,
            onValueChange = { amount ->
                configViewModel.updateWidgetCartAmount(amount)
            },
            step = BigDecimal("1.0"), // Step of 1.0 for easier incrementing
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

