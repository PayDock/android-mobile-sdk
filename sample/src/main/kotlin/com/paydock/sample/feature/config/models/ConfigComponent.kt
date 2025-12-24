package com.paydock.sample.feature.config.models

enum class ConfigComponent(val hasSubComponents: Boolean = false) {
    // Common properties
    ACCESS_TOKEN,
    GATEWAY_ID,

    // Card Details specific
    COLLECT_CARDHOLDER_NAME,
    ALLOW_SAVE_CARD(hasSubComponents = true),
    SCHEME_SUPPORT(hasSubComponents = true),

    // Save Card Config properties
    SAVE_CARD_CONSENT_TEXT,
    PRIVACY_POLICY(hasSubComponents = true),

    // Privacy Policy Config properties
    PRIVACY_POLICY_TEXT,
    PRIVACY_POLICY_URL,

    // Supported Scheme Config properties
    SUPPORTED_SCHEMES,
    ENABLE_VALIDATION,

    // Gift Card specific
    STORE_PIN,

    // PayPal specific
    REQUEST_SHIPPING,
    FUNDING_SOURCE,

    // Coles Pay specific
    CLIENT_ID,

    // Click to Pay specific
    SERVICE_ID,
    CLICK_TO_PAY_META(hasSubComponents = true),

    // Click to Pay Meta sub-components
    DISABLE_SUMMARY_SCREEN,
    CHECKOUT_EXPERIENCE,
    SERVICES,
    UNACCEPTED_CARD_TYPE,
    CARD_BRANDS,
    CO_BRAND_NAMES,
    DPA_DATA(hasSubComponents = true),

    // DPA Data sub-components
    DPA_ADDRESS,
    DPA_EMAIL_ADDRESS,
    DPA_PHONE_NUMBER(hasSubComponents = true),
    DPA_LOGO_URI,
    DPA_SUPPORTED_EMAIL_ADDRESS,
    DPA_SUPPORTED_PHONE_NUMBER(hasSubComponents = true),
    DPA_URI,
    DPA_SUPPORT_URI,
    DPA_APPLICATION_TYPE,

    // DPA Phone Number sub-components
    DPA_PHONE_COUNTRY_CODE,
    DPA_PHONE_NUMBER_FIELD,

    DPA_TRANSACTION_OPTIONS(hasSubComponents = true),

    // DPA Transaction Options sub-components (from ClickToPayDPAOptions)
    DPA_BILLING_PREFERENCE,
    PAYMENT_OPTIONS,
    ORDER_TYPE,
    THREE_DS_PREFERENCE,
    CONFIRM_PAYMENT,

    // Payment Option sub-components
    PAYMENT_OPTION_DYNAMIC_DATA_TYPE,

    // Transaction Amount sub-components
    TRANSACTION_AMOUNT(hasSubComponents = true),
    TRANSACTION_AMOUNT_VALUE,
    TRANSACTION_CURRENCY_CODE,

    CUSTOMER(hasSubComponents = true),

    // Customer sub-components
    CUSTOMER_EMAIL,
    CUSTOMER_FIRST_NAME,
    CUSTOMER_LAST_NAME,
    CUSTOMER_PHONE(hasSubComponents = true),

    // Customer Phone sub-components
    CUSTOMER_PHONE_COUNTRY_CODE,
    CUSTOMER_PHONE_NUMBER,

    // Afterpay specific
    AFTERPAY_CONFIGURATION,
    AFTERPAY_CHECKOUT_OPTIONS,

    // Afterpay Configuration sub-components
    MINIMUM_AMOUNT,
    MAXIMUM_AMOUNT,
    CURRENCY,
    LANGUAGE,

    // Shared location fields
    COUNTRY,

    // Afterpay Checkout Options sub-components
    PICKUP,
    BUY_NOW,
    SHIPPING_OPTION_REQUIRED,
    ENABLE_SINGLE_SHIPPING_OPTION_UPDATE,
    CITY,
    STATE,
    POSTAL_CODE,

    // PayPal Vault specific
    BUTTON_ICON,

    // Address Details specific
    BILLING_ADDRESS,

    // Billing Address sub-components
    FIRST_NAME,
    LAST_NAME,
    NAME,
    ADDRESS_LINE1,
    ADDRESS_LINE2,
    PHONE_NUMBER,

    // Zip specific
    AMOUNT,
    ZIP_SHOPPER(hasSubComponents = true),
    ZIP_BILLING_ADDRESS(hasSubComponents = true),

    // Zip Shopper sub-components
    SHOPPER_FIRST_NAME,
    SHOPPER_LAST_NAME,
    SHOPPER_EMAIL,
    SHOPPER_PHONE,

    // Zip Billing Address sub-components
    ZIP_ADDRESS_FIRST_NAME,
    ZIP_ADDRESS_LAST_NAME,
    ZIP_ADDRESS_LINE1,
    ZIP_ADDRESS_LINE2,
    ZIP_ADDRESS_CITY,
    ZIP_ADDRESS_STATE,
    ZIP_ADDRESS_POSTAL_CODE,
    ZIP_ADDRESS_COUNTRY_CODE;

    fun displayName(): String = when (this) {
        ACCESS_TOKEN -> "Access Token"
        GATEWAY_ID -> "Gateway ID"
        COLLECT_CARDHOLDER_NAME -> "Collect Cardholder Name"
        ALLOW_SAVE_CARD -> "Allow Save Card"
        SCHEME_SUPPORT -> "Card Scheme Support"
        SAVE_CARD_CONSENT_TEXT -> "Consent Text"
        PRIVACY_POLICY -> "Privacy Policy"
        PRIVACY_POLICY_TEXT -> "Privacy Policy Text"
        PRIVACY_POLICY_URL -> "Privacy Policy URL"
        SUPPORTED_SCHEMES -> "Supported Card Schemes"
        ENABLE_VALIDATION -> "Enable Scheme Validation"
        STORE_PIN -> "Store PIN"
        REQUEST_SHIPPING -> "Request Shipping"
        FUNDING_SOURCE -> "Funding Source"
        CLIENT_ID -> "Client ID"
        SERVICE_ID -> "Service ID"
        CLICK_TO_PAY_META -> "Meta"
        DISABLE_SUMMARY_SCREEN -> "Disable Summary Screen"
        CHECKOUT_EXPERIENCE -> "Checkout Experience"
        SERVICES -> "Services"
        UNACCEPTED_CARD_TYPE -> "Unaccepted Card Type"
        CARD_BRANDS -> "Card Brands"
        CO_BRAND_NAMES -> "Co-Brand Names"
        DPA_DATA -> "DPA Data"
        DPA_ADDRESS -> "DPA Address"
        DPA_EMAIL_ADDRESS -> "DPA Email Address"
        DPA_PHONE_NUMBER -> "DPA Phone Number"
        DPA_LOGO_URI -> "DPA Logo URI"
        DPA_SUPPORTED_EMAIL_ADDRESS -> "DPA Supported Email Address"
        DPA_SUPPORTED_PHONE_NUMBER -> "DPA Supported Phone Number"
        DPA_URI -> "DPA URI"
        DPA_SUPPORT_URI -> "DPA Support URI"
        DPA_APPLICATION_TYPE -> "DPA Application Type"
        DPA_PHONE_COUNTRY_CODE -> "Country Code"
        DPA_PHONE_NUMBER_FIELD -> "Phone Number"
        DPA_TRANSACTION_OPTIONS -> "DPA Transaction Options"
        DPA_BILLING_PREFERENCE -> "DPA Billing Preference"
        PAYMENT_OPTIONS -> "Payment Options"
        ORDER_TYPE -> "Order Type"
        THREE_DS_PREFERENCE -> "3DS Preference"
        CONFIRM_PAYMENT -> "Confirm Payment"
        PAYMENT_OPTION_DYNAMIC_DATA_TYPE -> "Dynamic Data Type"
        TRANSACTION_AMOUNT -> "Transaction Amount"
        TRANSACTION_AMOUNT_VALUE -> "Amount"
        TRANSACTION_CURRENCY_CODE -> "Currency Code"
        CUSTOMER -> "Customer"
        CUSTOMER_EMAIL -> "Email"
        CUSTOMER_FIRST_NAME -> "First Name"
        CUSTOMER_LAST_NAME -> "Last Name"
        CUSTOMER_PHONE -> "Phone"
        CUSTOMER_PHONE_COUNTRY_CODE -> "Country Code"
        CUSTOMER_PHONE_NUMBER -> "Phone Number"
        AFTERPAY_CONFIGURATION -> "Configuration"
        AFTERPAY_CHECKOUT_OPTIONS -> "Checkout Options"
        MINIMUM_AMOUNT -> "Minimum Amount"
        MAXIMUM_AMOUNT -> "Maximum Amount"
        CURRENCY -> "Currency"
        LANGUAGE -> "Language"
        COUNTRY -> "Country"
        PICKUP -> "Pickup"
        BUY_NOW -> "Buy Now"
        SHIPPING_OPTION_REQUIRED -> "Shipping Option Required"
        ENABLE_SINGLE_SHIPPING_OPTION_UPDATE -> "Enable Single Shipping Option Update"
        CITY -> "City"
        STATE -> "State"
        POSTAL_CODE -> "Postal Code"
        BUTTON_ICON -> "Button Icon"
        BILLING_ADDRESS -> "Billing Address"
        FIRST_NAME -> "First Name"
        LAST_NAME -> "Last Name"
        NAME -> "Full Name"
        ADDRESS_LINE1 -> "Address Line 1"
        ADDRESS_LINE2 -> "Address Line 2"
        PHONE_NUMBER -> "Phone Number"
        AMOUNT -> "Amount"
        ZIP_SHOPPER -> "Shopper"
        ZIP_BILLING_ADDRESS -> "Billing Address"
        SHOPPER_FIRST_NAME -> "First Name"
        SHOPPER_LAST_NAME -> "Last Name"
        SHOPPER_EMAIL -> "Email"
        SHOPPER_PHONE -> "Phone"
        ZIP_ADDRESS_FIRST_NAME -> "First Name"
        ZIP_ADDRESS_LAST_NAME -> "Last Name"
        ZIP_ADDRESS_LINE1 -> "Address Line 1"
        ZIP_ADDRESS_LINE2 -> "Address Line 2"
        ZIP_ADDRESS_CITY -> "City"
        ZIP_ADDRESS_STATE -> "State"
        ZIP_ADDRESS_POSTAL_CODE -> "Postal Code"
        ZIP_ADDRESS_COUNTRY_CODE -> "Country Code"
    }

    fun mapConfigComponentToSubComponents(): List<ConfigComponent>? = when (this) {
        ALLOW_SAVE_CARD -> listOf(
            SAVE_CARD_CONSENT_TEXT,
            PRIVACY_POLICY
        )

        PRIVACY_POLICY -> listOf(
            PRIVACY_POLICY_TEXT,
            PRIVACY_POLICY_URL
        )

        SCHEME_SUPPORT -> listOf(
            SUPPORTED_SCHEMES,
            ENABLE_VALIDATION
        )

        BILLING_ADDRESS -> listOf(
            FIRST_NAME,
            LAST_NAME,
            NAME,
            ADDRESS_LINE1,
            ADDRESS_LINE2,
            CITY,
            STATE,
            POSTAL_CODE,
            COUNTRY,
            PHONE_NUMBER
        )

        AFTERPAY_CONFIGURATION -> listOf(
            MINIMUM_AMOUNT,
            MAXIMUM_AMOUNT,
            CURRENCY,
            LANGUAGE,
            COUNTRY
        )

        AFTERPAY_CHECKOUT_OPTIONS -> listOf(
            PICKUP,
            BUY_NOW,
            SHIPPING_OPTION_REQUIRED,
            ENABLE_SINGLE_SHIPPING_OPTION_UPDATE
        )

        CLICK_TO_PAY_META -> listOf(
            DISABLE_SUMMARY_SCREEN,
            CHECKOUT_EXPERIENCE,
            SERVICES,
            UNACCEPTED_CARD_TYPE,
            CARD_BRANDS,
            CO_BRAND_NAMES,
            DPA_DATA,
            DPA_TRANSACTION_OPTIONS,
            CUSTOMER
        )

        DPA_DATA -> listOf(
            DPA_ADDRESS,
            DPA_EMAIL_ADDRESS,
            DPA_PHONE_NUMBER,
            DPA_LOGO_URI,
            DPA_SUPPORTED_EMAIL_ADDRESS,
            DPA_SUPPORTED_PHONE_NUMBER,
            DPA_URI,
            DPA_SUPPORT_URI,
            DPA_APPLICATION_TYPE
        )

        DPA_PHONE_NUMBER -> listOf(
            DPA_PHONE_COUNTRY_CODE,
            DPA_PHONE_NUMBER_FIELD
        )

        DPA_SUPPORTED_PHONE_NUMBER -> listOf(
            DPA_PHONE_COUNTRY_CODE,
            DPA_PHONE_NUMBER_FIELD
        )

        DPA_TRANSACTION_OPTIONS -> listOf(
            DPA_BILLING_PREFERENCE,
            PAYMENT_OPTIONS,
            ORDER_TYPE,
            THREE_DS_PREFERENCE,
            CONFIRM_PAYMENT,
            TRANSACTION_AMOUNT
        )

        TRANSACTION_AMOUNT -> listOf(
            TRANSACTION_AMOUNT_VALUE,
            TRANSACTION_CURRENCY_CODE
        )

        CUSTOMER -> listOf(
            CUSTOMER_EMAIL,
            CUSTOMER_FIRST_NAME,
            CUSTOMER_LAST_NAME,
            CUSTOMER_PHONE
        )

        CUSTOMER_PHONE -> listOf(
            CUSTOMER_PHONE_COUNTRY_CODE,
            CUSTOMER_PHONE_NUMBER
        )

        ZIP_SHOPPER -> listOf(
            SHOPPER_FIRST_NAME,
            SHOPPER_LAST_NAME,
            SHOPPER_EMAIL,
            SHOPPER_PHONE
        )

        ZIP_BILLING_ADDRESS -> listOf(
            ZIP_ADDRESS_FIRST_NAME,
            ZIP_ADDRESS_LAST_NAME,
            ZIP_ADDRESS_LINE1,
            ZIP_ADDRESS_LINE2,
            ZIP_ADDRESS_CITY,
            ZIP_ADDRESS_STATE,
            ZIP_ADDRESS_POSTAL_CODE,
            ZIP_ADDRESS_COUNTRY_CODE
        )

        else -> null
    }
}



