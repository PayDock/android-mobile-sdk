package com.paydock.sample.feature.config

import androidx.lifecycle.ViewModel
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.feature.afterpay.domain.model.integration.AfterpaySDKConfig
import com.paydock.feature.card.domain.model.integration.CardDetailsWidgetConfig
import com.paydock.feature.card.domain.model.integration.GiftCardWidgetConfig
import com.paydock.feature.card.domain.model.integration.SaveCardConfig
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.model.integration.enums.CardType
import com.paydock.feature.colespay.integration.ColesPayWidgetConfig
import com.paydock.feature.googlepay.domain.model.integration.GooglePayWidgetConfig
import com.paydock.feature.googlepay.util.PaymentsUtil
import com.paydock.feature.paypal.checkout.domain.model.integration.PayPalWidgetConfig
import com.paydock.feature.paypal.vault.domain.model.integration.PayPalVaultConfig
import com.paydock.feature.src.domain.model.integration.ClickToPayWidgetConfig
import com.paydock.feature.src.domain.model.integration.meta.ClickToPayDPAData
import com.paydock.feature.src.domain.model.integration.meta.ClickToPayDPAOptions
import com.paydock.feature.src.domain.model.integration.meta.ClickToPayMeta
import com.paydock.feature.src.domain.model.integration.meta.Customer
import com.paydock.feature.src.domain.model.integration.meta.PaymentOption
import com.paydock.feature.src.domain.model.integration.meta.Phone
import com.paydock.feature.src.domain.model.integration.meta.PhoneNumber
import com.paydock.feature.src.domain.model.integration.meta.enum.ApplicationType
import com.paydock.feature.src.domain.model.integration.meta.enum.CardBrands
import com.paydock.feature.src.domain.model.integration.meta.enum.CheckoutExperience
import com.paydock.feature.src.domain.model.integration.meta.enum.ClickToPayDPAShippingBillingPreference
import com.paydock.feature.src.domain.model.integration.meta.enum.ClickToPayOrderType
import com.paydock.feature.src.domain.model.integration.meta.enum.Services
import com.paydock.feature.src.domain.model.integration.meta.enum.UnacceptedCardType
import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AU_COUNTRY_CODE
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.core.MERCHANT_NAME
import com.paydock.sample.feature.config.data.CheckoutConfigRepository
import com.paydock.sample.feature.config.data.GlobalConfigRepository
import com.paydock.sample.feature.config.data.WidgetConfigRepository
import com.paydock.sample.feature.config.models.ConfigComponent
import com.paydock.sample.feature.config.models.PaymentProcessor
import com.paydock.sample.feature.config.models.ThreeDSService
import com.paydock.sample.feature.widgets.ui.models.WidgetType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.paydock.feature.googlepay.domain.model.integration.GooglePayBillingAddressParameters
import com.paydock.feature.googlepay.domain.model.integration.GooglePayShippingAddressParameters
import java.math.BigDecimal
import java.util.Locale
import javax.inject.Inject

/**
 * ConfigViewModel manages three types of configurations:
 *
 * 1. **GlobalConfig** - Applies to all widgets and flows
 *    - API Access Token
 *
 * 2. **CheckoutConfig** - Specific to checkout flow
 *    - Payment Processor preferences (MPGS/CyberSource)
 *    - Processor-specific settings (Service IDs, 3DS services)
 *
 * 3. **WidgetConfig** - Widget-specific settings
 *    - Cart Amount & Currency
 *
 * 4. **Widget-specific configs** - Individual widget configurations
 *    - Card Details, Gift Card, PayPal, Coles Pay, etc.
 *    - Each widget has its own configuration state
 */
@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val globalConfigRepository: GlobalConfigRepository,
    private val checkoutConfigRepository: CheckoutConfigRepository,
    private val widgetConfigRepository: WidgetConfigRepository
) : ViewModel() {

    // ========== Global Config ==========
    // Global Config - delegate to repository
    val globalConfig: StateFlow<GlobalConfig> =
        globalConfigRepository.globalConfig

    // ========== Checkout Config ==========
    // Checkout Config - delegate to repository
    val checkoutConfig: StateFlow<CheckoutConfig> =
        checkoutConfigRepository.checkoutConfig

    // ========== Widget Config ==========
    // Widget Config - delegate to repository
    val widgetConfig: StateFlow<WidgetConfig> =
        widgetConfigRepository.widgetConfig

    // ========== Widget-specific Configs ==========

    // Helper function to get access token for widget configs
    // Widgets default to ACCESS_TOKEN_WIDGET, but can use global config if set
    private fun getAccessToken(): String {
        val globalToken = globalConfigRepository.globalConfig.value.apiAccessToken
        // Use global token if it's different from default (user has set it)
        return if (globalToken != BuildConfig.ACCESS_TOKEN_API && globalToken.isNotBlank()) {
            globalToken
        } else {
            BuildConfig.ACCESS_TOKEN_WIDGET
        }
    }

    // Helper function to create default SaveCardConfig with privacy policy
    private fun createDefaultSaveCardConfig(): SaveCardConfig {
        return SaveCardConfig(
            privacyPolicyConfig = SaveCardConfig.PrivacyPolicyConfig(
                privacyPolicyURL = "https://www.paydock.com/privacy"
            )
        )
    }

    // Helper function to create default configs
    private fun createDefaultCardDetailsConfig(): CardDetailsWidgetConfig {
        return CardDetailsWidgetConfig(
            accessToken = getAccessToken(),
            gatewayId = BuildConfig.SERVICE_ID_MPGS,
            collectCardholderName = true,
            allowSaveCard = createDefaultSaveCardConfig(),
            storeSecurityCode = true,
            schemeSupport = SupportedSchemeConfig(
                supportedSchemes = CardType.entries.toSet(),
                enableValidation = true
            ),
            activePrimaryButton = true
        )
    }

    private fun createDefaultGiftCardConfig(): GiftCardWidgetConfig {
        return GiftCardWidgetConfig(
            accessToken = getAccessToken()
        )
    }

    private fun createDefaultPayPalConfig(): PayPalWidgetConfig {
        return PayPalWidgetConfig(
            accessToken = getAccessToken(),
            gatewayId = BuildConfig.SERVICE_ID_PAYPAL
        )
    }

    private fun createDefaultColesPayConfig(): ColesPayWidgetConfig {
        return ColesPayWidgetConfig(
            clientId = BuildConfig.WALLET_ID_COLES_PAY
        )
    }

    private fun createDefaultClickToPayConfig(): ClickToPayWidgetConfig {
        return ClickToPayWidgetConfig(
            serviceId = BuildConfig.SERVICE_ID_CLICK_TO_PAY,
            meta = ClickToPayMeta(disableSummaryScreen = true),
            accessToken = getAccessToken()
        )
    }

    private fun createDefaultAfterpayConfig(): AfterpaySDKConfig {
        return AfterpaySDKConfig(
            options = AfterpaySDKConfig.CheckoutOptions(
                shippingOptionRequired = true,
                enableSingleShippingOptionUpdate = true
            )
        )
    }

    private fun createDefaultPayPalVaultConfig(): PayPalVaultConfig {
        return PayPalVaultConfig(
            accessToken = getAccessToken(),
            gatewayId = BuildConfig.SERVICE_ID_PAYPAL
        )
    }

    private fun createDefaultZipConfig(): ZipWidgetConfig {
        val widgetConfig = widgetConfigRepository.widgetConfig.value
        val globalConfig = globalConfigRepository.globalConfig.value
        return ZipWidgetConfig(
            accessToken = getAccessToken(),
            gatewayId = BuildConfig.SERVICE_ID_ZIP,
            amount = widgetConfig.cartAmount,
            currency = globalConfig.currencyCode,
            firstName = "Joshua",
            lastName = "Wood",
            email = "joshuawood@hotmail.com.au",
            phone = "+61412345678",
            tokenize = true,
            gender = "male",
            dateOfBirth = null,
            shippingType = "delivery",
            billing = ZipWidgetConfig.Address(
                firstName = "Joshua",
                lastName = "Wood",
                line1 = "Suite 660",
                line2 = "test",
                city = "Sydney",
                state = "LA",
                postcode = "3223",
                country = "AU"
            ),
            shipping = defaultZipShippingAddress()
        )
    }

    private fun defaultZipShippingAddress(): ZipWidgetConfig.Address =
        ZipWidgetConfig.Address(
            firstName = "Joshua",
            lastName = "Wood",
            line1 = "Suite 660",
            line2 = "822 Ruiz Square",
            city = "Sydney",
            state = "LA",
            postcode = "3223",
            country = "AU"
        )

    private fun createDefaultGooglePayConfig(): GooglePayWidgetConfig {
        return GooglePayWidgetConfig(
            accessToken = BuildConfig.ACCESS_TOKEN_WIDGET,
            serviceId = BuildConfig.SERVICE_ID_GOOGLE_PAY,
            isReadyToPayRequest = PaymentsUtil.createIsReadyToPayRequest(),
            paymentRequest = createGooglePayPaymentRequest(
                amount = widgetConfig.value.cartAmount,
                currencyCode = globalConfig.value.currencyCode
            )
        )
    }

    private fun createGooglePayPaymentRequest(
        amount: BigDecimal,
        currencyCode: String
    ) = PaymentsUtil.createGooglePayRequest(
        amount = amount,
        amountLabel = "Goodies",
        currencyCode = currencyCode,
        countryCode = AU_COUNTRY_CODE,
        merchantName = MERCHANT_NAME,
        merchantIdentifier = BuildConfig.MERCHANT_ID_GOOGLE_PAY,
        shippingAddressRequired = true,
        shippingAddressParameters = GooglePayShippingAddressParameters(
            allowedCountryCodes = listOf("US", "GB", "AU")
        ),
        emailRequired = true,
        phoneNumberRequired = true
    )

    // Card Details Config - initialized with defaults
    private val _cardDetailsWidgetConfig = MutableStateFlow(createDefaultCardDetailsConfig())
    val cardDetailsWidgetConfig: StateFlow<CardDetailsWidgetConfig> =
        _cardDetailsWidgetConfig.asStateFlow()

    // Gift Card Config - initialized with defaults
    private val _giftCardWidgetConfig = MutableStateFlow(createDefaultGiftCardConfig())
    val giftCardWidgetConfig: StateFlow<GiftCardWidgetConfig> =
        _giftCardWidgetConfig.asStateFlow()

    // Google Pay Config
    private val _googlePayWidgetConfig =
        MutableStateFlow<GooglePayWidgetConfig>(createDefaultGooglePayConfig())
    val googlePayWidgetConfig: StateFlow<GooglePayWidgetConfig> =
        _googlePayWidgetConfig.asStateFlow()

    // PayPal Config - initialized with defaults
    private val _paypalWidgetConfig = MutableStateFlow(createDefaultPayPalConfig())
    val paypalWidgetConfig: StateFlow<PayPalWidgetConfig> =
        _paypalWidgetConfig.asStateFlow()

    // Coles Pay Config - initialized with defaults
    private val _colesPayWidgetConfig = MutableStateFlow(createDefaultColesPayConfig())
    val colesPayWidgetConfig: StateFlow<ColesPayWidgetConfig> =
        _colesPayWidgetConfig.asStateFlow()

    // Click to Pay Config - initialized with defaults
    private val _clickToPayWidgetConfig = MutableStateFlow(createDefaultClickToPayConfig())
    val clickToPayWidgetConfig: StateFlow<ClickToPayWidgetConfig> =
        _clickToPayWidgetConfig.asStateFlow()

    // Afterpay Config - initialized with defaults
    private val _afterpayWidgetConfig = MutableStateFlow(createDefaultAfterpayConfig())
    val afterpayWidgetConfig: StateFlow<AfterpaySDKConfig> =
        _afterpayWidgetConfig.asStateFlow()

    // PayPal Vault Config - initialized with defaults
    private val _paypalVaultWidgetConfig = MutableStateFlow(createDefaultPayPalVaultConfig())
    val paypalVaultWidgetConfig: StateFlow<PayPalVaultConfig> =
        _paypalVaultWidgetConfig.asStateFlow()

    // Zip Config - initialized with defaults
    private val _zipWidgetConfig = MutableStateFlow(createDefaultZipConfig())
    val zipWidgetConfig: StateFlow<ZipWidgetConfig> =
        _zipWidgetConfig.asStateFlow()

    // Address Config - nullable as it's optional pre-fill
    private val _addressConfig = MutableStateFlow<BillingAddress?>(null)
    val addressConfig: StateFlow<BillingAddress?> =
        _addressConfig.asStateFlow()

    // --- Global Config methods ---
    fun updateGlobalAccessToken(accessToken: String?) {
        val newToken =
            accessToken?.takeIf { token -> token.isNotBlank() } ?: BuildConfig.ACCESS_TOKEN_API
        globalConfigRepository.updateAccessToken(newToken)
        // Update all widget configs that use access token with new global token if they're using defaults
        updateConfigsWithGlobalAccessToken(newToken)
    }

    fun updateWidgetCartAmount(amount: BigDecimal?) {
        val newAmount = amount ?: BigDecimal(0)
        widgetConfigRepository.updateCartAmount(newAmount)
        // Update Zip config with new cart amount
        _zipWidgetConfig.update { current ->
            current.copy(amount = newAmount)
        }
        // Update Google Pay config paymentRequest with new amount (used by Google Pay SDK for display)
        _googlePayWidgetConfig.update { current ->
            current.copy(
                paymentRequest = current.paymentRequest.copy(
                    transactionInfo = current.paymentRequest.transactionInfo.copy(
                        totalPrice = PaymentsUtil.formatAmountForGooglePay(newAmount)
                    )
                )
            )
        }
    }

    fun updateGlobalCartCurrency(currency: String?) {
        val newCurrency = currency?.takeIf { curr -> curr.isNotBlank() } ?: AU_CURRENCY_CODE
        globalConfigRepository.updateCartCurrency(newCurrency)
        // Update Zip config with new cart currency
        _zipWidgetConfig.update { current ->
            current.copy(currency = newCurrency)
        }
        // Update Google Pay config paymentRequest with new currency (used by Google Pay SDK for display)
        _googlePayWidgetConfig.update { current ->
            current.copy(
                paymentRequest = current.paymentRequest.copy(
                    transactionInfo = current.paymentRequest.transactionInfo.copy(
                        currencyCode = newCurrency
                    )
                )
            )
        }
    }

    // --- Checkout Config methods ---
    fun updateMpgsServiceId(serviceId: String) {
        checkoutConfigRepository.updateMpgsServiceId(serviceId)
    }

    fun updateMpgsThreeDSService(service: ThreeDSService) {
        checkoutConfigRepository.updateMpgsThreeDSService(service)
    }

    fun updateCyberSourceServiceId(serviceId: String) {
        checkoutConfigRepository.updateCyberSourceServiceId(serviceId)
    }

    fun updateCyberSourceThreeDSService(service: ThreeDSService) {
        checkoutConfigRepository.updateCyberSourceThreeDSService(service)
    }

    fun updatePreferredProcessor(processor: PaymentProcessor?) {
        checkoutConfigRepository.updatePreferredProcessor(processor)
    }

    // Helper to update configs that should use global access token
    private fun updateConfigsWithGlobalAccessToken(accessToken: String) {
        // Only update if configs are still at defaults (using BuildConfig.ACCESS_TOKEN_WIDGET)
        // This prevents overwriting user-configured values
        _cardDetailsWidgetConfig.update { current ->
            if (current.accessToken == BuildConfig.ACCESS_TOKEN_WIDGET) {
                current.copy(accessToken = accessToken)
            } else {
                current
            }
        }
        _giftCardWidgetConfig.update { current ->
            if (current.accessToken == BuildConfig.ACCESS_TOKEN_WIDGET) {
                current.copy(accessToken = accessToken)
            } else {
                current
            }
        }
        _paypalWidgetConfig.update { current ->
            if (current.accessToken == BuildConfig.ACCESS_TOKEN_WIDGET) {
                current.copy(accessToken = accessToken)
            } else {
                current
            }
        }
        _clickToPayWidgetConfig.update { current ->
            if (current.accessToken == BuildConfig.ACCESS_TOKEN_WIDGET) {
                current.copy(accessToken = accessToken)
            } else {
                current
            }
        }
        _paypalVaultWidgetConfig.update { current ->
            if (current.accessToken == BuildConfig.ACCESS_TOKEN_WIDGET) {
                current.copy(accessToken = accessToken)
            } else {
                current
            }
        }
        _zipWidgetConfig.update { current ->
            if (current.accessToken == BuildConfig.ACCESS_TOKEN_WIDGET) {
                current.copy(accessToken = accessToken)
            } else {
                current
            }
        }
    }

    // --- Initialization methods (optional - can override defaults) ---
    fun updateInitialCardDetailsConfig(config: CardDetailsWidgetConfig) {
        _cardDetailsWidgetConfig.update { config }
    }

    fun updateInitialGiftCardConfig(config: GiftCardWidgetConfig) {
        _giftCardWidgetConfig.update { config }
    }

    fun updateInitialGooglePayConfig(config: GooglePayWidgetConfig) {
        _googlePayWidgetConfig.update { config }
    }

    fun updateInitialPayPalConfig(config: PayPalWidgetConfig) {
        _paypalWidgetConfig.update { config }
    }

    fun updateInitialColesPayConfig(config: ColesPayWidgetConfig) {
        _colesPayWidgetConfig.update { config }
    }

    fun updateInitialClickToPayConfig(config: ClickToPayWidgetConfig) {
        _clickToPayWidgetConfig.update { config }
    }

    fun updateInitialAfterpayConfig(config: AfterpaySDKConfig) {
        _afterpayWidgetConfig.update { config }
    }

    fun updateInitialPayPalVaultConfig(config: PayPalVaultConfig) {
        _paypalVaultWidgetConfig.update { config }
    }

    fun updateInitialZipConfig(config: ZipWidgetConfig) {
        _zipWidgetConfig.update { config }
    }

    fun updateInitialAddressConfig(address: BillingAddress?) {
        _addressConfig.update { address }
    }

    // --- Update methods for specific config properties ---
    fun updateWidgetConfig(
        widgetType: WidgetType,
        component: ConfigComponent,
        newValue: Any?
    ) {
        when (widgetType) {
            WidgetType.CARD_DETAILS -> {
                val current = _cardDetailsWidgetConfig.value
                val updatedConfig = when (component) {
                    ConfigComponent.ACCESS_TOKEN -> current.copy(accessToken = newValue as String)
                    ConfigComponent.GATEWAY_ID -> current.copy(gatewayId = newValue as? String)
                    ConfigComponent.COLLECT_CARDHOLDER_NAME -> current.copy(
                        collectCardholderName = newValue as Boolean
                    )

                    ConfigComponent.ACTIVE_PRIMARY_BUTTON -> current.copy(
                        activePrimaryButton = newValue as Boolean
                    )

                    ConfigComponent.ENABLE_SAVE_CARD -> {
                        val isEnabled = newValue as Boolean
                        current.copy(
                            allowSaveCard = if (isEnabled) createDefaultSaveCardConfig() else null
                        )
                    }

                    ConfigComponent.ALLOW_SAVE_CARD -> current.copy(allowSaveCard = newValue as? SaveCardConfig)

                    ConfigComponent.STORE_SECURITY_CODE -> {
                        current.copy(storeSecurityCode = newValue as? Boolean)
                    }

                    ConfigComponent.SCHEME_SUPPORT -> current.copy(schemeSupport = newValue as SupportedSchemeConfig)
                    ConfigComponent.SAVE_CARD_CONSENT_TEXT -> {
                        val saveCardConfig = current.allowSaveCard
                        if (saveCardConfig != null) {
                            current.copy(
                                allowSaveCard = saveCardConfig.copy(consentText = newValue as String)
                            )
                        } else {
                            // Initialize SaveCardConfig if it doesn't exist
                            current.copy(
                                allowSaveCard = SaveCardConfig(consentText = newValue as String)
                            )
                        }
                    }

                    ConfigComponent.PRIVACY_POLICY_TEXT -> {
                        val saveCardConfig = current.allowSaveCard
                        val privacyConfig = saveCardConfig?.privacyPolicyConfig
                        when {
                            saveCardConfig != null && privacyConfig != null -> {
                                current.copy(
                                    allowSaveCard = saveCardConfig.copy(
                                        privacyPolicyConfig = privacyConfig.copy(
                                            privacyPolicyText = newValue as String
                                        )
                                    )
                                )
                            }

                            saveCardConfig != null && privacyConfig == null -> {
                                // Initialize PrivacyPolicyConfig if it doesn't exist
                                current.copy(
                                    allowSaveCard = saveCardConfig.copy(
                                        privacyPolicyConfig = SaveCardConfig.PrivacyPolicyConfig(
                                            privacyPolicyText = newValue as String,
                                            privacyPolicyURL = ""
                                        )
                                    )
                                )
                            }

                            else -> {
                                // Initialize both SaveCardConfig and PrivacyPolicyConfig
                                current.copy(
                                    allowSaveCard = SaveCardConfig(
                                        privacyPolicyConfig = SaveCardConfig.PrivacyPolicyConfig(
                                            privacyPolicyText = newValue as String,
                                            privacyPolicyURL = ""
                                        )
                                    )
                                )
                            }
                        }
                    }

                    ConfigComponent.PRIVACY_POLICY_URL -> {
                        val saveCardConfig = current.allowSaveCard
                        val privacyConfig = saveCardConfig?.privacyPolicyConfig
                        when {
                            saveCardConfig != null && privacyConfig != null -> {
                                current.copy(
                                    allowSaveCard = saveCardConfig.copy(
                                        privacyPolicyConfig = privacyConfig.copy(
                                            privacyPolicyURL = newValue as String
                                        )
                                    )
                                )
                            }

                            saveCardConfig != null && privacyConfig == null -> {
                                // Initialize PrivacyPolicyConfig if it doesn't exist
                                current.copy(
                                    allowSaveCard = saveCardConfig.copy(
                                        privacyPolicyConfig = SaveCardConfig.PrivacyPolicyConfig(
                                            privacyPolicyText = "",
                                            privacyPolicyURL = newValue as String
                                        )
                                    )
                                )
                            }

                            else -> {
                                // Initialize both SaveCardConfig and PrivacyPolicyConfig
                                current.copy(
                                    allowSaveCard = SaveCardConfig(
                                        privacyPolicyConfig = SaveCardConfig.PrivacyPolicyConfig(
                                            privacyPolicyText = "",
                                            privacyPolicyURL = newValue as String
                                        )
                                    )
                                )
                            }
                        }
                    }

                    ConfigComponent.ENABLE_VALIDATION -> {
                        current.copy(
                            schemeSupport = current.schemeSupport.copy(
                                enableValidation = newValue as Boolean
                            )
                        )
                    }

                    ConfigComponent.SUPPORTED_SCHEMES -> {
                        current.copy(
                            schemeSupport = current.schemeSupport.copy(
                                supportedSchemes = newValue as? Set<CardType>
                            )
                        )
                    }

                    else -> current
                }
                _cardDetailsWidgetConfig.value = updatedConfig
            }

            WidgetType.GIFT_CARD -> {
                val current = _giftCardWidgetConfig.value
                val updatedConfig = when (component) {
                    ConfigComponent.ACCESS_TOKEN -> current.copy(accessToken = newValue as String)
                    ConfigComponent.STORE_PIN -> current.copy(storePin = newValue as Boolean)
                    else -> current
                }
                _giftCardWidgetConfig.value = updatedConfig
            }

            WidgetType.PAY_PAL -> {
                val current = _paypalWidgetConfig.value
                val updatedConfig = when (component) {
                    ConfigComponent.ACCESS_TOKEN -> current.copy(accessToken = newValue as String)
                    ConfigComponent.GATEWAY_ID -> current.copy(gatewayId = newValue as String)
                    ConfigComponent.REQUEST_SHIPPING -> current.copy(requestShipping = newValue as Boolean)
                    ConfigComponent.FUNDING_SOURCE -> current.copy(fundingSource = newValue as PayPalWidgetConfig.PayPalFundingSource)
                    else -> current
                }
                _paypalWidgetConfig.value = updatedConfig
            }

            WidgetType.GOOGLE_PAY -> {
                val current = _googlePayWidgetConfig.value
                val updatedConfig = when (component) {
                    ConfigComponent.ACCESS_TOKEN -> current.copy(accessToken = newValue as String)
                    ConfigComponent.SERVICE_ID -> current.copy(serviceId = newValue as String)
                    ConfigComponent.EMAIL_REQUIRED -> {
                        current.copy(
                            paymentRequest = current.paymentRequest.copy(
                                emailRequired = newValue as Boolean
                            )
                        )
                    }

                    ConfigComponent.PHONE_NUMBER_REQUIRED -> {
                        val phoneNumberRequired = newValue as Boolean

                        val updatedShippingAddressParameters =
                            current.paymentRequest.shippingAddressParameters?.copy(
                                phoneNumberRequired = phoneNumberRequired
                            ) ?: GooglePayShippingAddressParameters(
                                phoneNumberRequired = phoneNumberRequired
                            )

                        val updatedAllowedPaymentMethods =
                            current.paymentRequest.allowedPaymentMethods.map { method ->
                                val updatedBillingAddressParameters =
                                    method.parameters.billingAddressParameters?.copy(
                                        phoneNumberRequired = phoneNumberRequired
                                    ) ?: GooglePayBillingAddressParameters(
                                        phoneNumberRequired = phoneNumberRequired
                                    )

                                method.copy(
                                    parameters = method.parameters.copy(
                                        billingAddressParameters = updatedBillingAddressParameters
                                    )
                                )
                            }

                        current.copy(
                            paymentRequest = current.paymentRequest.copy(
                                shippingAddressParameters = updatedShippingAddressParameters,
                                allowedPaymentMethods = updatedAllowedPaymentMethods
                            )
                        )
                    }

                    ConfigComponent.SHIPPING_REQUIRED -> {
                        current.copy(
                            paymentRequest = current.paymentRequest.copy(
                                shippingAddressRequired = newValue as Boolean
                            )
                        )
                    }

                    ConfigComponent.BILLING_REQUIRED -> {
                        current.copy(
                            paymentRequest = current.paymentRequest.copy(
                                allowedPaymentMethods = current.paymentRequest.allowedPaymentMethods.map { method ->
                                    method.copy(
                                        parameters = method.parameters.copy(
                                            billingAddressRequired = newValue as Boolean
                                        )
                                    )
                                }
                            )
                        )
                    }

                    else -> current
                }
                _googlePayWidgetConfig.value = updatedConfig
            }

            WidgetType.COLES_PAY -> {
                val current = _colesPayWidgetConfig.value
                val updatedConfig = when (component) {
                    ConfigComponent.CLIENT_ID -> current.copy(clientId = newValue as String)
                    else -> current
                }
                _colesPayWidgetConfig.value = updatedConfig
            }

            WidgetType.CLICK_TO_PAY -> {
                val current = _clickToPayWidgetConfig.value
                val updatedConfig = when (component) {
                    ConfigComponent.SERVICE_ID -> current.copy(serviceId = newValue as String)
                    ConfigComponent.ACCESS_TOKEN -> current.copy(accessToken = newValue as String)

                    ConfigComponent.CLICK_TO_PAY_META -> {
                        // Update the entire ClickToPayMeta object
                        current.copy(meta = newValue as? ClickToPayMeta)
                    }

                    // Handle sub-components (individual fields)
                    ConfigComponent.DISABLE_SUMMARY_SCREEN -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        current.copy(
                            meta = currentMeta.copy(disableSummaryScreen = newValue as? Boolean)
                        )
                    }

                    ConfigComponent.CHECKOUT_EXPERIENCE -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        current.copy(
                            meta = currentMeta.copy(checkoutExperience = newValue as? CheckoutExperience)
                        )
                    }

                    ConfigComponent.SERVICES -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        current.copy(
                            meta = currentMeta.copy(services = newValue as? Services)
                        )
                    }

                    ConfigComponent.UNACCEPTED_CARD_TYPE -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        current.copy(
                            meta = currentMeta.copy(unacceptedCardType = newValue as? UnacceptedCardType)
                        )
                    }

                    ConfigComponent.CARD_BRANDS -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        current.copy(
                            meta = currentMeta.copy(cardBrands = newValue as? List<CardBrands>)
                        )
                    }

                    ConfigComponent.CO_BRAND_NAMES -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        current.copy(
                            meta = currentMeta.copy(coBrandNames = newValue as? List<String>)
                        )
                    }

                    ConfigComponent.DPA_DATA -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        current.copy(
                            meta = currentMeta.copy(dpaData = newValue as? ClickToPayDPAData)
                        )
                    }

                    // Handle DPA Data sub-components
                    ConfigComponent.DPA_ADDRESS -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAData = currentMeta.dpaData ?: ClickToPayDPAData()
                        current.copy(
                            meta = currentMeta.copy(
                                dpaData = currentDPAData.copy(
                                    dpaAddress = (newValue as? String)?.ifEmpty { null }
                                )
                            )
                        )
                    }

                    ConfigComponent.DPA_EMAIL_ADDRESS -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAData = currentMeta.dpaData ?: ClickToPayDPAData()
                        current.copy(
                            meta = currentMeta.copy(
                                dpaData = currentDPAData.copy(
                                    dpaEmailAddress = (newValue as? String)?.ifEmpty { null }
                                )
                            )
                        )
                    }

                    ConfigComponent.DPA_PHONE_NUMBER -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAData = currentMeta.dpaData ?: ClickToPayDPAData()
                        current.copy(
                            meta = currentMeta.copy(
                                dpaData = currentDPAData.copy(
                                    dpaPhoneNumber = newValue as? PhoneNumber
                                )
                            )
                        )
                    }

                    ConfigComponent.DPA_PHONE_COUNTRY_CODE -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAData = currentMeta.dpaData ?: ClickToPayDPAData()
                        val currentPhone = currentDPAData.dpaPhoneNumber
                        val newCountryCode = (newValue as? String)?.takeIf { it.isNotBlank() }
                        val currentPhoneNumber = currentPhone?.phoneNumber ?: ""

                        val updatedPhone =
                            if (newCountryCode != null && currentPhoneNumber.isNotBlank()) {
                                PhoneNumber(
                                    countryCode = newCountryCode,
                                    phoneNumber = currentPhoneNumber
                                )
                            } else {
                                null
                            }
                        current.copy(
                            meta = currentMeta.copy(
                                dpaData = currentDPAData.copy(dpaPhoneNumber = updatedPhone)
                            )
                        )
                    }

                    ConfigComponent.DPA_PHONE_NUMBER_FIELD -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAData = currentMeta.dpaData ?: ClickToPayDPAData()
                        val currentPhone = currentDPAData.dpaPhoneNumber
                        val newPhoneNumber = (newValue as? String)?.takeIf { it.isNotBlank() }
                        val currentCountryCode = currentPhone?.countryCode ?: ""

                        val updatedPhone =
                            if (currentCountryCode.isNotBlank() && newPhoneNumber != null) {
                                PhoneNumber(
                                    countryCode = currentCountryCode,
                                    phoneNumber = newPhoneNumber
                                )
                            } else {
                                null
                            }
                        current.copy(
                            meta = currentMeta.copy(
                                dpaData = currentDPAData.copy(dpaPhoneNumber = updatedPhone)
                            )
                        )
                    }

                    ConfigComponent.DPA_LOGO_URI -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAData = currentMeta.dpaData ?: ClickToPayDPAData()
                        current.copy(
                            meta = currentMeta.copy(
                                dpaData = currentDPAData.copy(
                                    dpaLogoUri = (newValue as? String)?.ifEmpty { null }
                                )
                            )
                        )
                    }

                    ConfigComponent.DPA_SUPPORTED_EMAIL_ADDRESS -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAData = currentMeta.dpaData ?: ClickToPayDPAData()
                        current.copy(
                            meta = currentMeta.copy(
                                dpaData = currentDPAData.copy(
                                    dpaSupportedEmailAddress = (newValue as? String)?.ifEmpty { null }
                                )
                            )
                        )
                    }

                    ConfigComponent.DPA_SUPPORTED_PHONE_NUMBER -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAData = currentMeta.dpaData ?: ClickToPayDPAData()
                        current.copy(
                            meta = currentMeta.copy(
                                dpaData = currentDPAData.copy(
                                    dpaSupportedPhoneNumber = newValue as? PhoneNumber
                                )
                            )
                        )
                    }

                    ConfigComponent.DPA_URI -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAData = currentMeta.dpaData ?: ClickToPayDPAData()
                        current.copy(
                            meta = currentMeta.copy(
                                dpaData = currentDPAData.copy(
                                    dpaUri = (newValue as? String)?.ifEmpty { null }
                                )
                            )
                        )
                    }

                    ConfigComponent.DPA_SUPPORT_URI -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAData = currentMeta.dpaData ?: ClickToPayDPAData()
                        current.copy(
                            meta = currentMeta.copy(
                                dpaData = currentDPAData.copy(
                                    dpaSupportUri = (newValue as? String)?.ifEmpty { null }
                                )
                            )
                        )
                    }

                    ConfigComponent.DPA_APPLICATION_TYPE -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAData = currentMeta.dpaData ?: ClickToPayDPAData()
                        current.copy(
                            meta = currentMeta.copy(
                                dpaData = currentDPAData.copy(
                                    applicationType = newValue as? ApplicationType
                                )
                            )
                        )
                    }

                    ConfigComponent.DPA_TRANSACTION_OPTIONS -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        current.copy(
                            meta = currentMeta.copy(dpaTransactionOptions = newValue as? ClickToPayDPAOptions)
                        )
                    }

                    // Handle DPA Transaction Options sub-components
                    ConfigComponent.DPA_BILLING_PREFERENCE -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAOptions =
                            currentMeta.dpaTransactionOptions ?: ClickToPayDPAOptions()
                        current.copy(
                            meta = currentMeta.copy(
                                dpaTransactionOptions = currentDPAOptions.copy(
                                    dpaBillingPreference = newValue as? ClickToPayDPAShippingBillingPreference
                                )
                            )
                        )
                    }

                    ConfigComponent.ORDER_TYPE -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAOptions =
                            currentMeta.dpaTransactionOptions ?: ClickToPayDPAOptions()
                        current.copy(
                            meta = currentMeta.copy(
                                dpaTransactionOptions = currentDPAOptions.copy(
                                    orderType = newValue as? ClickToPayOrderType
                                )
                            )
                        )
                    }

                    ConfigComponent.PAYMENT_OPTIONS -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAOptions =
                            currentMeta.dpaTransactionOptions ?: ClickToPayDPAOptions()
                        current.copy(
                            meta = currentMeta.copy(
                                dpaTransactionOptions = currentDPAOptions.copy(
                                    paymentOptions = newValue as? List<PaymentOption>
                                )
                            )
                        )
                    }

                    ConfigComponent.THREE_DS_PREFERENCE -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAOptions =
                            currentMeta.dpaTransactionOptions ?: ClickToPayDPAOptions()
                        current.copy(
                            meta = currentMeta.copy(
                                dpaTransactionOptions = currentDPAOptions.copy(
                                    threeDSPreference = (newValue as? String)?.ifEmpty { null }
                                )
                            )
                        )
                    }

                    ConfigComponent.CONFIRM_PAYMENT -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAOptions =
                            currentMeta.dpaTransactionOptions ?: ClickToPayDPAOptions()
                        current.copy(
                            meta = currentMeta.copy(
                                dpaTransactionOptions = currentDPAOptions.copy(
                                    confirmPayment = newValue as? Boolean
                                )
                            )
                        )
                    }

                    ConfigComponent.TRANSACTION_AMOUNT_VALUE -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAOptions =
                            currentMeta.dpaTransactionOptions ?: ClickToPayDPAOptions()
                        // Since transactionAmount is inherited from BaseDPAOptions and can't be modified via .copy(),
                        // we need to recreate the object. However, this is a limitation of the SDK structure.
                        // For now, we'll update other properties and note that transactionAmount updates
                        // require SDK-level changes to support transactionAmount in ClickToPayDPAOptions constructor.
                        current.copy(
                            meta = currentMeta.copy(
                                dpaTransactionOptions = currentDPAOptions.copy()
                            )
                        )
                    }

                    ConfigComponent.TRANSACTION_CURRENCY_CODE -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentDPAOptions =
                            currentMeta.dpaTransactionOptions ?: ClickToPayDPAOptions()
                        // Same limitation as TRANSACTION_AMOUNT_VALUE
                        current.copy(
                            meta = currentMeta.copy(
                                dpaTransactionOptions = currentDPAOptions.copy()
                            )
                        )
                    }

                    ConfigComponent.CUSTOMER -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        current.copy(
                            meta = currentMeta.copy(customer = newValue as? Customer)
                        )
                    }

                    // Handle Customer sub-components
                    ConfigComponent.CUSTOMER_EMAIL -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentCustomer = currentMeta.customer ?: Customer()
                        current.copy(
                            meta = currentMeta.copy(
                                customer = currentCustomer.copy(
                                    email = (newValue as? String)?.ifEmpty { null }
                                )
                            )
                        )
                    }

                    ConfigComponent.CUSTOMER_FIRST_NAME -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentCustomer = currentMeta.customer ?: Customer()
                        current.copy(
                            meta = currentMeta.copy(
                                customer = currentCustomer.copy(
                                    firstName = (newValue as? String)?.ifEmpty { null }
                                )
                            )
                        )
                    }

                    ConfigComponent.CUSTOMER_LAST_NAME -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentCustomer = currentMeta.customer ?: Customer()
                        current.copy(
                            meta = currentMeta.copy(
                                customer = currentCustomer.copy(
                                    lastName = (newValue as? String)?.ifEmpty { null }
                                )
                            )
                        )
                    }

                    ConfigComponent.CUSTOMER_PHONE -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentCustomer = currentMeta.customer ?: Customer()
                        current.copy(
                            meta = currentMeta.copy(
                                customer = currentCustomer.copy(phone = newValue as? Phone)
                            )
                        )
                    }

                    ConfigComponent.CUSTOMER_PHONE_COUNTRY_CODE -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentCustomer = currentMeta.customer ?: Customer()
                        val currentPhone = currentCustomer.phone
                        val newCountryCode = (newValue as? String)?.takeIf { it.isNotBlank() }
                        val currentPhoneNumber = currentPhone?.phone ?: ""

                        val updatedPhone =
                            if (newCountryCode != null && currentPhoneNumber.isNotBlank()) {
                                Phone(countryCode = newCountryCode, phone = currentPhoneNumber)
                            } else if (newCountryCode != null || currentPhoneNumber.isNotBlank()) {
                                Phone(
                                    countryCode = newCountryCode,
                                    phone = currentPhoneNumber.ifEmpty { null }
                                )
                            } else {
                                null
                            }
                        current.copy(
                            meta = currentMeta.copy(
                                customer = currentCustomer.copy(phone = updatedPhone)
                            )
                        )
                    }

                    ConfigComponent.CUSTOMER_PHONE_NUMBER -> {
                        val currentMeta = current.meta ?: ClickToPayMeta()
                        val currentCustomer = currentMeta.customer ?: Customer()
                        val currentPhone = currentCustomer.phone
                        val newPhoneNumber = (newValue as? String)?.takeIf { it.isNotBlank() }
                        val currentCountryCode = currentPhone?.countryCode ?: ""

                        val updatedPhone =
                            if (currentCountryCode.isNotBlank() && newPhoneNumber != null) {
                                Phone(countryCode = currentCountryCode, phone = newPhoneNumber)
                            } else if (currentCountryCode.isNotBlank() || newPhoneNumber != null) {
                                Phone(
                                    countryCode = currentCountryCode.ifEmpty { null },
                                    phone = newPhoneNumber
                                )
                            } else {
                                null
                            }
                        current.copy(
                            meta = currentMeta.copy(
                                customer = currentCustomer.copy(phone = updatedPhone)
                            )
                        )
                    }

                    else -> current
                }
                _clickToPayWidgetConfig.value = updatedConfig
            }

            WidgetType.AFTER_PAY -> {
                val current = _afterpayWidgetConfig.value
                val updatedConfig = when (component) {
                    ConfigComponent.AFTERPAY_LOCALE -> {
                        current.copy(locale = newValue as? Locale)
                    }

                    ConfigComponent.AFTERPAY_CHECKOUT_OPTIONS -> {
                        // Update the entire CheckoutOptions object
                        current.copy(options = newValue as? AfterpaySDKConfig.CheckoutOptions)
                    }

                    ConfigComponent.PICKUP -> {
                        val currentOptions =
                            current.options ?: AfterpaySDKConfig.CheckoutOptions()
                        current.copy(
                            options = currentOptions.copy(pickup = newValue as? Boolean)
                        )
                    }

                    ConfigComponent.BUY_NOW -> {
                        val currentOptions =
                            current.options ?: AfterpaySDKConfig.CheckoutOptions()
                        current.copy(
                            options = currentOptions.copy(buyNow = newValue as? Boolean)
                        )
                    }

                    ConfigComponent.SHIPPING_OPTION_REQUIRED -> {
                        val currentOptions =
                            current.options ?: AfterpaySDKConfig.CheckoutOptions()
                        current.copy(
                            options = currentOptions.copy(shippingOptionRequired = newValue as? Boolean)
                        )
                    }

                    ConfigComponent.ENABLE_SINGLE_SHIPPING_OPTION_UPDATE -> {
                        val currentOptions =
                            current.options ?: AfterpaySDKConfig.CheckoutOptions()
                        current.copy(
                            options = currentOptions.copy(enableSingleShippingOptionUpdate = newValue as? Boolean)
                        )
                    }

                    else -> current
                }
                _afterpayWidgetConfig.value = updatedConfig
            }

            WidgetType.PAY_PAL_VAULT -> {
                val current = _paypalVaultWidgetConfig.value
                val updatedConfig = when (component) {
                    ConfigComponent.ACCESS_TOKEN -> current.copy(accessToken = newValue as String)
                    ConfigComponent.GATEWAY_ID -> current.copy(gatewayId = newValue as String)
                    else -> current
                }
                _paypalVaultWidgetConfig.value = updatedConfig
            }

            WidgetType.ADDRESS_DETAILS -> {
                when (component) {
                    ConfigComponent.BILLING_ADDRESS -> {
                        // Update the entire BillingAddress object
                        _addressConfig.value = newValue as? BillingAddress ?: BillingAddress()
                    }

                    else -> {
                        // Handle sub-components (individual fields)
                        val current = _addressConfig.value ?: BillingAddress()
                        val updatedConfig = when (component) {
                            ConfigComponent.FIRST_NAME -> current.copy(firstName = (newValue as? String)?.ifEmpty { null })
                            ConfigComponent.LAST_NAME -> current.copy(lastName = (newValue as? String)?.ifEmpty { null })
                            ConfigComponent.NAME -> current.copy(name = (newValue as? String)?.ifEmpty { null })
                            ConfigComponent.ADDRESS_LINE1 -> current.copy(addressLine1 = (newValue as? String)?.ifEmpty { null })
                            ConfigComponent.ADDRESS_LINE2 -> current.copy(addressLine2 = (newValue as? String)?.ifEmpty { null })
                            ConfigComponent.CITY -> current.copy(city = (newValue as? String)?.ifEmpty { null })
                            ConfigComponent.STATE -> current.copy(state = (newValue as? String)?.ifEmpty { null })
                            ConfigComponent.POSTAL_CODE -> current.copy(postalCode = (newValue as? String)?.ifEmpty { null })
                            ConfigComponent.COUNTRY -> current.copy(country = (newValue as? String)?.ifEmpty { null })
                            ConfigComponent.PHONE_NUMBER -> current.copy(phoneNumber = (newValue as? String)?.ifEmpty { null })
                            else -> current
                        }
                        _addressConfig.value = updatedConfig
                    }
                }
            }

            WidgetType.ZIP -> {
                val current = _zipWidgetConfig.value
                val updatedConfig = when (component) {
                    ConfigComponent.ACCESS_TOKEN -> current.copy(accessToken = newValue as String)
                    ConfigComponent.GATEWAY_ID -> current.copy(gatewayId = newValue as String)

                    // Direct shopper fields (no nested Shopper object)
                    ConfigComponent.ZIP_FIRST_NAME -> current.copy(firstName = newValue as String)
                    ConfigComponent.ZIP_LAST_NAME -> current.copy(lastName = newValue as String)
                    ConfigComponent.ZIP_EMAIL -> current.copy(email = newValue as String)
                    ConfigComponent.ZIP_PHONE -> current.copy(phone = (newValue as? String)?.ifEmpty { null })
                    ConfigComponent.ZIP_TOKENIZE -> current.copy(tokenize = newValue as Boolean)
                    ConfigComponent.ZIP_GENDER -> current.copy(gender = (newValue as? String)?.ifEmpty { null })
                    ConfigComponent.ZIP_DATE_OF_BIRTH -> current.copy(dateOfBirth = (newValue as? String)?.ifEmpty { null })
                    ConfigComponent.ZIP_SHIPPING_TYPE -> current.copy(shippingType = (newValue as? String)?.ifEmpty { null })
                    ConfigComponent.ZIP_USE_DEFAULT_SHIPPING_ADDRESS -> current.copy(
                        shipping = if (newValue == true) defaultZipShippingAddress() else null
                    )

                    // Billing address
                    ConfigComponent.ZIP_BILLING_ADDRESS -> current.copy(billing = newValue as? ZipWidgetConfig.Address)

                    // Individual billing address fields
                    ConfigComponent.ZIP_BILLING_FIRST_NAME -> {
                        val currentBilling = current.billing ?: ZipWidgetConfig.Address()
                        current.copy(billing = currentBilling.copy(firstName = (newValue as? String)?.ifEmpty { null }))
                    }

                    ConfigComponent.ZIP_BILLING_LAST_NAME -> {
                        val currentBilling = current.billing ?: ZipWidgetConfig.Address()
                        current.copy(billing = currentBilling.copy(lastName = (newValue as? String)?.ifEmpty { null }))
                    }

                    ConfigComponent.ZIP_BILLING_LINE1 -> {
                        val currentBilling = current.billing ?: ZipWidgetConfig.Address()
                        current.copy(billing = currentBilling.copy(line1 = (newValue as? String)?.ifEmpty { null }))
                    }

                    ConfigComponent.ZIP_BILLING_LINE2 -> {
                        val currentBilling = current.billing ?: ZipWidgetConfig.Address()
                        current.copy(billing = currentBilling.copy(line2 = (newValue as? String)?.ifEmpty { null }))
                    }

                    ConfigComponent.ZIP_BILLING_CITY -> {
                        val currentBilling = current.billing ?: ZipWidgetConfig.Address()
                        current.copy(billing = currentBilling.copy(city = (newValue as? String)?.ifEmpty { null }))
                    }

                    ConfigComponent.ZIP_BILLING_STATE -> {
                        val currentBilling = current.billing ?: ZipWidgetConfig.Address()
                        current.copy(billing = currentBilling.copy(state = (newValue as? String)?.ifEmpty { null }))
                    }

                    ConfigComponent.ZIP_BILLING_POSTCODE -> {
                        val currentBilling = current.billing ?: ZipWidgetConfig.Address()
                        current.copy(billing = currentBilling.copy(postcode = (newValue as? String)?.ifEmpty { null }))
                    }

                    ConfigComponent.ZIP_BILLING_COUNTRY -> {
                        val currentBilling = current.billing ?: ZipWidgetConfig.Address()
                        current.copy(billing = currentBilling.copy(country = (newValue as? String)?.ifEmpty { null }))
                    }

                    // Shipping address
                    ConfigComponent.ZIP_SHIPPING_ADDRESS -> current.copy(shipping = newValue as? ZipWidgetConfig.Address)

                    // Individual shipping address fields
                    ConfigComponent.ZIP_SHIPPING_FIRST_NAME -> {
                        val currentShipping = current.shipping ?: ZipWidgetConfig.Address()
                        current.copy(shipping = currentShipping.copy(firstName = (newValue as? String)?.ifEmpty { null }))
                    }

                    ConfigComponent.ZIP_SHIPPING_LAST_NAME -> {
                        val currentShipping = current.shipping ?: ZipWidgetConfig.Address()
                        current.copy(shipping = currentShipping.copy(lastName = (newValue as? String)?.ifEmpty { null }))
                    }

                    ConfigComponent.ZIP_SHIPPING_LINE1 -> {
                        val currentShipping = current.shipping ?: ZipWidgetConfig.Address()
                        current.copy(shipping = currentShipping.copy(line1 = (newValue as? String)?.ifEmpty { null }))
                    }

                    ConfigComponent.ZIP_SHIPPING_LINE2 -> {
                        val currentShipping = current.shipping ?: ZipWidgetConfig.Address()
                        current.copy(shipping = currentShipping.copy(line2 = (newValue as? String)?.ifEmpty { null }))
                    }

                    ConfigComponent.ZIP_SHIPPING_CITY -> {
                        val currentShipping = current.shipping ?: ZipWidgetConfig.Address()
                        current.copy(shipping = currentShipping.copy(city = (newValue as? String)?.ifEmpty { null }))
                    }

                    ConfigComponent.ZIP_SHIPPING_STATE -> {
                        val currentShipping = current.shipping ?: ZipWidgetConfig.Address()
                        current.copy(shipping = currentShipping.copy(state = (newValue as? String)?.ifEmpty { null }))
                    }

                    ConfigComponent.ZIP_SHIPPING_POSTCODE -> {
                        val currentShipping = current.shipping ?: ZipWidgetConfig.Address()
                        current.copy(shipping = currentShipping.copy(postcode = (newValue as? String)?.ifEmpty { null }))
                    }

                    ConfigComponent.ZIP_SHIPPING_COUNTRY -> {
                        val currentShipping = current.shipping ?: ZipWidgetConfig.Address()
                        current.copy(shipping = currentShipping.copy(country = (newValue as? String)?.ifEmpty { null }))
                    }

                    else -> current
                }
                _zipWidgetConfig.value = updatedConfig
            }

            else -> {
                // No config for other widget types currently
            }
        }
    }
}

