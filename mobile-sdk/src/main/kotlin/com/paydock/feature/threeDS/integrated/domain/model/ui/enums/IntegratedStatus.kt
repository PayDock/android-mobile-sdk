package com.paydock.feature.threeDS.integrated.domain.model.ui.enums

import com.paydock.feature.threeDS.integrated.domain.utils.IntegratedStatusSerializer
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
@Serializable(with = IntegratedStatusSerializer::class)
internal enum class IntegratedStatus {
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
     * Companion object providing utility methods for [IntegratedStatus].
     */
    companion object {
        /**
         * Retrieves a [IntegratedStatus] based on its string representation.
         *
         * @param value The string representation of the schema.
         * @return The corresponding [IntegratedStatus] if found, or `UNKNOWN` if no match is found.
         */
        fun fromValue(value: String): IntegratedStatus? {
            return IntegratedStatus.entries.find {
                it.name.equals(
                    value,
                    ignoreCase = true
                )
            }
        }
    }
}