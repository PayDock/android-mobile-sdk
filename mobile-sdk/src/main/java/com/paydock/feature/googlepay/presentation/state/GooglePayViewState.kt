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

package com.paydock.feature.googlepay.presentation.state

import com.google.android.gms.wallet.PaymentData
import com.paydock.core.domain.error.ErrorModel
import com.paydock.feature.charge.domain.model.ChargeResponse

/**
 * Represents the state of the Google Pay feature in the application.
 *
 * @param googlePayAvailable Indicates whether Google Pay is available and can be used.
 * @param paymentData The payment data received from a successful Google Pay transaction.
 * @param isLoading Indicates whether a loading state is active (determines whether the Google Pay button is clickable).
 * @param error An [ErrorModel] that holds information about any errors encountered.
 * @property token The token associated with the wallet.
 */
internal data class GooglePayViewState(
    val googlePayAvailable: Boolean = false,
    val paymentData: PaymentData? = null,
    val chargeData: ChargeResponse? = null,
    val isLoading: Boolean = false,
    val error: ErrorModel? = null,
    val token: String? = null,
)