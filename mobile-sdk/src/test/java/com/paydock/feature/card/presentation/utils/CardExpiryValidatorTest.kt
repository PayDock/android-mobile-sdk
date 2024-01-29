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

class CardExpiryValidatorTest {

    @Test
    fun testCheckExpiry_ValidInput() {
        assertTrue(CardExpiryValidator.checkExpiry("0123"))
        assertTrue(CardExpiryValidator.checkExpiry("1123"))
        assertTrue(CardExpiryValidator.checkExpiry("0525"))
    }

    @Test
    fun testCheckExpiry_InvalidInput() {
        assertFalse(CardExpiryValidator.checkExpiry(""))
        assertFalse(CardExpiryValidator.checkExpiry("12345"))
        assertFalse(CardExpiryValidator.checkExpiry("abc"))
    }

    @Test
    fun testIsExpiryValid_ValidInput() {
        assertTrue(CardExpiryValidator.isExpiryValid("0125"))
        assertTrue(CardExpiryValidator.isExpiryValid("1125"))
        assertTrue(CardExpiryValidator.isExpiryValid("0525"))
    }

    @Test
    fun testIsExpiryValid_InvalidInput() {
        assertFalse(CardExpiryValidator.isExpiryValid(""))
        assertFalse(CardExpiryValidator.isExpiryValid("01"))
        assertFalse(CardExpiryValidator.isExpiryValid("12/45"))
        assertFalse(CardExpiryValidator.isExpiryValid("00"))
        assertFalse(CardExpiryValidator.isExpiryValid("1325"))
    }

    @Test
    fun testIsCardExpired_NotExpired() {
        assertFalse(CardExpiryValidator.isCardExpired("1225"))
        assertFalse(CardExpiryValidator.isCardExpired("0127"))
        assertFalse(CardExpiryValidator.isCardExpired("0625"))
    }

    @Test
    fun testIsCardExpired_Expired() {
        assertTrue(CardExpiryValidator.isCardExpired("0621"))
        assertTrue(CardExpiryValidator.isCardExpired("0221"))
        assertTrue(CardExpiryValidator.isCardExpired("1220"))
    }
}