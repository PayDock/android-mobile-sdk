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

class GiftCardNumberValidatorTest {

    @Test
    fun testCheckNumber_ValidNumber() {
        assertTrue(GiftCardNumberValidator.checkNumber("62734010001104878"))
        assertTrue(GiftCardNumberValidator.checkNumber("12345678901234567891234"))
        assertTrue(GiftCardNumberValidator.checkNumber("55555555555544"))
    }

    @Test
    fun testCheckNumber_BlankNumber() {
        assertFalse(GiftCardNumberValidator.checkNumber(""))
        assertFalse(GiftCardNumberValidator.checkNumber("  "))
    }

    @Test
    fun testCheckNumber_NonDigitNumber() {
        assertFalse(GiftCardNumberValidator.checkNumber("4111-1111-1111-1111"))
        assertFalse(GiftCardNumberValidator.checkNumber("4111 abc 1111 1111"))
    }

    @Test
    fun testCheckNumber_ExceedsMaxLength() {
        assertFalse(GiftCardNumberValidator.checkNumber("41111222233334444555566667777"))
    }

    @Test
    fun testCheckNumberValid_FailMinLength() {
        assertFalse(GiftCardNumberValidator.isCardNumberValid("411112222"))
    }

    @Test
    fun testCheckNumberValid_ExceedsMaxLength() {
        assertFalse(GiftCardNumberValidator.isCardNumberValid("41111222233334444555566667777"))
    }
}
