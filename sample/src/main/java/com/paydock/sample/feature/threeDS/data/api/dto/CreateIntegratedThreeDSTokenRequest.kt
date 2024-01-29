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
import com.paydock.sample.feature.wallet.data.api.dto.Customer

data class CreateIntegratedThreeDSTokenRequest(
    val token: String? = null,
    val amount: String = "10",
    val currency: String = "AUD",
    val customer: Customer? = null,
    @SerializedName("_3ds") val threeDSDetails: ThreeDSDetails = ThreeDSDetails()
    // We do not need any other request properties (ie. customer, shipping)
) {
    data class ThreeDSDetails(
        @SerializedName("browser_details") val browserDetails: BrowserDetails = BrowserDetails()
    )

    data class BrowserDetails(
        val name: String = "chrome",
        @SerializedName("java_enabled") val javaEnabled: String = "true",
        val language: String = "en-US",
        @SerializedName("screen_height") val screenHeight: String = "640",
        @SerializedName("screen_width") val screenWidth: String = "480",
        @SerializedName("time_zone") val timeZone: String = "273",
        @SerializedName("color_depth") val colorDepth: String = "24",
    )
}