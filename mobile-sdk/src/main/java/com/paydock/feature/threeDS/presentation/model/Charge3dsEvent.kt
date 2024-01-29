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

package com.paydock.feature.threeDS.presentation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an event related to 3D Secure (3DS) charge processing.
 *
 * This class is used to model events that may occur during the 3DS authentication process.
 * It includes information such as the event type and the associated 3DS charge ID.
 *
 * @param event The type of 3DS event, e.g., "chargeAuthSuccess", "chargeAuthReject", etc.
 * @param charge3dsId The unique identifier for the 3DS charge associated with the event.
 */
@Serializable
data class Charge3dsEvent(
    @SerialName("event") val event: String,
    @SerialName("charge3dsId") val charge3dsId: String
)
