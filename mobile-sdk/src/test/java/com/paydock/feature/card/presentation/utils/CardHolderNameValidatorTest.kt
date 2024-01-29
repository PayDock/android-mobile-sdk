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

class CardHolderNameValidatorTest {

    @Test
    fun testCheckHolderName_ValidName() {
        assertTrue(CardHolderNameValidator.checkHolderName("John Doe"))
        assertTrue(CardHolderNameValidator.checkHolderName("Jane Smith"))
        assertTrue(CardHolderNameValidator.checkHolderName("Alex Johnson"))

        // Card Holder Name Validation document
        assertTrue(CardHolderNameValidator.checkHolderName("Mr Test Name"))
        assertTrue(CardHolderNameValidator.checkHolderName("Mr. Test Name Name-Name"))
        assertTrue(CardHolderNameValidator.checkHolderName("John O'Neil"))
        assertTrue(CardHolderNameValidator.checkHolderName("^aA!@#\$&()-`.+,'_<>;:*=?[ ]/"))
        assertTrue(CardHolderNameValidator.checkHolderName("Test"))
        assertTrue(CardHolderNameValidator.checkHolderName("Test The 2nd"))
        assertTrue(CardHolderNameValidator.checkHolderName("Test III"))
    }

    @Test
    fun testCheckHolderName_BlankName() {
        assertFalse(CardHolderNameValidator.checkHolderName(""))
        assertFalse(CardHolderNameValidator.checkHolderName("  "))
    }
}