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

package com.paydock.sample.feature.widgets.ui.models

data class Widget(
    val type: WidgetType,
    val title: String,
    val description: String
)

enum class WidgetType {
    CREDIT_CARD_DETAILS, GIFT_CARD_DETAILS, ADDRESS_DETAILS, PAY_PAL, FLY_PAY, INTEGRATED_3DS, STANDALONE_3DS, GOOGLE_PAY
}

fun WidgetType.displayName(): String = when (this) {
    WidgetType.CREDIT_CARD_DETAILS -> "Credit Card Details"
    WidgetType.GIFT_CARD_DETAILS -> "Gift Card Details"
    WidgetType.ADDRESS_DETAILS -> "Address"
    WidgetType.GOOGLE_PAY -> "Google Pay"
    WidgetType.PAY_PAL -> "PayPal"
    WidgetType.FLY_PAY -> "FlyPay"
    WidgetType.INTEGRATED_3DS -> "Integrated 3DS"
    WidgetType.STANDALONE_3DS -> "Standalone 3DS"
}
