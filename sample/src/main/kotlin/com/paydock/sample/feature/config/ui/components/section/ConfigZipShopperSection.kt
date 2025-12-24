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
import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.TextField

@Composable
fun ConfigZipShopperSection(
    currentShopper: ZipWidgetConfig.Shopper,
    onShopperChange: (ZipWidgetConfig.Shopper) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            label = stringResource(R.string.label_first_name),
            value = currentShopper.firstName,
            onValueChange = { newValue ->
                onShopperChange(currentShopper.copy(firstName = newValue))
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_last_name),
            value = currentShopper.lastName,
            onValueChange = { newValue ->
                onShopperChange(currentShopper.copy(lastName = newValue))
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_email),
            value = currentShopper.email,
            onValueChange = { newValue ->
                onShopperChange(currentShopper.copy(email = newValue))
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_phone_number_optional),
            value = currentShopper.phone ?: "",
            onValueChange = { newValue ->
                onShopperChange(currentShopper.copy(phone = newValue.ifEmpty { null }))
            }
        )
    }
}


