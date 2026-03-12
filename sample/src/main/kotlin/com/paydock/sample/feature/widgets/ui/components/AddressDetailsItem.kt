package com.paydock.sample.feature.widgets.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.core.domain.model.Event
import com.paydock.core.presentation.util.WidgetEventDelegate
import com.paydock.feature.address.domain.model.integration.AddressDetailsWidgetConfig
import com.paydock.feature.address.presentation.AddressDetailsAppearanceDefaults
import com.paydock.feature.address.presentation.AddressDetailsWidget
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.style.StylingViewModel

@Composable
fun AddressDetailsItem(
    context: Context,
    stylingViewModel: StylingViewModel,
    configViewModel: ConfigViewModel
) {
    // Showcase pre-filled address
//    val mockAddress = BillingAddress(
//        addressLine1 = "1 Park Avenue",
//        city = "Manchester",
//        state = "Greater Manchester",
//        postalCode = "M11 5MW",
//        country = "United Kingdom"
//    )
    val addressAppearance by stylingViewModel.addressWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance =
        addressAppearance ?: AddressDetailsAppearanceDefaults.appearance()
    val addressConfig by configViewModel.addressConfig.collectAsState()

    AddressDetailsWidget(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        config = AddressDetailsWidgetConfig(
            address = addressConfig
        ),
        eventDelegate = object : WidgetEventDelegate {
            override fun widgetEvent(event: Event) {
                Log.d("[AddressDetailsWidget Event]", "[type=${event.type}] $event")
            }
        },
        appearance = currentOrDefaultAppearance
    ) { result ->
        Log.d("[AddressDetailsWidget]", "Address details returned [$result]")
        Toast.makeText(context, "Address details returned [$result]", Toast.LENGTH_SHORT).show()
    }
}