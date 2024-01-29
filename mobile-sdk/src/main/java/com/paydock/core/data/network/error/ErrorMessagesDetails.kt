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

package com.paydock.core.data.network.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Serializable data class representing details of error messages.
 *
 * @property path The path associated with the error messages (nullable).
 * @property messages List of error details related to the messages (nullable list).
 */
@Serializable
data class ErrorMessagesDetails(
    @SerialName("path") val path: String? = null,
    @SerialName("messages") val messages: List<ErrorDetails>? = null
)