package com.paydock.sample.feature.config.ui.properties.googlepay

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.paydock.feature.googlepay.domain.model.GooglePayWidgetConfig
import com.paydock.feature.paypal.checkout.domain.model.integration.PayPalWidgetConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BooleanField
import com.paydock.sample.designsystems.components.fields.EnumDropdown
import com.paydock.sample.designsystems.components.fields.TextField
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.config.models.ConfigComponent
import com.paydock.sample.feature.widgets.ui.models.WidgetType
import org.json.JSONObject

@Composable
fun GooglePayProperties(
    config: GooglePayWidgetConfig,
    configItemName: ConfigComponent,
    widgetContext: WidgetType,
    configViewModel: ConfigViewModel
) {
    when (configItemName) {
        ConfigComponent.ACCESS_TOKEN -> {
            TextField(
                label = stringResource(R.string.label_access_token),
                value = config.accessToken,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ACCESS_TOKEN,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.SERVICE_ID -> {
            TextField(
                label = stringResource(R.string.label_service_id),
                value = config.serviceId,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.SERVICE_ID,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.EMAIL_REQUIRED -> {
            BooleanField(
                label = stringResource(R.string.label_email_required),
                value = config.paymentRequest.get("emailRequired") as Boolean,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.EMAIL_REQUIRED,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.PHONE_NUMBER_REQUIRED -> {
            val shippingParams = config.paymentRequest.getJSONObject("shippingAddressParameters")
            BooleanField(
                label = stringResource(R.string.label_phone_number_required),
                value = shippingParams.get("phoneNumberRequired") as Boolean,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.PHONE_NUMBER_REQUIRED,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.SHIPPING_REQUIRED -> {
            BooleanField(
                label = stringResource(R.string.label_shipping_required),
                value = config.paymentRequest.get("shippingAddressRequired") as Boolean,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.SHIPPING_REQUIRED,
                        newValue
                    )
                }
            )
        }

        else -> {}
    }
}

