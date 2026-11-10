package com.paydock.core.telemetry

import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.domain.error.exceptions.GiftCardException
import com.paydock.core.domain.error.exceptions.GooglePayException
import com.paydock.core.domain.error.exceptions.PayPalException
import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.core.domain.error.exceptions.ZipException
import com.paydock.core.network.dto.error.ApiError
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorDetails
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.dto.error.ErrorSummaryDetails
import com.paydock.core.network.exceptions.ApiException
import com.paydock.telemetry.TelemetryEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException
import java.lang.reflect.Modifier
import java.util.jar.JarFile

/**
 * Unit tests for [SdkErrorFacts] (R_01 to R_03): the error code, the cause rule and the API error fields, read from
 * types, frames and codes only. Every exception here is synthetic; no message is ever read.
 */
internal class SdkErrorFactsTest {

    private fun response(
        status: Int = 400,
        code: String? = "invalid_request",
        summaryCode: String = "validation_error",
        errorGateway: String? = "05",
        summaryGateway: String? = "N7",
    ) = ApiErrorResponse(
        status = status,
        error = code?.let { ApiError(message = "synthetic", code = it, details = ErrorDetails(errorGateway)) },
        summary = ErrorSummary(
            message = "synthetic",
            code = summaryCode,
            details = ErrorSummaryDetails(gatewaySpecificCode = summaryGateway)
        )
    )

    private val apiError = TelemetryEvent.ApiError(
        status = 400,
        code = "invalid_request",
        summaryCode = "validation_error",
        gatewayCodes = listOf("05", "N7")
    )

    // region code

    @Test
    fun code_isTheSimpleNameOfANestedSdkExceptionSubclass() {
        assertEquals(
            "TokenisingCardException",
            SdkErrorFacts.code(CardDetailsException.TokenisingCardException(response()))
        )
        assertEquals("ResultException", SdkErrorFacts.code(GooglePayException.ResultException("synthetic")))
        assertEquals(
            "NetworkError",
            SdkErrorFacts.code(GooglePayException.SDKException.NetworkError(statusCodeString = "7"))
        )
    }

    @Test
    fun code_isNullForAnyOtherThrowable() {
        assertNull(SdkErrorFacts.code(IllegalStateException()))
        assertNull(SdkErrorFacts.code(IOException()))
        assertNull(SdkErrorFacts.code(ApiException(response())))
    }

    // endregion

    // region errorInfo: the cause rule

    @Test
    fun errorInfo_withoutACause_isTheThrowablesOwnInfo() {
        val throwable = PayPalException.ConfigurationException("synthetic")

        assertEquals(TelemetryEvent.ErrorInfo.from(throwable), SdkErrorFacts.errorInfo(throwable, cause = null))
    }

    @Test
    fun errorInfo_withACause_andNoOwnCause_followsTheMerchantsExceptionWithTheOriginalsChain() {
        val original = IllegalStateException(IOException())
        val failure = PayPalException.InitialisationWalletTokenException("synthetic")

        val info = SdkErrorFacts.errorInfo(failure, cause = original)

        assertEquals(PayPalException.InitialisationWalletTokenException::class.java.name, info.type)
        assertEquals(TelemetryEvent.ErrorInfo.from(failure).frames, info.frames)
        assertEquals(TelemetryEvent.ErrorInfo.from(original), info.cause)
        assertEquals(
            listOf(IllegalStateException::class.java.name, IOException::class.java.name),
            info.causeTypes
        )
    }

    @Test
    fun errorInfo_whenTheThrowableHasItsOwnCause_keepsItsOwnChain() {
        val own = IOException()
        val throwable = IllegalStateException(own)

        val info = SdkErrorFacts.errorInfo(throwable, cause = IllegalArgumentException())

        assertEquals(TelemetryEvent.ErrorInfo.from(throwable), info)
        assertEquals(listOf(IOException::class.java.name), info.causeTypes)
    }

    @Test
    fun errorInfo_whenTheCauseIsTheThrowableItself_addsNoCause() {
        val throwable = ZipException.UnknownException("synthetic")

        assertEquals(TelemetryEvent.ErrorInfo.from(throwable), SdkErrorFacts.errorInfo(throwable, cause = throwable))
    }

    @Test
    fun errorInfo_keepsAtMostMaxCausesBelowTheMerchantsException() {
        val deep = (1..TelemetryEvent.ErrorInfo.MAX_CAUSES + 3).fold(IOException() as Throwable) { inner, _ ->
            IllegalStateException(inner)
        }

        val info = SdkErrorFacts.errorInfo(GooglePayException.ResultException("synthetic"), cause = deep)

        assertEquals(TelemetryEvent.ErrorInfo.MAX_CAUSES, info.causeTypes.size)
        assertEquals(IllegalStateException::class.java.name, info.cause?.type)
    }

    // endregion

    // region apiError

    @Test
    fun apiError_fromAnSdkExceptionsErrorBody() {
        assertEquals(apiError, SdkErrorFacts.apiError(CardDetailsException.TokenisingCardException(response()), null))
        assertEquals(apiError, SdkErrorFacts.apiError(GiftCardException.TokenisingCardException(response()), null))
        assertEquals(apiError, SdkErrorFacts.apiError(PayPalException.GetPayPalClientIdException(response()), null))
        assertEquals(
            apiError,
            SdkErrorFacts.apiError(ZipException.CreatingPaymentSourceTokenException(response()), null)
        )
    }

    @Test
    fun apiError_fromAMappedSdkExceptionHandedOnAsTheCause() {
        val mapped = GooglePayException.TokenisingGooglePayException(response())

        assertEquals(apiError, SdkErrorFacts.apiError(GooglePayException.ResultException("synthetic"), mapped))
    }

    @Test
    fun apiError_fromAnApiExceptionAnywhereInTheThrowablesOrTheCausesChain() {
        val api = ApiException(response())

        assertEquals(apiError, SdkErrorFacts.apiError(api, null))
        assertEquals(apiError, SdkErrorFacts.apiError(IllegalStateException(IOException(api)), null))
        assertEquals(
            apiError,
            SdkErrorFacts.apiError(GooglePayException.ResultException("synthetic"), IllegalStateException(api))
        )
    }

    @Test
    fun apiError_prefersAnSdkExceptionsErrorBody_overAnApiException() {
        val sdkBody = response(status = 402, code = "card_declined")
        val apiBody = response(status = 500, code = "server_error")

        val fromThrowable = SdkErrorFacts.apiError(
            PayPalException.CapturingChargeException(sdkBody),
            cause = ApiException(apiBody)
        )
        val fromCause = SdkErrorFacts.apiError(
            IllegalStateException(ApiException(apiBody)),
            cause = PayPalException.FetchingUrlException(sdkBody)
        )

        assertEquals(402, fromThrowable?.status)
        assertEquals("card_declined", fromThrowable?.code)
        assertEquals(402, fromCause?.status)
    }

    @Test
    fun apiError_isNullWithoutAnyErrorBody() {
        assertNull(SdkErrorFacts.apiError(IllegalStateException(IOException()), null))
        assertNull(SdkErrorFacts.apiError(PayPalException.ConfigurationException("synthetic"), IOException()))
        assertNull(SdkErrorFacts.apiError(CardDetailsException.ParseException("synthetic", errorBody = null), null))
    }

    @Test
    fun apiError_blankCodesBecomeNull_andGatewayCodesAreDistinctAndNonBlank() {
        val blank = SdkErrorFacts.apiError(
            ApiException(response(code = " ", summaryCode = "", errorGateway = " ", summaryGateway = "")),
            null
        )
        val same = SdkErrorFacts.apiError(ApiException(response(errorGateway = "05", summaryGateway = "05")), null)
        val noError = SdkErrorFacts.apiError(ApiException(response(code = null, summaryGateway = null)), null)

        assertEquals(TelemetryEvent.ApiError(status = 400), blank)
        assertEquals(listOf("05"), same?.gatewayCodes)
        assertEquals(TelemetryEvent.ApiError(status = 400, summaryCode = "validation_error"), noError)
    }

    @Test
    fun apiError_stopsAtACyclicChain() {
        val first = IllegalStateException()
        val second = IllegalArgumentException(first)
        first.initCause(second)

        assertNull(SdkErrorFacts.apiError(first, null))
        assertSame(second, first.cause)
        assertTrue(SdkErrorFacts.errorInfo(first, null).causeTypes.isNotEmpty())
    }

    // endregion

    /**
     * Every SdkException subclass holding an ApiErrorResponse property gives its body (R_03): the subclasses are found by
     * reflection over the compiled exceptions package, so a new one that SdkErrorFacts does not read fails here.
     */
    @Test
    fun everySdkExceptionWithAnApiErrorBody_isReadByApiError() {
        val base = SdkException::class.java
        val location = File(base.protectionDomain!!.codeSource!!.location.toURI())
        val packageName = base.name.substringBeforeLast('.')
        val packagePath = packageName.replace('.', '/') + "/"
        // The compiled classes are a directory or a jar, depending on how Gradle wires the unit-test classpath.
        val entries: List<String> = if (location.isDirectory) {
            File(location, packagePath).listFiles().orEmpty().map { packagePath + it.name }
        } else {
            JarFile(location).use { jar -> jar.entries().toList().map { it.name } }
        }
        val classNames = entries
            .filter { it.startsWith(packagePath) && it.endsWith(".class") && '/' !in it.removePrefix(packagePath) }
            .map { it.removeSuffix(".class").replace('/', '.') }
        assertTrue("no compiled exceptions found in ${location.path}", classNames.isNotEmpty())

        val withBody = classNames
            .map { Class.forName(it) }
            .filter { base.isAssignableFrom(it) && !Modifier.isAbstract(it.modifiers) }
            .filter { type -> type.declaredFields.any { it.type == ApiErrorResponse::class.java } }
        assertTrue("found only ${withBody.size} subclasses with an API error body", withBody.size >= 15)

        val body = ApiErrorResponse(status = 418, summary = ErrorSummary(code = "reflected", message = "synthetic"))
        withBody.forEach { type ->
            val constructor = type.constructors.singleOrNull {
                it.parameterTypes.contentEquals(arrayOf(ApiErrorResponse::class.java))
            }
            assertTrue("${type.name} has no (ApiErrorResponse) constructor for this test", constructor != null)
            val exception = constructor!!.newInstance(body) as Throwable

            val apiError = SdkErrorFacts.apiError(exception, cause = null)

            assertEquals("${type.name} is not read by SdkErrorFacts.apiError", 418, apiError?.status)
            assertEquals("reflected", apiError?.summaryCode)
        }
    }
}
