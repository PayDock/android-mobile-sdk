/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 4:15 PM
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

package com.paydock.core.domain.error

val ErrorModel?.displayableMessage: String
    get() {
        return when (this) {
            is ErrorModel.ApiError -> exception.displayableMessage
            is ErrorModel.ConnectionError -> "_Troubles with connection"
            is ErrorModel.SerializationError -> "_Troubles with data serialization"
            is ErrorModel.GooglePayError -> exception.message ?: "Unexpected Google Pay exception when trying to deliver the task result!"
            is ErrorModel.PayPalError -> exception.message ?: "Unexpected PayPal exception when trying to deliver the task result!"
            is ErrorModel.FlyPayError -> exception.message ?: "Unexpected FlyPay exception when trying to deliver the task result!"
            else -> "_Unknown error"
        }
    }