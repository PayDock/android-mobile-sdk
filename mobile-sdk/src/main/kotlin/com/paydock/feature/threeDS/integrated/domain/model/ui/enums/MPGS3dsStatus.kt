package com.paydock.feature.threeDS.integrated.domain.model.ui.enums

import com.paydock.feature.threeDS.integrated.domain.utils.MPGS3dsStatusSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the status of an operation or entity.
 *
 * This enum provides a set of predefined states that an operation or entity can be in.
 * It is designed to be used in various parts of the application to represent the
 * outcome or current state of a process.
 *
 * The [SerialName] annotation is used to define the string representation of each
 * status when serializing/deserializing using kotlinx.serialization.
 */
@Serializable(with = MPGS3dsStatusSerializer::class)
internal enum class MPGS3dsStatus {
    @SerialName("rejected")
    REJECTED,

    @SerialName("authenticated")
    AUTHENTICATED,

    @SerialName("not_authenticated")
    NOT_AUTHENTICATED,

    @SerialName("additional_data_complete")
    ADDITIONAL_DATA_COMPLETE,

    @SerialName("additional_data_failed")
    ADDITIONAL_DATA_FAILED,

    @SerialName("authentication_cancelled")
    AUTHENTICATION_CANCELLED;

    /**
     * Companion object providing utility methods for [MPGS3dsStatus].
     */
    companion object Companion {
        /**
         * Retrieves a [MPGS3dsStatus] based on its string representation.
         *
         * @param value The string representation of the schema.
         * @return The corresponding [MPGS3dsStatus] if found, or `UNKNOWN` if no match is found.
         */
        fun fromValue(value: String): MPGS3dsStatus? {
            return entries.find {
                it.name.equals(
                    value,
                    ignoreCase = true
                )
            }
        }
    }
}