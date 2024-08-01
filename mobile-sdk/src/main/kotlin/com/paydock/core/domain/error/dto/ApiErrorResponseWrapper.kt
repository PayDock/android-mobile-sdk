package com.paydock.core.domain.error.dto

import com.paydock.core.network.dto.Resource
import com.paydock.core.network.dto.error.ApiError
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorDetails
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.dto.error.ErrorSummaryDetails
import kotlinx.serialization.Serializable

@Serializable
data class SdkApiErrorResponse(
    private val response: ApiErrorResponse
) {
    val status: Int
        get() = response.status

    val error: SdkApiError?
        get() = response.error?.let { SdkApiError(it) }

    val resource: SdkResource<Unit>?
        get() = response.resource?.let { SdkResource(it) }

    val summary: SdkErrorSummary
        get() = SdkErrorSummary(response.summary)
}

@Serializable
data class SdkApiError(
    private val error: ApiError
) {
    val message: String
        get() = error.message

    val code: String
        get() = error.code

    val details: SdkErrorDetails?
        get() = error.details?.let { SdkErrorDetails(it) }
}

@Serializable
class SdkErrorDetails(
    private val errorDetails: ErrorDetails
) {
    val gatewaySpecificCode: String?
        get() = errorDetails.gatewaySpecificCode

    val gatewaySpecificDescription: String?
        get() = errorDetails.gatewaySpecificDescription

    val paramName: String?
        get() = errorDetails.paramName

    val description: String?
        get() = errorDetails.description

    val path: String?
        get() = errorDetails.path

    val messages: List<String>?
        get() = errorDetails.messages

    val statusCode: String?
        get() = errorDetails.statusCode

    val statusCodeDescription: String?
        get() = errorDetails.statusCodeDescription
}

@Serializable
data class SdkResource<T>(
    private val resource: Resource<T>
) {
    val type: String
        get() = resource.type

    val data: T?
        get() = resource.data
}

@Serializable
data class SdkErrorSummary(
    private val summary: ErrorSummary
) {
    val message: String
        get() = summary.message

    val code: String
        get() = summary.code

    val statusCode: String?
        get() = summary.statusCode

    val statusCodeDescription: String?
        get() = summary.statusCodeDescription

    val details: SdkErrorSummaryDetails?
        get() = summary.details?.let { SdkErrorSummaryDetails(it) }
}

@Serializable
data class SdkErrorSummaryDetails(
    private val summaryDetails: ErrorSummaryDetails
) {
    val gatewaySpecificCode: String?
        get() = summaryDetails.gatewaySpecificCode

    val gatewaySpecificDescription: String?
        get() = summaryDetails.gatewaySpecificDescription

    val messages: List<String>?
        get() = summaryDetails.messages

    val description: String?
        get() = summaryDetails.description

    val statusCode: String?
        get() = summaryDetails.statusCode

    val statusCodeDescription: String?
        get() = summaryDetails.statusCodeDescription

}

/**
 * Extension property that provides a error summary message.
 *
 * @return The displayable error message.
 */
val SdkApiErrorResponse?.displayableMessage: String
    get() = this?.summary?.message ?: ""