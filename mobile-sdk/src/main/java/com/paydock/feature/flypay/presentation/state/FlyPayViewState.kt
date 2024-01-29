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

package com.paydock.feature.flypay.presentation.state

import com.paydock.core.domain.error.ErrorModel
import com.paydock.feature.wallet.domain.model.WalletCallback

/**
 * Represents the state of the FlyPay view in the application.
 *
 * @property isLoading A boolean indicating whether the wallet data is currently being loaded.
 * @property error An [ErrorModel] instance representing any occurred errors in the wallet view.
 * @property token The token associated with the wallet.
 * @property callbackData The details for the wallet callback, if available.
 */
internal data class FlyPayViewState(
    val isLoading: Boolean = false,
    val error: ErrorModel? = null,
    val token: String? = null,
    val callbackData: WalletCallback? = null
)