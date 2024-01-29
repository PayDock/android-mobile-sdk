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

class CardPinValidatorTest {

    @Test
    fun testCheckPin_ValidNumber() {
        assertTrue(CardPinValidator.checkPin("1234"))
    }

    @Test
    fun testCheckPin_BlankNumber() {
        assertFalse(CardPinValidator.checkPin(""))
        assertFalse(CardPinValidator.checkPin("  "))
    }

    @Test
    fun testCheckPin_NonDigitNumber() {
        assertFalse(CardPinValidator.checkPin("abcd"))
    }
}
