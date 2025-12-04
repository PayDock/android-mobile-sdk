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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BigDecimalCounter
import com.paydock.sample.designsystems.components.fields.EnumDropdown
import com.paydock.sample.designsystems.components.fields.TextField
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.config.models.CurrencyCode
import java.math.BigDecimal

@Composable
fun GlobalConfigScreen(
    configViewModel: ConfigViewModel,
    modifier: Modifier = Modifier
) {
    val globalConfig by configViewModel.globalConfig.collectAsState()

    val currentCurrency = remember(globalConfig.cartCurrency) {
        CurrencyCode.fromCode(globalConfig.cartCurrency) ?: CurrencyCode.USD
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.label_global_configuration),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = stringResource(R.string.label_global_config_description),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // API Access Token
        TextField(
            label = stringResource(R.string.label_api_access_token),
            value = globalConfig.apiAccessToken ?: "",
            onValueChange = { configViewModel.updateGlobalAccessToken(it) },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Test Cart Configuration Section
        Text(
            text = stringResource(R.string.label_test_cart_configuration),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        // Total Amount - using BigDecimal counter
        BigDecimalCounter(
            label = stringResource(R.string.label_total_amount),
            value = globalConfig.cartAmount,
            onValueChange = { amount ->
                configViewModel.updateGlobalCartAmount(amount)
            },
            step = BigDecimal("1.0"), // Step of 1.0 for easier incrementing
            modifier = Modifier.padding(bottom = 16.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Currency - using dropdown
        EnumDropdown(
            label = stringResource(R.string.label_currency),
            options = CurrencyCode.entries,
            selectedOption = currentCurrency,
            onOptionSelected = { newCurrency ->
                configViewModel.updateGlobalCartCurrency(newCurrency.code)
            },
            displayText = { it.code },
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

