package com.paydock.sample.feature.config.ui.properties.clicktopay

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.paydock.feature.src.domain.model.integration.ClickToPayWidgetConfig
import com.paydock.feature.src.domain.model.integration.meta.TransactionAmount
import com.paydock.feature.src.domain.model.integration.meta.enum.ApplicationType
import com.paydock.feature.src.domain.model.integration.meta.enum.CheckoutExperience
import com.paydock.feature.src.domain.model.integration.meta.enum.ClickToPayDPAShippingBillingPreference
import com.paydock.feature.src.domain.model.integration.meta.enum.ClickToPayOrderType
import com.paydock.feature.src.domain.model.integration.meta.enum.Services
import com.paydock.feature.src.domain.model.integration.meta.enum.UnacceptedCardType
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BooleanField
import com.paydock.sample.designsystems.components.fields.EnumDropdown
import com.paydock.sample.designsystems.components.fields.TextField
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.config.models.ConfigComponent
import com.paydock.sample.feature.config.models.CurrencyCode
import com.paydock.sample.feature.config.ui.components.properties.card.ConfigCardBrandsField
import com.paydock.sample.feature.config.ui.components.properties.clicktopay.ConfigCoBrandNamesField
import com.paydock.sample.feature.config.ui.components.properties.clicktopay.ConfigPaymentOptionsField
import com.paydock.sample.feature.config.ui.components.section.ConfigClickToPayMetaSection
import com.paydock.sample.feature.config.ui.components.section.ConfigCustomerSection
import com.paydock.sample.feature.config.ui.components.section.ConfigDPADataSection
import com.paydock.sample.feature.config.ui.components.section.ConfigDPATransactionOptionsSection
import com.paydock.sample.feature.widgets.ui.models.WidgetType
import java.math.BigDecimal

@Composable
fun ClickToPayProperties(
    config: ClickToPayWidgetConfig,
    configItemName: ConfigComponent,
    widgetContext: WidgetType,
    configViewModel: ConfigViewModel
) {
    when (configItemName) {
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

        ConfigComponent.CLICK_TO_PAY_META -> {
            ConfigClickToPayMetaSection(
                currentMeta = config.meta,
                onMetaChange = { newMeta ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.CLICK_TO_PAY_META,
                        newMeta
                    )
                }
            )
        }

        // Handle sub-components (individual fields) for backward compatibility
        ConfigComponent.DISABLE_SUMMARY_SCREEN -> {
            BooleanField(
                label = stringResource(R.string.label_disable_summary_screen_optional),
                value = config.meta?.disableSummaryScreen ?: false,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.DISABLE_SUMMARY_SCREEN,
                        newValue.takeIf { it }
                    )
                }
            )
        }

        ConfigComponent.CHECKOUT_EXPERIENCE -> {
            val currentExperience = config.meta?.checkoutExperience
                ?: com.paydock.feature.src.domain.model.integration.meta.enum.CheckoutExperience.WITHIN_CHECKOUT
            EnumDropdown(
                label = stringResource(R.string.label_checkout_experience_optional),
                options = CheckoutExperience.entries,
                selectedOption = currentExperience,
                onOptionSelected = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.CHECKOUT_EXPERIENCE,
                        newValue
                    )
                },
                displayText = { it.name }
            )
        }

        ConfigComponent.SERVICES -> {
            val currentServices = config.meta?.services
                ?: com.paydock.feature.src.domain.model.integration.meta.enum.Services.INLINE_CHECKOUT
            EnumDropdown(
                label = stringResource(R.string.label_services_optional),
                options = Services.entries,
                selectedOption = currentServices,
                onOptionSelected = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.SERVICES,
                        newValue
                    )
                },
                displayText = { it.name }
            )
        }

        ConfigComponent.UNACCEPTED_CARD_TYPE -> {
            val currentType = config.meta?.unacceptedCardType
                ?: com.paydock.feature.src.domain.model.integration.meta.enum.UnacceptedCardType.CREDIT
            EnumDropdown(
                label = stringResource(R.string.label_unaccepted_card_type_optional),
                options = UnacceptedCardType.entries,
                selectedOption = currentType,
                onOptionSelected = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.UNACCEPTED_CARD_TYPE,
                        newValue
                    )
                },
                displayText = { it.name }
            )
        }

        ConfigComponent.CARD_BRANDS -> {
            ConfigCardBrandsField(
                label = stringResource(R.string.label_card_brands_optional),
                selectedBrands = config.meta?.cardBrands,
                onBrandsChange = { newBrands ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.CARD_BRANDS,
                        newBrands
                    )
                }
            )
        }

        ConfigComponent.CO_BRAND_NAMES -> {
            ConfigCoBrandNamesField(
                label = stringResource(R.string.label_co_brand_names_optional),
                coBrandNames = config.meta?.coBrandNames,
                onCoBrandNamesChange = { newNames ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.CO_BRAND_NAMES,
                        newNames
                    )
                }
            )
        }

        ConfigComponent.DPA_DATA -> {
            ConfigDPADataSection(
                currentDPAData = config.meta?.dpaData,
                onDPADataChange = { newDPAData ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.DPA_DATA,
                        newDPAData
                    )
                }
            )
        }

        ConfigComponent.DPA_TRANSACTION_OPTIONS -> {
            ConfigDPATransactionOptionsSection(
                currentOptions = config.meta?.dpaTransactionOptions,
                onOptionsChange = { newOptions ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.DPA_TRANSACTION_OPTIONS,
                        newOptions
                    )
                }
            )
        }

        ConfigComponent.CUSTOMER -> {
            ConfigCustomerSection(
                currentCustomer = config.meta?.customer,
                onCustomerChange = { newCustomer ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.CUSTOMER,
                        newCustomer
                    )
                }
            )
        }

        // Handle DPA Transaction Options sub-components (backward compatibility)
        ConfigComponent.DPA_BILLING_PREFERENCE -> {
            val currentOptions = config.meta?.dpaTransactionOptions
            EnumDropdown(
                label = stringResource(R.string.label_dpa_billing_preference_optional),
                options = ClickToPayDPAShippingBillingPreference.entries,
                selectedOption = currentOptions?.dpaBillingPreference
                    ?: ClickToPayDPAShippingBillingPreference.NONE,
                onOptionSelected = { newPreference ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.DPA_BILLING_PREFERENCE,
                        newPreference
                    )
                },
                displayText = { it.name }
            )
        }

        ConfigComponent.ORDER_TYPE -> {
            val currentOptions = config.meta?.dpaTransactionOptions
            EnumDropdown(
                label = stringResource(R.string.label_order_type_optional),
                options = ClickToPayOrderType.entries,
                selectedOption = currentOptions?.orderType
                    ?: ClickToPayOrderType.SPLIT_SHIPMENT,
                onOptionSelected = { newOrderType ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ORDER_TYPE,
                        newOrderType
                    )
                },
                displayText = { it.name }
            )
        }

        ConfigComponent.THREE_DS_PREFERENCE -> {
            TextField(
                label = stringResource(R.string.label_3ds_preference_optional),
                value = config.meta?.dpaTransactionOptions?.threeDSPreference ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.THREE_DS_PREFERENCE,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.CONFIRM_PAYMENT -> {
            BooleanField(
                label = stringResource(R.string.label_confirm_payment_optional),
                value = config.meta?.dpaTransactionOptions?.confirmPayment ?: false,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.CONFIRM_PAYMENT,
                        if (newValue) true else null
                    )
                }
            )
        }

        ConfigComponent.PAYMENT_OPTIONS -> {
            ConfigPaymentOptionsField(
                label = stringResource(R.string.label_payment_options_optional),
                paymentOptions = config.meta?.dpaTransactionOptions?.paymentOptions,
                onPaymentOptionsChange = { newOptions ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.PAYMENT_OPTIONS,
                        newOptions
                    )
                }
            )
        }

        ConfigComponent.TRANSACTION_AMOUNT_VALUE -> {
            val currentAmount = config.meta?.dpaTransactionOptions?.transactionAmount
            TextField(
                label = stringResource(R.string.label_transaction_amount_optional),
                value = currentAmount?.transactionAmount?.toString() ?: "",
                onValueChange = { newValue ->
                    val updatedAmount = if (newValue.isNotBlank()) {
                        try {
                            val amount = BigDecimal(newValue)
                            TransactionAmount(
                                transactionAmount = amount,
                                transactionCurrencyCode = currentAmount?.transactionCurrencyCode
                            )
                        } catch (e: NumberFormatException) {
                            currentAmount
                        }
                    } else {
                        currentAmount?.transactionCurrencyCode?.let {
                            TransactionAmount(
                                transactionAmount = null,
                                transactionCurrencyCode = it
                            )
                        } ?: null
                    }
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.TRANSACTION_AMOUNT_VALUE,
                        updatedAmount
                    )
                }
            )
        }

        ConfigComponent.TRANSACTION_CURRENCY_CODE -> {
            val currentAmount = config.meta?.dpaTransactionOptions?.transactionAmount
            val currentCurrency =
                currentAmount?.transactionCurrencyCode?.let { code ->
                    CurrencyCode.fromCode(code)
                } ?: CurrencyCode.USD
            EnumDropdown(
                label = stringResource(R.string.label_transaction_currency_code_optional),
                options = CurrencyCode.entries,
                selectedOption = currentCurrency,
                onOptionSelected = { newCurrency ->
                    val updatedAmount =
                        TransactionAmount(
                            transactionAmount = currentAmount?.transactionAmount,
                            transactionCurrencyCode = newCurrency.code
                        )
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.TRANSACTION_CURRENCY_CODE,
                        updatedAmount
                    )
                },
                displayText = { it.code }
            )
        }

        // Handle Customer sub-components (backward compatibility)
        ConfigComponent.CUSTOMER_EMAIL -> {
            TextField(
                label = stringResource(R.string.label_email_optional),
                value = config.meta?.customer?.email ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.CUSTOMER_EMAIL,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.CUSTOMER_FIRST_NAME -> {
            TextField(
                label = stringResource(R.string.label_first_name_optional),
                value = config.meta?.customer?.firstName ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.CUSTOMER_FIRST_NAME,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.CUSTOMER_LAST_NAME -> {
            TextField(
                label = stringResource(R.string.label_last_name_optional),
                value = config.meta?.customer?.lastName ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.CUSTOMER_LAST_NAME,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.CUSTOMER_PHONE_COUNTRY_CODE -> {
            TextField(
                label = stringResource(R.string.label_phone_country_code_optional),
                value = config.meta?.customer?.phone?.countryCode ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.CUSTOMER_PHONE_COUNTRY_CODE,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.CUSTOMER_PHONE_NUMBER -> {
            TextField(
                label = stringResource(R.string.label_phone_number_optional),
                value = config.meta?.customer?.phone?.phone ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.CUSTOMER_PHONE_NUMBER,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        // Handle DPA Data sub-components (backward compatibility)
        ConfigComponent.DPA_ADDRESS -> {
            TextField(
                label = stringResource(R.string.label_dpa_address_optional),
                value = config.meta?.dpaData?.dpaAddress ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.DPA_ADDRESS,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.DPA_EMAIL_ADDRESS -> {
            TextField(
                label = stringResource(R.string.label_dpa_email_address_optional),
                value = config.meta?.dpaData?.dpaEmailAddress ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.DPA_EMAIL_ADDRESS,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.DPA_PHONE_COUNTRY_CODE -> {
            TextField(
                label = stringResource(R.string.label_dpa_phone_country_code_optional),
                value = config.meta?.dpaData?.dpaPhoneNumber?.countryCode ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.DPA_PHONE_COUNTRY_CODE,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.DPA_PHONE_NUMBER_FIELD -> {
            TextField(
                label = stringResource(R.string.label_dpa_phone_number_optional),
                value = config.meta?.dpaData?.dpaPhoneNumber?.phoneNumber ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.DPA_PHONE_NUMBER_FIELD,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.DPA_LOGO_URI -> {
            TextField(
                label = stringResource(R.string.label_dpa_logo_uri_optional),
                value = config.meta?.dpaData?.dpaLogoUri ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.DPA_LOGO_URI,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.DPA_SUPPORTED_EMAIL_ADDRESS -> {
            TextField(
                label = stringResource(R.string.label_dpa_supported_email_address_optional),
                value = config.meta?.dpaData?.dpaSupportedEmailAddress ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.DPA_SUPPORTED_EMAIL_ADDRESS,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.DPA_URI -> {
            TextField(
                label = stringResource(R.string.label_dpa_uri_optional),
                value = config.meta?.dpaData?.dpaUri ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.DPA_URI,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.DPA_SUPPORT_URI -> {
            TextField(
                label = stringResource(R.string.label_dpa_support_uri_optional),
                value = config.meta?.dpaData?.dpaSupportUri ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.DPA_SUPPORT_URI,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.DPA_APPLICATION_TYPE -> {
            val currentType = config.meta?.dpaData?.applicationType
                ?: com.paydock.feature.src.domain.model.integration.meta.enum.ApplicationType.WEB_BROWSER
            EnumDropdown(
                label = stringResource(R.string.label_dpa_application_type_optional),
                options = ApplicationType.entries,
                selectedOption = currentType,
                onOptionSelected = { newType ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.DPA_APPLICATION_TYPE,
                        newType
                    )
                },
                displayText = { it.name }
            )
        }

        else -> {}
    }
}

