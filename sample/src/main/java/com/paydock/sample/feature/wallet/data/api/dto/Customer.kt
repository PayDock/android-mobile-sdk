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

package com.paydock.sample.feature.wallet.data.api.dto

import com.google.gson.annotations.SerializedName

data class Customer(
    val email: String = "john.doe@test.com",
    @SerializedName("first_name") val firstName: String = "John",
    @SerializedName("last_name") val lastName: String = "Doe",
    @SerializedName("credentials_updated_at") val credentialsUpdatedAt: String? = null,
    val suspicious: Boolean? = false,
    @SerializedName("payment_source") val paymentSource: PaymentSource,
    val phone: String = "+1234567890"
)