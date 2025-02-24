package com.paydock.feature.threeDS.standalone.domain.model.ui.enums

import com.paydock.feature.threeDS.standalone.domain.utils.StandaloneStatusSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the status of an operation or entity.
 *
 * This enum provides a set of predefined states that an operation or entity can be in.
 * It is designed to be used in various parts of the application to represent the
 * outcome or current state of a process.
 *
 * The [SerialName] annotation is used to define the string representation of each status
 * when serializing/deserializing vusing kotlinx.serialization.
 */
@Serializable(with = StandaloneStatusSerializer::class)
internal enum class StandaloneStatus {
    @SerialName("pending")
    PENDING,

    @SerialName("success")
    SUCCESS,

    @SerialName("error")
    ERROR;

    /**
     * Companion object providing utility methods for [StandaloneStatus].
     */
    companion object {
        /**
         * Retrieves a [StandaloneStatus] based on its string representation.
         *
         * @param value The string representation of the schema.
         * @return The corresponding [StandaloneStatus] if found, or `UNKNOWN` if no match is found.
         */
        fun fromValue(value: String): StandaloneStatus? {
            return StandaloneStatus.entries.find {
                it.name.equals(
                    value,
                    ignoreCase = true
                )
            }
        }
    }
}