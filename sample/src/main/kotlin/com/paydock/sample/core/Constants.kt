package com.paydock.sample.core

const val SMALL_SCREEN_SIZE = 360

const val AMOUNT = "5.0" // This allows for the exact amount to be used!
const val AU_CURRENCY_CODE = "AUD"
const val AU_COUNTRY_CODE = "AU"
const val US_CURRENCY_CODE = "USD"
const val US_COUNTRY_CODE = "US"
val COUNTRY_CODE_LIST = listOf(US_COUNTRY_CODE, AU_COUNTRY_CODE)
const val MERCHANT_NAME = "John Doe Supplies"
const val FIRST_NAME = "John"
const val LAST_NAME = "Doe"
const val EMAIL = "john.doe@test.com"
const val PHONE_NUMBER = "+1234567890"
const val WALLET_INITIALISE_ERROR = "Error trying to initialise wallet transaction"
const val CHARGE_TRANSACTION_ERROR = "Error trying to capture charge transaction"
const val WALLET_CHARGE_TRANSACTION_ERROR = "Error trying to capture wallet charge transaction"
const val COLES_PAY_CHARGE_TRANSACTION_ERROR =
    "Error trying to capture Coles Pay charge transaction"
const val THREE_DS_CHARGE_TRANSACTION_ERROR = "Error trying to capture 3DS charge transaction"
const val TOKENISE_CARD_ERROR = "Error tokenising card details token!"
const val TOKENISE_CLICK_TO_PAY_ERROR = "Error tokenising click to pay details token!"
const val THREE_DS_CARD_ERROR = "Error creating 3DS token!"
const val THREE_DS_NOT_SUPPORTED_ERROR = "Card details not supported by 3DS"
const val THREE_DS_STATUS_ERROR = "Invalid 3DS Status!"