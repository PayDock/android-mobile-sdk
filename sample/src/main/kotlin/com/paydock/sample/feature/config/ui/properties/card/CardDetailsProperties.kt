package com.paydock.sample.feature.config.ui.properties.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.paydock.feature.card.domain.model.integration.CardDetailsWidgetConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BooleanField
import com.paydock.sample.designsystems.components.fields.TextField
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.config.models.ConfigComponent
import com.paydock.sample.feature.config.ui.components.properties.card.ConfigCardSchemesField
import com.paydock.sample.feature.config.ui.components.section.ConfigSaveCardSection
import com.paydock.sample.feature.config.ui.components.section.ConfigSchemeSupportSection
import com.paydock.sample.feature.config.ui.components.section.ConfigStoreSecurityCodeSection
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun CardDetailsProperties(
    config: CardDetailsWidgetConfig,
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

        ConfigComponent.GATEWAY_ID -> {
            TextField(
                label = stringResource(R.string.label_gateway_id_optional),
                value = config.gatewayId ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.GATEWAY_ID,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.COLLECT_CARDHOLDER_NAME -> {
            BooleanField(
                label = stringResource(R.string.label_collect_cardholder_name),
                value = config.collectCardholderName,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.COLLECT_CARDHOLDER_NAME,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.ACTIVE_PRIMARY_BUTTON -> {
            BooleanField(
                label = stringResource(R.string.label_active_primary_button),
                value = config.activePrimaryButton,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ACTIVE_PRIMARY_BUTTON,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.SHOW_SUBMIT_BUTTON -> {
            BooleanField(
                label = stringResource(R.string.label_show_submit_button),
                value = config.showSubmitButton,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.SHOW_SUBMIT_BUTTON,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.ALLOW_SAVE_CARD -> {
            ConfigSaveCardSection(
                currentSaveCardConfig = config.allowSaveCard,
                onSaveCardConfigChange = { newSaveCardConfig ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ALLOW_SAVE_CARD,
                        newSaveCardConfig
                    )
                }
            )
        }

        ConfigComponent.STORE_SECURITY_CODE -> {
            ConfigStoreSecurityCodeSection(
                currentStoreSecurityCode = config.storeSecurityCode,
                onStoreSecurityCodeChange = { newStoreSecurityCode ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.STORE_SECURITY_CODE,
                        newStoreSecurityCode
                    )
                }
            )
        }

        ConfigComponent.SCHEME_SUPPORT -> {
            ConfigSchemeSupportSection(
                currentSchemeSupport = config.schemeSupport,
                onSchemeSupportChange = { newSchemeSupport ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.SCHEME_SUPPORT,
                        newSchemeSupport
                    )
                }
            )
        }

        // Handle sub-components (individual fields) for backward compatibility
        ConfigComponent.ENABLE_VALIDATION -> {
            BooleanField(
                label = stringResource(R.string.label_enable_scheme_validation),
                value = config.schemeSupport.enableValidation,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ENABLE_VALIDATION,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.SUPPORTED_SCHEMES -> {
            ConfigCardSchemesField(
                label = stringResource(R.string.label_supported_card_schemes),
                selectedSchemes = config.schemeSupport.supportedSchemes,
                onSchemesChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.SUPPORTED_SCHEMES,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.SHOW_SCHEME_LIST -> {
            BooleanField(
                label = stringResource(R.string.label_show_scheme_list),
                value = config.schemeSupport.showSchemeList,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.SHOW_SCHEME_LIST,
                        newValue
                    )
                }
            )
        }

        else -> {}
    }
}

