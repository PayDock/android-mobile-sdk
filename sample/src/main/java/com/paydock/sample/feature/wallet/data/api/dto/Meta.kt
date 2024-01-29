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
import com.paydock.sample.core.MERCHANT_NAME

data class Meta(
    @SerializedName("store_id") val storeId: String = "007",
    @SerializedName("merchant_name") val merchantName: String = MERCHANT_NAME,
    @SerializedName("store_name") val storeName: String = MERCHANT_NAME
)