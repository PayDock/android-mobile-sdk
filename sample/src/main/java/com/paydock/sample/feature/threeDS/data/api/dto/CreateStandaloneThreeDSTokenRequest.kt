/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 5:58 PM
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

package com.paydock.sample.feature.threeDS.data.api.dto

import com.google.gson.annotations.SerializedName
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.feature.wallet.data.api.dto.Customer
import com.paydock.sample.feature.wallet.data.api.dto.PaymentSource

data class CreateStandaloneThreeDSTokenRequest(
    val amount: String = "10",
    val currency: String = AU_CURRENCY_CODE,
    val customer: Customer,
    @SerializedName("_3ds") val threeDSDetails: ThreeDSDetails = ThreeDSDetails()
    // We do not need any other request properties (ie. customer, shipping)
) {
    data class ThreeDSDetails(
        @SerializedName("service_id") val serviceId: String = BuildConfig.STANDALONE_3DS_SERVICE_ID,
        val authentication: Authentication = Authentication()
    )

    data class Authentication(
        val type: String = "01",
        val date: String = "2023-11-11T13:00:00.521Z",
        val version: String = "2.2.0",
        val customer: Customer = Customer(
            credentialsUpdatedAt = "2023-05-31T20:06:05.521Z",
            suspicious = false,
            paymentSource = PaymentSource(
                createdAt = "2023-11-11T20:06:05.521Z",
                addAttempts = listOf("2023-11-11T20:06:05.521Z"),
                cardType = "02"
            )
        )
    )
}