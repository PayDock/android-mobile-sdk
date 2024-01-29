/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.core

// Network Constants
internal const val CONNECTION_TIMEOUT = 45L
internal const val READ_TIMEOUT = 60L
internal const val WRITE_TIMEOUT = 60L

// UI Constants
internal const val SMALL_SCREEN_SIZE = 360
internal const val DEBOUNCE_DELAY = 500L

// Animation Constants
internal const val DEFAULT_ANIMATION_DURATION = 300

// Card Details Constants
internal const val CARD_NUMBER_SECTION_SIZE = 4
internal const val MAX_CREDIT_CARD_LENGTH = 19
internal const val MIN_GIFT_CARD_LENGTH = 14
internal const val MAX_GIFT_CARD_LENGTH = 25
internal const val MAX_EXPIRY_LENGTH = 4
internal const val EXPIRY_CHUNK_SIZE = 2
internal const val CVV_CVC_LENGTH = 3
internal const val CSC_LENGTH = 4
internal const val EXPIRY_BASE_YEAR = 2000
internal const val MAX_MONTH_COUNT = 12

// Address Details Constants
internal const val MAX_SEARCH_RESULTS = 5
internal const val EXPANSION_TRANSITION_DURATION = 400

// Google Pay
internal const val GATEWAY = "paydock"

@Suppress("TopLevelPropertyNaming")
internal val ALLOWED_CARD_AUTH_METHODS = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")

@Suppress("TopLevelPropertyNaming")
internal val ALLOWED_CARD_NETWORKS = listOf("AMEX", "DISCOVER", "JCB", "MASTERCARD", "VISA")

internal const val TOKENIZATION_TYPE = "PAYMENT_GATEWAY"
internal const val CARD_PAYMENT_TYPE = "CARD"
internal const val TRANSACTION_PRICE_STATUS = "FINAL"

/**
 * MIN: Name, country code, and postal code (default).
 * FULL: Name, street address, locality, region, country code, and postal code.
 */
internal const val BILLING_ADDRESS_FORMAT = "FULL"

// Wallet Callback Types
internal const val TYPE_CREATE_TRANSACTION = "CREATE_TRANSACTION"
internal const val TYPE_CREATE_SESSION = "CREATE_SESSION"

// PayPal
internal const val PAY_PAL_REDIRECT_URL = "https://paydock-mobile.sdk/paypal/success"
internal const val FLY_PAY_REDIRECT_URL = "https://paydock.sdk/"
internal const val REDIRECT_PARAM_NAME = "redirect_uri"
internal const val PAY_PAL_REDIRECT_PARAM_VALUE = "https://paydock-mobile.sdk/paypal/success&native_xo=1"