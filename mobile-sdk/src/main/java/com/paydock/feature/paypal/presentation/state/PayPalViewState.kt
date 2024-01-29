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

package com.paydock.feature.paypal.presentation.state

import com.paydock.core.domain.error.ErrorModel
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.wallet.domain.model.WalletCallback

/**
 * Represents the state of the PayPal payment view.
 *
 * @property isLoading Indicates if the view is in a loading state.
 * @property error Represents any error occurred during the PayPal transaction.
 * @property token The token associated with the wallet.
 * @property chargeData The [ChargeResponse] instance holding the wallet data.
 * @property callbackData The [WalletCallback] for the wallet callback details, if available.
 * @property paymentData Contains the PayPal payment data, if available.
 */
internal data class PayPalViewState(
    val isLoading: Boolean = false,
    val error: ErrorModel? = null,
    val token: String? = null,
    val chargeData: ChargeResponse? = null,
    val callbackData: WalletCallback? = null,
    val paymentData: PayPalData? = null
)

/**
 * Represents the data related to a PayPal payment.
 *
 * @property payPalToken The PayPal token related to the payment.
 * @property payerId The PayerID related to the PayPal payment.
 */
internal data class PayPalData(
    var payPalToken: String,
    var payerId: String
)
