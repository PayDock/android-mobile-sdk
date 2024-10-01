/*
 * Created by Paydock on 11/16/23, 11:58 AM
 * Copyright (c) 2023 Lasting. All rights reserved.
 *
 * Last modified 11/16/23, 11:58 AM
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

package com.paydock.feature.threeDS.domain.model

/**
 * Data class representing the result of 3D Secure (3DS) processing.
 *
 * @param event The type of event that occurred during 3DS processing.
 * @param charge3dsId The Charge ID associated with the 3DS transaction to return to the merchant.
 */
data class ThreeDSResult(
    val event: EventType,
    val charge3dsId: String?
)
