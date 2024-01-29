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

package com.paydock.feature.wallet.data.api.dto

import com.paydock.core.utils.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * Serializable data class representing wallet information.
 * This class holds details regarding the wallet, including its status, ID, amount, and currency.
 *
 * @property status The status of the wallet.
 * @property id The unique identifier of the wallet.
 * @property amount The amount available in the wallet.
 * @property currency The currency type used in the wallet.
 */
@Serializable
data class WalletData(
    val status: String,
    val id: String,
    @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
    val currency: String
)