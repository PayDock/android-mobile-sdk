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

package com.paydock.feature.threeDS.standalone.domain.model.integration.enums

/**
 * Enum class representing various event types related to Standalone 3D Secure (3DS) charge processing.
 * This enum includes events that occur in Standalone 3DS authentication flows.
 */
enum class StandaloneEventType {
    /**
     * Indicates that the 3DS charge authorization was successfully completed.
     */
    CHARGE_AUTH_SUCCESS,

    /**
     * Indicates that the 3DS charge authorization was rejected.
     */
    CHARGE_AUTH_REJECT,

    // Standalone Flow Events
    /**
     * Indicates that the 3DS charge authorization requires a user challenge step.
     */
    CHARGE_AUTH_CHALLENGE,

    /**
     * Indicates that the 3DS charge authorization was completed using a decoupled flow,
     * meaning authentication occurred asynchronously without direct user interaction.
     */
    CHARGE_AUTH_DECOUPLED,

    /**
     * Represents an informational event related to the 3DS charge process,
     * without necessarily indicating success or failure.
     */
    CHARGE_AUTH_INFO,

    /**
     * Indicates that an error occurred during the 3DS charge authentication process.
     */
    CHARGE_ERROR
}
