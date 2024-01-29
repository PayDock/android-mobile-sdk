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

package com.paydock.feature.card.presentation.model

import com.paydock.core.CSC_LENGTH
import com.paydock.core.CVV_CVC_LENGTH

/**
 * Enum class representing different types of card security codes along with the
 * number of required digits for each code type.
 *
 * @property requiredDigits The number of digits required for the security code type.
 */
enum class SecurityCodeType(val requiredDigits: Int) {
    /**
     * Card Verification Value (CVV) security code type.
     * Required digits: 3
     */
    CVV(CVV_CVC_LENGTH),

    /**
     * Card Verification Code (CVC) security code type.
     * Required digits: 3
     */
    CVC(CVV_CVC_LENGTH),

    /**
     * Card Security Code (CSC) security code type.
     * Required digits: 4
     */
    CSC(CSC_LENGTH)
}
