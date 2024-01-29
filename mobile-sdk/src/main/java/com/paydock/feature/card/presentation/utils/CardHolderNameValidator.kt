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

/**
 * A utility object for validating and parsing card holder name details.
 */
internal object CardHolderNameValidator {

    /**
     * Checks if the provided cardholder name is valid.
     *
     * A valid cardholder name should meet the following conditions:
     * 1. It should not be blank or empty.
     *
     * @param name The cardholder name to be validated.
     * @return `true` if the name is valid, `false` otherwise.
     */
    fun checkHolderName(name: String): Boolean =
        name.isNotBlank()

}