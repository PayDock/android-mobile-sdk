package com.paydock.feature.src.presentation.model

import com.paydock.core.domain.error.ErrorModel
import com.paydock.core.domain.error.exceptions.MastercardSRCException
import com.paydock.feature.src.presentation.model.enum.EventDataType
import com.paydock.feature.src.presentation.utils.MastercardSRCErrorDataSerializer
import kotlinx.serialization.Serializable

/**
 * Represents various error data.
 */
@Serializable(with = MastercardSRCErrorDataSerializer::class)
internal sealed class ErrorData {
    /**
     * Represents user-specific error data.
     *
     * @property type The type of event data associated with the error.
     * @property data The list of error details for user errors.
     */
    @Serializable
    internal data class UserErrorData(
        val type: EventDataType = EventDataType.USER_ERROR,
        val data: String
    ) : ErrorData()

    /**
     * Represents critical error data.
     *
     * @property type The type of event data associated with the error.
     * @property data The critical error message.
     */
    @Serializable
    internal data class CriticalErrorData(
        val type: EventDataType = EventDataType.CRITICAL_ERROR,
        val data: String
    ) : ErrorData()
}

/**
 * Maps [ErrorData] to [ErrorModel].
 *
 * @return The mapped [ErrorModel].
 */
internal fun ErrorData.mapToException(): MastercardSRCException = when (this) {
    is ErrorData.UserErrorData -> MastercardSRCException.CheckoutErrorException(
        displayableMessage = data
    )

    is ErrorData.CriticalErrorData -> MastercardSRCException.CheckoutErrorException(
        displayableMessage = data
    )
}
