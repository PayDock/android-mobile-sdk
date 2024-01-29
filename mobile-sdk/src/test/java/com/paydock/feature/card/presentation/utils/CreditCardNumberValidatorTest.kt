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

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreditCardNumberValidatorTest {

    @Test
    fun testCheckNumber_ValidNumber() {
        assertTrue(CreditCardNumberValidator.checkNumber("4111111111111111"))
        assertTrue(CreditCardNumberValidator.checkNumber("1234567890123456"))
        assertTrue(CreditCardNumberValidator.checkNumber("5555555555554444"))
    }

    @Test
    fun testCheckNumber_BlankNumber() {
        assertFalse(CreditCardNumberValidator.checkNumber(""))
        assertFalse(CreditCardNumberValidator.checkNumber("  "))
    }

    @Test
    fun testCheckNumber_NonDigitNumber() {
        assertFalse(CreditCardNumberValidator.checkNumber("4111-1111-1111-1111"))
        assertFalse(CreditCardNumberValidator.checkNumber("4111 abc 1111 1111"))
    }

    @Test
    fun testCheckNumber_ExceedsMaxLength() {
        assertFalse(CreditCardNumberValidator.checkNumber("41111111111111112222"))
    }
}
