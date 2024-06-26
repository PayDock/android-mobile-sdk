package com.paydock.feature.src.presentation.model.enum

import com.paydock.feature.src.presentation.model.enum.EventDataType.CRITICAL_ERROR
import com.paydock.feature.src.presentation.model.enum.EventDataType.SUCCESS
import com.paydock.feature.src.presentation.model.enum.EventDataType.USER_ERROR
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * List of available event data types
 * @enum EventDataType
 *
 * @property CRITICAL_ERROR - CriticalError in this error scenario the checkout is understood to be in a non-recoverable state
 * and should be closed by the merchant and give alternate payment options to the user
 * @property USER_ERROR - UserError in this error scenario the error in likely a user input error and the checkout
 * is in a recoverable state, so the user will be kept within the checkout and can retry the flow
 * @property SUCCESS - Success
 */
@Serializable
enum class EventDataType {
    @SerialName("CriticalError")
    CRITICAL_ERROR,

    @SerialName("UserError")
    USER_ERROR,

    @SerialName("Success")
    SUCCESS
}