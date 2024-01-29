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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CardSecurityCodeValidatorTest {

    @Test
    fun testIsSecurityCodeValid_ValidCVV() {
        assertTrue(CardSecurityCodeValidator.isSecurityCodeValid("123", SecurityCodeType.CVV))
    }

    @Test
    fun testIsSecurityCodeValid_ValidCVC() {
        assertTrue(CardSecurityCodeValidator.isSecurityCodeValid("123", SecurityCodeType.CVC))
    }

    @Test
    fun testIsSecurityCodeValid_ValidCSC() {
        assertTrue(CardSecurityCodeValidator.isSecurityCodeValid("1234", SecurityCodeType.CSC))
    }

    @Test
    fun testIsSecurityCodeValid_BlankCode() {
        assertFalse(CardSecurityCodeValidator.isSecurityCodeValid("", SecurityCodeType.CVV))
    }

    @Test
    fun testIsSecurityCodeValid_NonDigitCode() {
        assertFalse(CardSecurityCodeValidator.isSecurityCodeValid("12a", SecurityCodeType.CSC))
    }

    @Test
    fun testIsSecurityCodeValid_ExceedsMaxLength() {
        assertFalse(CardSecurityCodeValidator.isSecurityCodeValid("12345", SecurityCodeType.CVV))
    }

    @Test
    fun testCheckSecurityCode_ValidCVV() {
        assertTrue(CardSecurityCodeValidator.checkSecurityCode("123", SecurityCodeType.CVV))
    }

    @Test
    fun testCheckSecurityCode_ValidCVC() {
        assertTrue(CardSecurityCodeValidator.checkSecurityCode("124", SecurityCodeType.CVC))
    }

    @Test
    fun testCheckSecurityCode_ValidCSC() {
        assertTrue(CardSecurityCodeValidator.checkSecurityCode("1234", SecurityCodeType.CSC))
    }

    @Test
    fun testCheckSecurityCode_BlankCode() {
        assertFalse(CardSecurityCodeValidator.checkSecurityCode("", SecurityCodeType.CVV))
    }

    @Test
    fun testCheckSecurityCode_NonDigitCode() {
        assertFalse(CardSecurityCodeValidator.checkSecurityCode("12a", SecurityCodeType.CSC))
    }

    @Test
    fun testCheckSecurityCode_ExceedsMaxLength() {
        assertFalse(CardSecurityCodeValidator.checkSecurityCode("12345", SecurityCodeType.CVV))
    }

    @Test
    fun testDetectSecurityCodeType_Visa() {
        assertEquals(
            SecurityCodeType.CVV,
            CardSecurityCodeValidator.detectSecurityCodeType(CardIssuerType.VISA)
        )
    }

    @Test
    fun testDetectSecurityCodeType_MasterCard() {
        assertEquals(
            SecurityCodeType.CVC,
            CardSecurityCodeValidator.detectSecurityCodeType(CardIssuerType.MASTERCARD)
        )
    }

    @Test
    fun testDetectSecurityCodeType_AmericanExpress() {
        assertEquals(
            SecurityCodeType.CSC,
            CardSecurityCodeValidator.detectSecurityCodeType(CardIssuerType.AMERICAN_EXPRESS)
        )
    }
}
