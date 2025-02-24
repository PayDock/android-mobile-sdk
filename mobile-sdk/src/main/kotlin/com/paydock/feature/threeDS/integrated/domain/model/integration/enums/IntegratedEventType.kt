/*
 * Created by Paydock on 11/16/23, 11:59 AM
 * Copyright (c) 2023 Lasting. All rights reserved.
 *
 * Last modified 11/16/23, 11:59 AM
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

package com.paydock.feature.threeDS.integrated.domain.model.integration.enums

/**
 * Enum class representing various event types related to Integrated 3D Secure (3DS) charge processing.
 * This enum includes events that occur in Integrated 3DS authentication flows.
 */
enum class IntegratedEventType {
    /**
     * Indicates that the 3DS charge authorization was successfully completed.
     */
    CHARGE_AUTH_SUCCESS,

    /**
     * Indicates that the 3DS charge authorization was rejected.
     */
    CHARGE_AUTH_REJECT,

    /**
     * Represents a general charge authorization event in the 3DS flow.
     */
    CHARGE_AUTH,

    /**
     * Represents a charge cancellation event in the 3DS flow.
     */
    CHARGE_AUTH_CANCELLATION,

    /**
     * Indicates that additional data collection was successfully completed.
     */
    ADDITIONAL_DATA_COLLECT_SUCCESS,

    /**
     * Indicates that additional data collection was rejected.
     */
    ADDITIONAL_DATA_COLLECT_REJECT
}
