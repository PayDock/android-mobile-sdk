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

package com.paydock.feature.threeDS.domain.model

/**
 * Enum class representing different types of events related to 3D Secure (3DS) charge processing.
 *
 * Each enum constant corresponds to a specific event type, such as successful authentication,
 * rejection, challenge, decoupling, information, error, or an unknown event type.
 *
 * @param type The string representation of the event type.
 */
enum class EventType(val type: String) {
    /**
     * Represents a successful 3DS charge authorization.
     */
    CHARGE_AUTH_SUCCESS("chargeAuthSuccess"),

    /**
     * Represents a rejected 3DS charge authorization.
     */
    CHARGE_AUTH_REJECT("chargeAuthReject"),

    /**
     * Represents a 3DS charge authorization with a challenge.
     */
    CHARGE_AUTH_CHALLENGE("chargeAuthChallenge"),

    /**
     * Represents a decoupled 3DS charge authorization.
     */
    CHARGE_AUTH_DECOUPLED("chargeAuthDecoupled"),

    /**
     * Represents an informational event related to a 3DS charge.
     */
    CHARGE_AUTH_INFO("chargeAuthInfo"),

    /**
     * Represents an error event related to a 3DS charge.
     */
    CHARGE_ERROR("error"),

    /**
     * Represents an unknown or unrecognized event type.
     */
    UNKNOWN("unknown");

    companion object {
        /**
         * Returns the EventType enum constant corresponding to the specified event type name.
         *
         * This method is case-insensitive and returns [UNKNOWN] if no matching EventType is found.
         *
         * @param input The event type name to look up.
         * @return The EventType enum constant or [UNKNOWN] if not found.
         */
        fun byNameIgnoreCaseOrNull(input: String): EventType {
            return entries.firstOrNull { it.type.equals(input, true) } ?: UNKNOWN
        }
    }
}
