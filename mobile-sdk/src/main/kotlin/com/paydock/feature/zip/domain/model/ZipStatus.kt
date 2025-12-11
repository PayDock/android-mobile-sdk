package com.paydock.feature.zip.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the status of a Zip payment transaction.
 *
 * These statuses are returned in the 'result' query parameter from Zip callbacks.
 */
@Serializable
enum class ZipStatus(val value: String) {
    /**
     * Payment was approved successfully.
     */
    @SerialName("approved")
    APPROVED("approved"),

    /**
     * Payment was declined by Zip.
     */
    @SerialName("declined")
    DECLINED("declined"),

    /**
     * Payment was cancelled by the user.
     */
    @SerialName("cancelled")
    CANCELLED("cancelled"),

    /**
     * Payment requires manual review (referred).
     */
    @SerialName("referred")
    REFERRED("referred"),

    /**
     * Unexpected status received.
     */
    @SerialName("unexpected")
    UNEXPECTED("unexpected"),

    /**
     * An unexpected error occurred.
     */
    @SerialName("unexpected_error")
    UNEXPECTED_ERROR("unexpected_error");

    /**
     * Whether this status indicates a successful payment.
     */
    val isSuccess: Boolean
        get() = this == APPROVED

    /**
     * Whether this status indicates an error that should be reported.
     */
    val isError: Boolean
        get() = this in listOf(DECLINED, REFERRED, UNEXPECTED_ERROR, UNEXPECTED)

    /**
     * Whether this status indicates user cancellation.
     */
    val isCancellation: Boolean
        get() = this == CANCELLED

    companion object {
        /**
         * Parses a string value to the corresponding [ZipStatus].
         *
         * @param value The string value to parse.
         * @return The matching [ZipStatus] or null if not found.
         */
        fun fromValue(value: String?): ZipStatus? {
            return entries.find { it.value.equals(value, ignoreCase = true) }
        }
    }
}
