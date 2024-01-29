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

import com.paydock.core.MAX_GIFT_CARD_LENGTH
import com.paydock.core.MIN_GIFT_CARD_LENGTH

/**
 * A utility object for validating gift card number details.
 */
internal object GiftCardNumberValidator {

    /**
     * Checks if the provided gift card number is valid.
     *
     * A valid credit card number should meet the following conditions:
     * 1. It should not be blank or empty.
     * 2. It should contain only digits.
     * 3. It should not exceed the maximum allowed card length defined by [MAX_GIFT_CARD_LENGTH].
     *
     * @param number The gift card number to be validated.
     * @return `true` if the number is valid, `false` otherwise.
     */
    fun checkNumber(number: String): Boolean =
        number.isNotBlank() &&
            number.matches(Regex("^[0-9]+$")) &&
            number.length <= MAX_GIFT_CARD_LENGTH

    fun isCardNumberValid(number: String): Boolean = checkNumber(number) && number.length in MIN_GIFT_CARD_LENGTH..MAX_GIFT_CARD_LENGTH
}