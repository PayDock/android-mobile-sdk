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

package com.paydock.feature.card.presentation.utils

import com.paydock.feature.card.presentation.model.CardIssuerType
import com.paydock.feature.card.presentation.model.SecurityCodeType

/**
 * A utility object for detecting the type of security code (CVV, CSC, CVC) based on the card issuer type.
 */
internal object CardSecurityCodeValidator {

    fun isSecurityCodeValid(code: String, securityCodeType: SecurityCodeType): Boolean =
        checkSecurityCode(code, securityCodeType) && code.length == securityCodeType.requiredDigits

    /**
     * Checks if the security code input is valid based on the specified security code type.
     *
     * @param code The security code input string to validate.
     * @param securityCodeType The type of security code to validate against.
     * @return True if the security code input is valid, false otherwise.
     */
    fun checkSecurityCode(code: String, securityCodeType: SecurityCodeType): Boolean =
        code.isNotBlank() && code.matches(Regex("^[0-9]+$")) && code.length <= securityCodeType.requiredDigits

    /**
     * Detects the type of security code based on the specified card issuer type.
     *
     * @param cardIssuer The type of card issuer.
     * @return The detected [SecurityCodeType] based on the card issuer.
     */
    fun detectSecurityCodeType(cardIssuer: CardIssuerType): SecurityCodeType = when (cardIssuer) {
        CardIssuerType.VISA, CardIssuerType.DISCOVER, CardIssuerType.UNION_PAY ->
            SecurityCodeType.CVV

        CardIssuerType.AMERICAN_EXPRESS, CardIssuerType.DINERS_CLUB ->
            SecurityCodeType.CSC

        CardIssuerType.MASTERCARD -> SecurityCodeType.CVC
        else -> SecurityCodeType.CVV
    }
}