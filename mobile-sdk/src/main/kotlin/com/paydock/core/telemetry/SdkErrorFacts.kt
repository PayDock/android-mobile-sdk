package com.paydock.core.telemetry

import com.paydock.core.domain.error.exceptions.AfterpayException
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.domain.error.exceptions.ColesPayException
import com.paydock.core.domain.error.exceptions.GiftCardException
import com.paydock.core.domain.error.exceptions.GooglePayException
import com.paydock.core.domain.error.exceptions.PayPalDataCollectorException
import com.paydock.core.domain.error.exceptions.PayPalException
import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.core.domain.error.exceptions.ZipException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.exceptions.ApiException
import com.paydock.telemetry.TelemetryEvent
import java.util.Collections
import java.util.IdentityHashMap

/** What a reported throwable says, read from types, frames and codes only: never a message (R_01, R_03). */
internal object SdkErrorFacts {

    /** (throwable as? SdkException)?.let { it::class.simpleName }; null otherwise. */
    fun code(throwable: Throwable): String? = (throwable as? SdkException)?.let { it::class.simpleName }

    /**
     * ErrorInfo.from(throwable); when [cause] is given and throwable has no cause of its own, the result's cause is
     * ErrorInfo.from(cause), so the original's chain follows the merchant's exception. The joined chain keeps
     * [TelemetryEvent.ErrorInfo.MAX_CAUSES] causes at most, as ErrorInfo.from does.
     */
    fun errorInfo(throwable: Throwable, cause: Throwable?): TelemetryEvent.ErrorInfo {
        val info = TelemetryEvent.ErrorInfo.from(throwable)
        if (cause == null || cause === throwable || throwable.cause != null) return info
        return info.copy(cause = TelemetryEvent.ErrorInfo.from(cause).limit(TelemetryEvent.ErrorInfo.MAX_CAUSES))
    }

    /**
     * The first API error body found, in this order: the `error: ApiErrorResponse` of the SdkException subclasses
     * that have one (the list in [sdkExceptionBody]), then an ApiException anywhere in
     * throwable's or cause's chain. It maps to
     *   ApiError(status = response.status,
     *            code = response.error?.code,
     *            summaryCode = response.summary.code,
     *            gatewayCodes = distinct non-blank [response.error?.details?.gatewaySpecificCode,
     *                                               response.summary.details?.gatewaySpecificCode])
     * Blank codes become null. null when there is none.
     *
     * The SdkException subclasses are looked for in the same chains as the ApiException, throwable's first, so a
     * mapped SdkException handed on as [cause] gives its body too.
     */
    fun apiError(throwable: Throwable, cause: Throwable?): TelemetryEvent.ApiError? {
        val chain = chainOf(throwable) + (cause?.let(::chainOf).orEmpty())
        val response = chain.firstNotNullOfOrNull(::sdkExceptionBody)
            ?: chain.firstNotNullOfOrNull { (it as? ApiException)?.error }
        return response?.let(::toApiError)
    }

    /** The body of the SdkException subclasses that carry one; null for every other throwable. */
    @Suppress("CyclomaticComplexMethod") // one branch per subclass with an API error body
    private fun sdkExceptionBody(throwable: Throwable): ApiErrorResponse? = when (throwable) {
        is CardDetailsException.TokenisingCardException -> throwable.error
        is GiftCardException.TokenisingCardException -> throwable.error
        is AfterpayException.FetchingUrlException -> throwable.error
        is AfterpayException.CapturingChargeException -> throwable.error
        is ColesPayException.FetchingUrlException -> throwable.error
        is GooglePayException.TokenisingGooglePayException -> throwable.error
        is PayPalException.FetchingUrlException -> throwable.error
        is PayPalException.CapturingChargeException -> throwable.error
        is PayPalException.GetPayPalClientIdException -> throwable.error
        is PayPalVaultException.CreateSetupTokenException -> throwable.error
        is PayPalVaultException.GetPayPalClientIdException -> throwable.error
        is PayPalVaultException.CreatePaymentTokenException -> throwable.error
        is PayPalDataCollectorException.InitialisationClientIdException -> throwable.error
        is ZipException.FetchingCheckoutUrlException -> throwable.error
        is ZipException.CreatingPaymentSourceTokenException -> throwable.error
        else -> null
    }

    /**
     * Codes only: the body's message fields are never read. Shared with [NetworkFailureFacts], so an API error reads
     * the same in an SDK error and in a network failure.
     */
    internal fun toApiError(response: ApiErrorResponse): TelemetryEvent.ApiError = TelemetryEvent.ApiError(
        status = response.status,
        code = response.error?.code.nonBlank(),
        summaryCode = response.summary.code.nonBlank(),
        gatewayCodes = listOfNotNull(
            response.error?.details?.gatewaySpecificCode.nonBlank(),
            response.summary.details?.gatewaySpecificCode.nonBlank()
        ).distinct()
    )

    /** [throwable] and its causes, [TelemetryEvent.ErrorInfo.MAX_CAUSES] at most, stopping at a cause already seen. */
    private fun chainOf(throwable: Throwable): List<Throwable> {
        val visited = Collections.newSetFromMap(IdentityHashMap<Throwable, Boolean>())
        val chain = mutableListOf<Throwable>()
        var next: Throwable? = throwable
        while (next != null && chain.size <= TelemetryEvent.ErrorInfo.MAX_CAUSES && visited.add(next)) {
            chain.add(next)
            next = next.cause
        }
        return chain
    }

    /** This info with at most [depth] - 1 causes below it, so it fits as a cause [depth] deep. */
    private fun TelemetryEvent.ErrorInfo.limit(depth: Int): TelemetryEvent.ErrorInfo? = when {
        depth <= 0 -> null
        else -> copy(cause = cause?.limit(depth - 1))
    }

    private fun String?.nonBlank(): String? = this?.takeIf { it.isNotBlank() }
}
